# Script de lancement de l'application EcoTrack
# Ce script initialise la base de données MySQL et lance l'application JavaFX

Write-Host "========================================" -ForegroundColor Green
Write-Host "  EcoTrack - Gestion des Annonces" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Étape 1: Réinitialiser la base de données
Write-Host "1. Initialisation de la base de données MySQL..." -ForegroundColor Yellow

# Vérifier si MySQL est accessible
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$mysqlPath8030 = "C:\Program Files\MySQL\MySQL Server 8.0.30\bin\mysql.exe"

if (-not (Test-Path $mysqlPath)) {
    if (-not (Test-Path $mysqlPath8030)) {
        Write-Host "⚠️  MySQL n'a pas été trouvé. Vérifiez que MySQL est installé." -ForegroundColor Red
        Write-Host "Vous devez exécuter le script setup.sql manuellement:" -ForegroundColor Cyan
        Write-Host "  mysql -u root < setup.sql" -ForegroundColor Cyan
        Write-Host ""
    } else {
        $mysqlPath = $mysqlPath8030
    }
}

if (Test-Path $mysqlPath) {
    Try {
        & $mysqlPath -u root < setup.sql 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Base de données initialisée avec succès!" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Erreur lors de l'initialisation de la base de données." -ForegroundColor Yellow
        }
    } Catch {
        Write-Host "⚠️  Impossible d'exécuter MySQL: $_" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠️  Veuillez exécuter le script setup.sql manuellement." -ForegroundColor Yellow
}

Write-Host ""

# Étape 2: Compiler et package
Write-Host "2. Compilation du projet..." -ForegroundColor Yellow
mvn clean package -DskipTests -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilation réussie!" -ForegroundColor Green
} else {
    Write-Host "✗ Erreur de compilation!" -ForegroundColor Red
    Exit 1
}

Write-Host ""

# Étape 3: Lancer l'application
Write-Host "3. Lancement de l'application JavaFX..." -ForegroundColor Yellow
Write-Host "L'application s'ouvrira dans quelques instants..." -ForegroundColor Cyan
Write-Host ""

mvn javafx:run

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Application fermée" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

