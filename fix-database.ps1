# Script pour ajouter la colonne media_path à MySQL

Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Ajout de la colonne media_path à la BD          ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Chercher mysql.exe
$mysqlPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0.30\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp\bin\mysql\mysql5.7.36\bin\mysql.exe"
)

$mysqlPath = $null
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysqlPath = $path
        Write-Host "✓ MySQL trouvé: $path" -ForegroundColor Green
        break
    }
}

if ($mysqlPath -eq $null) {
    Write-Host "❌ MySQL n'a pas été trouvé!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Solutions:" -ForegroundColor Yellow
    Write-Host "1. Installer MySQL Server"
    Write-Host "2. OU exécuter manuellement dans MySQL Workbench:"
    Write-Host ""
    Write-Host "USE ecotrack;"
    Write-Host "ALTER TABLE annonce ADD COLUMN media_path VARCHAR(500) DEFAULT NULL;"
    Write-Host ""
    Write-Host "Puis relancer l'application"
    exit 1
}

Write-Host ""
Write-Host "Exécution du script SQL..." -ForegroundColor Yellow

# Exécuter le script
$scriptPath = Join-Path $PSScriptRoot "add_media_column.sql"
Get-Content $scriptPath | & $mysqlPath -u root 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ SUCCÈS! La colonne media_path a été ajoutée." -ForegroundColor Green
    Write-Host ""
    Write-Host "Vous pouvez maintenant:"
    Write-Host "  1. Lancer l'application: mvn javafx:run"
    Write-Host "  2. OU exécuter: .\run-app.ps1"
} else {
    Write-Host ""
    Write-Host "❌ ERREUR lors de l'exécution du script SQL" -ForegroundColor Red
    Write-Host "Veuillez exécuter manuellement:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "mysql -u root < add_media_column.sql"
}

Write-Host ""
Pause

