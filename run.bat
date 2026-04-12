@echo off
REM Script pour démarrer l'application JavaFX CrudAnnonce
REM Assurez-vous que Maven est installé et Java 17+ est disponible

set MAVEN_HOME=C:\Maven\apache-maven-3.8.1
set PATH=%MAVEN_HOME%\bin;%PATH%

cd /d "%~dp0"

echo ======================================
echo EcoTrack - CrudAnnonce Application
echo ======================================
echo.
echo Compilation et lancement de l'application...
echo.

call mvn javafx:run

pause

