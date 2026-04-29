@echo off
REM Script de lancement EcoTrack - JavaFX

set "PROJECT_DIR=%~dp0"
set "COHERE_KEY_FILE=%PROJECT_DIR%cohere_api_key.local.txt"
set "TWILIO_CFG_FILE=%PROJECT_DIR%twilio_sms.local.txt"

REM Charger la cle API automatiquement si elle n'est pas deja definie
if "%COHERE_API_KEY%"=="" (
    if exist "%COHERE_KEY_FILE%" (
        set /p COHERE_API_KEY=<"%COHERE_KEY_FILE%"
    )
)

REM Charger la config SMS Twilio automatiquement pour toute variable manquante
if exist "%TWILIO_CFG_FILE%" (
    for /f "usebackq tokens=1,* delims==" %%A in ("%TWILIO_CFG_FILE%") do (
        if /I "%%A"=="TWILIO_ACCOUNT_SID" if "%TWILIO_ACCOUNT_SID%"=="" set "TWILIO_ACCOUNT_SID=%%B"
        if /I "%%A"=="TWILIO_AUTH_TOKEN" if "%TWILIO_AUTH_TOKEN%"=="" set "TWILIO_AUTH_TOKEN=%%B"
        if /I "%%A"=="TWILIO_FROM_NUMBER" if "%TWILIO_FROM_NUMBER%"=="" set "TWILIO_FROM_NUMBER=%%B"
        if /I "%%A"=="TWILIO_DEFAULT_COUNTRY_CODE" if "%TWILIO_DEFAULT_COUNTRY_CODE%"=="" set "TWILIO_DEFAULT_COUNTRY_CODE=%%B"
    )
)

if "%COHERE_API_KEY%"=="" (
    echo ERREUR: COHERE_API_KEY est introuvable.
    echo.
    echo Option 1 ^(recommandee^): creez le fichier suivant avec la cle sur la premiere ligne:
    echo   %COHERE_KEY_FILE%
    echo.
    echo Option 2: definir une variable d'environnement Windows permanente:
    echo   setx COHERE_API_KEY "votre_cle"
    echo.
    pause
    exit /b 1
)

echo ========================================
echo  EcoTrack - Application JavaFX
echo ========================================
echo.

REM Verifier que Java 21 est installe
java -version
if errorlevel 1 (
    echo ERREUR: Java n'est pas dans le PATH
    echo Installez JDK 21 ou ajoutez-le au PATH
    pause
    exit /b 1
)

REM Verifier que Maven est installe
mvn -v > nul 2>&1
if errorlevel 1 (
    echo ERREUR: Maven n'est pas installe ou pas dans le PATH
    echo.
    echo Solutions:
    echo 1. Installez Maven depuis: https://maven.apache.org/
    echo 2. Ou utilisez IntelliJ pour lancer le projet
    echo.
    echo En attendant, nous allons compiler et lancer via Java directement...
    echo.

    REM Compiler avec javac
    echo Compilation du projet...
    cd /d "%~dp0"

    REM Créer les dossiers
    if not exist "target\classes" mkdir "target\classes"

    REM Compiler ^(simplifiee - pas ideal^)
    echo Veuillez utiliser Maven ou IntelliJ pour une compilation appropriee
    pause
    exit /b 1
)

REM Lancer l'application
echo.
echo Lancement de l'application EcoTrack...
echo.

mvn clean javafx:run

if errorlevel 1 (
    echo.
    echo ERREUR lors du lancement de l'application!
    pause
    exit /b 1
)

echo.
echo Application terminee.
pause
