cd C:\Users\bhiri\Downloads\3A38\CrudAnnonce

# Script pour ajouter la colonne media_path à MySQL
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Ajout de media_path a la BD ecotrack" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$sql = @"
USE ecotrack;
ALTER TABLE annonce ADD COLUMN IF NOT EXISTS media_path VARCHAR(500) DEFAULT NULL;
SELECT "✅ Colonne media_path ajoutée!" AS Message;
"@

# Chercher MySQL
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysqlPath)) {
    $mysqlPath = "C:\xampp\mysql\bin\mysql.exe"
}
if (-not (Test-Path $mysqlPath)) {
    $mysqlPath = "mysql"
}

Write-Host "Exécution du script SQL..." -ForegroundColor Yellow
Write-Host ""

# Exécuter
$sql | & $mysqlPath -u root 2>&1 | Write-Host

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ SUCCÈS! Colonne créée." -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines étapes:" -ForegroundColor Green
    Write-Host "  1. mvn clean compile" -ForegroundColor White
    Write-Host "  2. mvn javafx:run" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "⚠️  Vérifiez que MySQL est lancé" -ForegroundColor Yellow
}

Write-Host ""

