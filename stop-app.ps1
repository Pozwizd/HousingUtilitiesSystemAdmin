# Stop Housing Utilities System
# For Windows 10

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Housing Utilities System - Stopping" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Stopping containers..." -ForegroundColor Yellow
docker-compose down

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "[SUCCESS] Containers stopped successfully" -ForegroundColor Green
    Write-Host ""
    Write-Host "Note: MongoDB data is preserved in Docker volumes" -ForegroundColor Gray
    Write-Host "To completely remove data use: docker-compose down -v" -ForegroundColor Gray
} else {
    Write-Host "[ERROR] Failed to stop containers" -ForegroundColor Red
    exit 1
}
