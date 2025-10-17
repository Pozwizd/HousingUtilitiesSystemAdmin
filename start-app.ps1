# Start Housing Utilities System with MongoDB Replica Set
# For Windows 10

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Housing Utilities System - Starting" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Check Docker installation
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "[OK] Docker installed: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker not found! Install Docker Desktop for Windows" -ForegroundColor Red
    Write-Host "Download: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Check if Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "[OK] Docker is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker is not running! Start Docker Desktop" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Starting containers..." -ForegroundColor Yellow

# Stop old containers if exist
Write-Host "Stopping old containers (if any)..." -ForegroundColor Yellow
docker-compose down 2>$null

# Start containers
Write-Host "Building and starting application..." -ForegroundColor Yellow
docker-compose up --build -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host "[SUCCESS] Application started!" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Services:" -ForegroundColor Cyan
    Write-Host "  - Application: http://localhost:8080" -ForegroundColor White
    Write-Host "  - MongoDB Primary (mongo1): localhost:27017" -ForegroundColor White
    Write-Host "  - MongoDB Secondary (mongo2): localhost:27018" -ForegroundColor White
    Write-Host "  - MongoDB Secondary (mongo3): localhost:27019" -ForegroundColor White
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Cyan
    Write-Host "  - View logs: docker-compose logs -f" -ForegroundColor White
    Write-Host "  - Stop: docker-compose down" -ForegroundColor White
    Write-Host "  - Status: docker-compose ps" -ForegroundColor White
    Write-Host ""
    
    # Show container status
    Write-Host "Container Status:" -ForegroundColor Cyan
    docker-compose ps
    
    Write-Host ""
    Write-Host "Waiting for application to start (may take 1-2 minutes)..." -ForegroundColor Yellow
    Write-Host "You can monitor: docker-compose logs -f app" -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host "[ERROR] Failed to start!" -ForegroundColor Red
    Write-Host "Check logs: docker-compose logs" -ForegroundColor Yellow
    exit 1
}
