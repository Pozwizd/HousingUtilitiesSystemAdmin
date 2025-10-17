# Complete cleanup of Housing Utilities System
# For Windows 10

Write-Host "=====================================" -ForegroundColor Red
Write-Host "WARNING: Complete Cleanup" -ForegroundColor Red
Write-Host "=====================================" -ForegroundColor Red
Write-Host ""
Write-Host "This will remove:" -ForegroundColor Yellow
Write-Host "  - All containers" -ForegroundColor White
Write-Host "  - All MongoDB data" -ForegroundColor White
Write-Host "  - All uploaded files" -ForegroundColor White
Write-Host "  - Docker images" -ForegroundColor White
Write-Host ""

$confirmation = Read-Host "Are you sure? (yes/no)"
if ($confirmation -ne 'yes') {
    Write-Host "Operation cancelled" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Stopping and removing containers..." -ForegroundColor Yellow
docker-compose down -v

Write-Host "Removing application images..." -ForegroundColor Yellow
docker rmi housingutilitiessystemadmin-app 2>$null

Write-Host "Cleaning unused images..." -ForegroundColor Yellow
docker image prune -f

Write-Host ""
Write-Host "[SUCCESS] Complete cleanup finished" -ForegroundColor Green
Write-Host ""
Write-Host "To start fresh use: .\start-app.ps1" -ForegroundColor Cyan
