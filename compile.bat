@echo off
REM Script de compilation du projet EcoTrack CrudAnnonce
REM Configuration: Java 17, MySQL JDBC, JavaFX 17

cd /d %~dp0

REM Créer les répertoires de compilation s'ils n'existent pas
if not exist "target\classes" mkdir target\classes

REM Compiler tous les fichiers Java
echo Compilation en cours...
javac -d target\classes ^
  --module-path "lib" ^
  -cp "lib/*" ^
  src\main\java\utils\*.java ^
  src\main\java\entities\*.java ^
  src\main\java\services\*.java ^
  src\main\java\gui\*.java ^
  src\main\java\main\*.java ^
  src\main\java\org\example\*.java

if %ERRORLEVEL% EQU 0 (
  echo Compilation reussie!
) else (
  echo Erreur lors de la compilation
  exit /b 1
)

echo.
echo Pour continuer, installez les dependances Maven:
echo - mysql-connector-java 8.0.15
echo - javafx-fxml 17.0.2
echo - javafx-controls 17.0.2
echo.
echo Puis executez: mvn compile
pause

