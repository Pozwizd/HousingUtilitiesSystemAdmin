# Check Housing Utilities System Status
# For Windows 10

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Housing Utilities System - Status" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Containers:" -ForegroundColor Yellow
docker-compose ps

Write-Host ""
Write-Host "MongoDB Replica Set Status:" -ForegroundColor Yellow
docker exec mongo1 mongosh --eval "rs.status().members.forEach(m => print(m.name + ' - ' + m.stateStr))" --quiet 2>$null

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "[OK] Replica Set is working properly" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[WARNING] Replica Set not initialized or MongoDB not running" -ForegroundColor Yellow
}
