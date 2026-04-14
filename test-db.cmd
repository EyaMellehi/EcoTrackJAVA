@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
for /f %%i in ('call mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout 2^>nul') do set CLASSPATH=.\target\classes;%%i
java -cp "%CLASSPATH%" org.example.Main

