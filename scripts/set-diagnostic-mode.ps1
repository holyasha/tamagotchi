param(
    [ValidateSet('NORMAL','SLOW','FAIL')][string]$Mode = 'NORMAL',
    [ValidateRange(1,120)][int]$Seconds = 90,
    [string]$ServiceUrl = 'http://localhost:8083',
    [string]$Issuer = 'http://localhost:8085/realms/tamagotchi'
)
$ErrorActionPreference = 'Stop'
$operator = Invoke-RestMethod -Method Post -Uri "$Issuer/protocol/openid-connect/token" `
    -ContentType 'application/x-www-form-urlencoded' -Body @{
        grant_type='client_credentials'; client_id='tamagotchi-ops-client'; client_secret='tamagotchi-ops-secret'
    }
Invoke-RestMethod -Method Put -Headers @{Authorization="Bearer $($operator.access_token)"} `
    -Uri "$ServiceUrl/internal/admin/failure-mode?mode=$Mode&seconds=$Seconds"
Write-Host "Diagnostic mode set to $Mode for $Seconds seconds"
