$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$source = Join-Path $root "src/main/resources/application.example.yaml"
$target = Join-Path $root "src/main/resources/application.yaml"
Copy-Item -Path $source -Destination $target -Force
Write-Host "application.yaml substituido pela configuracao baseada em variaveis de ambiente."
Write-Host "Agora defina DB_URL, DB_USER e DB_PASSWORD antes de iniciar o backend."
