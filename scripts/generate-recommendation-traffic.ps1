param(
    [ValidateRange(1,1000)][int]$Count = 30,
    [ValidateRange(0,5000)][int]$IntervalMs = 200,
    [string]$ServiceUrl = 'http://localhost:8083',
    [string]$Issuer = 'http://localhost:8085/realms/tamagotchi'
)
$ErrorActionPreference = 'Stop'
if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'Нужен PowerShell 7 (pwsh).' }
$tokenResponse = Invoke-RestMethod -Method Post -Uri "$Issuer/protocol/openid-connect/token" `
    -ContentType 'application/x-www-form-urlencoded' -Body @{
        grant_type='client_credentials'; client_id='tamagotchi-service-client'; client_secret='tamagotchi-service-secret'
    }
$statuses = @{}
for ($i=0; $i -lt $Count; $i++) {
    $petId = [guid]::NewGuid()
    $response = Invoke-WebRequest -Uri "$ServiceUrl/internal/recommendations/$petId" `
        -Headers @{Authorization="Bearer $($tokenResponse.access_token)"} -SkipHttpErrorCheck
    $status = [string][int]$response.StatusCode
    if ($status -notin @('200','503')) { throw "Неожиданный HTTP $status; проверьте токен и настройки." }
    $statuses[$status] = 1 + [int]$statuses[$status]
    if ($IntervalMs -gt 0) { Start-Sleep -Milliseconds $IntervalMs }
}
[pscustomobject]@{Requests=$Count; Statuses=($statuses | ConvertTo-Json -Compress)}
