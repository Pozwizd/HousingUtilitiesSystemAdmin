# View logs for Housing Utilities System
# For Windows 10

param(
    [string]$Service = "app"
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Logs for service: $Service" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to exit" -ForegroundColor Gray
Write-Host ""

docker-compose logs -f $Service
