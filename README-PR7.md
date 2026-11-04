# PR7 — промышленная наблюдаемость для проекта «Тамагочи»

Архив содержит только новые и изменённые файлы. Корневая папка архива соответствует корню проекта `tamagotchi`. Файлы из архива нужно скопировать в существующий проект с сохранением структуры; при совпадении путей заменить файлы.

## Что добавлено

- Prometheus Registry и отдельный management HTTP-сервер на loopback: pet-service `9080`, recommendation-service `9083`, gateway `9088`.
- Метрики HTTP, гистограммы задержки и JVM; прикладные `pet.recommendation.requests`, `pet.recommendation.processing`, `pet.recommendation.active` с ограниченными метками `result=success|failure`.
- Пробы `/livez` и `/readyz` на бизнес-портах; readiness pet-service зависит от PostgreSQL, gateway — от Redis, liveness не зависит от внешних хранилищ.
- Структурированные JSON ECS-логи в `runtime-logs/` с полями корреляции, которые уже используются в проекте PR6.
- Prometheus, Loki, Alloy и Alertmanager в Docker Compose; дашборд Grafana с нагрузкой, ошибками, p95, SLI и JVM.
- Контролируемые режимы `NORMAL`, `SLOW` (750 мс), `FAIL` (HTTP 503) в recommendation-service. Переключение защищено ролью OPERATOR.
- PowerShell-скрипты генерации запросов и переключения режима диагностики.

## Критический сценарий

Пользователь через BFF запрашивает рекомендации по питомцу: `GET /api/pets/{petId}/recommendation`. Gateway передаёт запрос в pet-service, который проверяет питомца и вызывает recommendation-service: `GET /internal/recommendations/{petId}`. Для воспроизводимой нагрузки скрипт `scripts/generate-recommendation-traffic.ps1` вызывает защищённый внутренний endpoint recommendation-service напрямую. Это позволяет изолированно проверить HTTP-метрики сервиса рекомендаций, не создавая фиктивные записи питомцев в БД.

## Запуск

1. Установить изменения из архива в корень проекта.
2. Открыть PowerShell 7 (`pwsh`) в корне проекта.
3. Поднять инфраструктуру: `docker compose up -d`. Не используйте `-v`, чтобы не удалять данные существующих томов.
4. Проверить контейнеры: `docker compose ps`. Дождаться готовности Keycloak, Loki и Tempo.
5. Собрать три самостоятельных Maven-проекта (в корне проекта):
   - `./mvnw.cmd -DskipTests install` — pet-service;
   - `./mvnw.cmd -f recommendation-service/pom.xml -DskipTests package`;
   - `./mvnw.cmd -f gateway-service/pom.xml -DskipTests package`.
   На Linux/macOS используйте `./mvnw` вместо `./mvnw.cmd`.
6. Запустить приложения в отдельных терминалах. Для каждого сервиса важно сохранить его рабочий каталог, поскольку пути лог-файлов заданы относительно него:
   - из корня проекта: `./mvnw.cmd spring-boot:run` — pet-service;
   - перейти в `recommendation-service` и выполнить `../mvnw.cmd spring-boot:run`;
   - перейти в `gateway-service` и выполнить `../mvnw.cmd spring-boot:run`.
7. Открыть Prometheus Targets: `http://localhost:9090/targets`. Ожидаются три UP: `gateway-service`, `pet-service`, `recommendation-service`.
8. Открыть Grafana: `http://localhost:3000` (admin / admin-dev-only), дашборд `Tamagotchi — Observability`.

## Проверка изоляции Actuator

В `pwsh` выполнить:

```powershell
(Invoke-WebRequest http://localhost:9080/actuator/prometheus).StatusCode
(Invoke-WebRequest http://localhost:9083/actuator/prometheus).StatusCode
(Invoke-WebRequest http://localhost:9088/actuator/prometheus).StatusCode
(Invoke-WebRequest http://localhost:8080/actuator/prometheus -SkipHttpErrorCheck).StatusCode
```

Три служебных адреса должны отвечать `200`; на основном бизнес-порту endpoint Prometheus не должен быть опубликован. Management-порты привязаны к `127.0.0.1`; не меняйте адрес на `0.0.0.0` без сетевых ограничений.

## Нормальная нагрузка и управляемые инциденты

Нормальный режим:

```powershell
./scripts/set-diagnostic-mode.ps1 -Mode NORMAL
./scripts/generate-recommendation-traffic.ps1 -Count 30 -IntervalMs 200
```

Медленный успешный ответ — HTTP 200, но около 750 мс, то есть нарушение порога SLO 500 мс:

```powershell
try {
  ./scripts/set-diagnostic-mode.ps1 -Mode SLOW -Seconds 45
  ./scripts/generate-recommendation-traffic.ps1 -Count 20 -IntervalMs 200
} finally {
  ./scripts/set-diagnostic-mode.ps1 -Mode NORMAL
}
```

Быстрый отказ — HTTP 503; для перехода алерта в firing нужно достаточно запросов и удержание условия не менее 20 секунд:

```powershell
try {
  ./scripts/set-diagnostic-mode.ps1 -Mode FAIL -Seconds 90
  ./scripts/generate-recommendation-traffic.ps1 -Count 180 -IntervalMs 250
} finally {
  ./scripts/set-diagnostic-mode.ps1 -Mode NORMAL
}
./scripts/generate-recommendation-traffic.ps1 -Count 20 -IntervalMs 200
```

Алерты доступны в `http://localhost:9090/alerts` и `http://localhost:9093`. Они могут оставаться активными до выхода ошибок из скользящего минутного окна.

## PromQL и SLI/SLO

Путь наблюдения на стороне recommendation-service: `GET /internal/recommendations/{petId}`. HTTP-метрики используют нормализованный URI, а не фактический UUID питомца.

- Нагрузка: `sum(rate(http_server_requests_seconds_count{job="recommendation-service",uri="/internal/recommendations/{petId}"}[1m]))`
- Доля 5xx: `tamagotchi:recommendation_error_ratio:rate1m`
- p95 успешных ответов: `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job="recommendation-service",uri="/internal/recommendations/{petId}",status="200"}[1m])))`
- Доменный счётчик: `pet_recommendation_requests_total`
- Текущая параллельность: `pet_recommendation_active`

SLI: доля подходящих аутентифицированных запросов к `GET /internal/recommendations/{petId}` со статусом 200 и временем ответа менее 500 мс. Подходящие запросы — ответы 2xx и 5xx; ошибки аутентификации 401/403 и невалидные UUID 400 исключаются. Медленный 200 и любой 5xx считаются плохими запросами.

SLO: не менее 99,5% хороших запросов на скользящем окне 7 дней. Бюджет ошибок — 0,5% подходящих запросов. Для учебной демонстрации SLI отображается на 5-минутном окне; такой короткий срез не подтверждает выполнение семидневного SLO. Алерт срабатывает при доле 5xx выше 20% за минуту, если за окно было не менее 10 запросов, и условие сохраняется 20 секунд.

## Логи и трассировка

Alloy читает `runtime-logs/tamagotchi-*.log` и отправляет JSON ECS в Loki. В Grafana Explore выберите Loki и выполните:

```logql
{job="tamagotchi-apps"} | json | outcome="failure"
```

Для поиска конкретного запроса используйте `requestId`, извлечённый из ответа `X-Request-ID` или записей приложения:

```logql
{job="tamagotchi-apps"} | json | requestId="ВАШ_REQUEST_ID"
```

В traceId сохраняется корреляция с Tempo, поскольку существующая W3C-трассировка PR6 не отключается. Exemplars доступны на гистограмме при наличии трассы и данных для конкретной точки.

## Runbook при алерте

1. Открыть дашборд и определить, выросла ли доля 5xx или задержка p95.
2. Если выросла задержка без 5xx — проверить SLOW/внутреннюю нагрузку и p95; если выросли 5xx — проверить логи `outcome="failure"`.
3. Скопировать `requestId`/`traceId`, найти запись в Loki и открыть связанную трассу в Tempo.
4. Проверить доступность `pet-service`, `recommendation-service`, PostgreSQL и Redis; отдельно проверить `up` в Prometheus.
5. После устранения причины выполнить тестовый трафик в NORMAL и убедиться, что error ratio снижается, SLI восстанавливается, а алерт переходит в resolved.

## Важные замечания

- Скрипт генерации трафика обращается напрямую к recommendation-service и использует service client. Для демонстрации полного пользовательского маршрута дополнительно выполните вход в BFF и запрос `GET /api/pets/{petId}/recommendation` для существующего питомца.
- В архиве не меняются настройки PR6 трассировки, Keycloak realm, миграции БД и бизнес-модель питомца.
- Внешняя доставка Alertmanager намеренно не настроена: инциденты проверяются локально через веб-интерфейс.
