param(
    [string]$TokenUrl = "http://localhost:8085/realms/tamagotchi/protocol/openid-connect/token",
    [string]$ClientId = "tamagotchi-service-client",
    [string]$ClientSecret = "tamagotchi-service-secret"
)

$response = Invoke-RestMethod -Method Post -Uri $TokenUrl `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{
        grant_type = "client_credentials"
        client_id = $ClientId
        client_secret = $ClientSecret
    }

$response.access_token
