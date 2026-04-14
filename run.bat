@echo off
REM Script de lancement EcoTrack - JavaFX

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

    REM Compiler (simplifiee - pas ideal)
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

