@echo off
REM Réinitialiser complètement la base de données ecotrack

cd /d "C:\Users\bhiri\Downloads\3A38\CrudAnnonce"

echo ================================================
echo  REINITIALISATION COMPLETE DE LA BD
echo ================================================
echo.

REM Chercher MySQL
set MYSQL_PATH=
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    goto FOUND
)
if exist "C:\xampp\mysql\bin\mysql.exe" (
    set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
    goto FOUND
)

REM Sinon chercher dans PATH
where mysql.exe >nul 2>&1
if %errorlevel% equ 0 (
    set MYSQL_PATH=mysql.exe
    goto FOUND
)

echo ❌ MySQL non trouvé!
echo Installez MySQL ou XAMPP
pause
exit /b 1

:FOUND
echo ✓ MySQL trouvé: %MYSQL_PATH%
echo.
echo Exécution du script setup.sql...
echo.

REM Exécuter le script
"%MYSQL_PATH%" -u root < setup.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ BASE DE DONNEES REINITIALISEE AVEC SUCCES!
    echo.
    echo Les tables ont été créées:
    echo  - annonce (avec media_path, date_pub auto)
    echo  - commentaire
    echo.
    pause
) else (
    echo.
    echo ❌ ERREUR lors de l'exécution du script
    echo Vérifiez que MySQL est lancé!
    pause
)

