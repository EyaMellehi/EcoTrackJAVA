#!/usr/bin/env powershell
# Build and Run Script for EcoTrack CrudAnnonce with JavaFX

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "EcoTrack - Build & Run Script" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Check if Maven is installed
$mvnCommand = $null
$mavenPath = Get-Command mvn -ErrorAction SilentlyContinue
if ($mavenPath) {
    $mvnCommand = "mvn"
    Write-Host "✓ Maven found!" -ForegroundColor Green
} else {
    # Try to find Maven in common locations
    $commonPaths = @(
        "C:\Program Files\Apache\Maven\bin\mvn.cmd",
        "C:\Program Files (x86)\Apache\Maven\bin\mvn.cmd",
        "C:\Maven\bin\mvn.cmd",
        "$env:MAVEN_HOME\bin\mvn.cmd"
    )

    foreach ($path in $commonPaths) {
        if (Test-Path $path) {
            $mvnCommand = $path
            Write-Host "✓ Maven found at: $path" -ForegroundColor Green
            break
        }
    }
}

if (-not $mvnCommand) {
    Write-Host "✗ Maven not found. Please install Maven or set MAVEN_HOME environment variable." -ForegroundColor Red
    exit 1
}

# Change to project directory
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location $projectDir

try {
    Write-Host ""
    Write-Host "1. Cleaning previous build..." -ForegroundColor Cyan
    & $mvnCommand clean

    Write-Host ""
    Write-Host "2. Compiling project..." -ForegroundColor Cyan
    & $mvnCommand compile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Compilation failed!" -ForegroundColor Red
        exit 1
    }

    Write-Host ""
    Write-Host "3. Running with JavaFX..." -ForegroundColor Cyan
    & $mvnCommand javafx:run

} finally {
    Pop-Location
}

