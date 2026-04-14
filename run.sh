#!/bin/bash
# Script de lancement EcoTrack - JavaFX (pour Linux/macOS)

echo "========================================"
echo " EcoTrack - Application JavaFX"
echo "========================================"
echo

# Vérifier Java
java -version
if [ $? -ne 0 ]; then
    echo "ERREUR: Java n'est pas installé ou pas dans le PATH"
    exit 1
fi

# Vérifier Maven
mvn -v > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "ERREUR: Maven n'est pas installé ou pas dans le PATH"
    echo
    echo "Installation Maven:"
    echo "  Ubuntu/Debian: sudo apt-get install maven"
    echo "  macOS: brew install maven"
    echo "  Autre: https://maven.apache.org/"
    exit 1
fi

# Lancer l'application
echo
echo "Lancement de l'application EcoTrack..."
echo

mvn clean javafx:run

if [ $? -ne 0 ]; then
    echo
    echo "ERREUR lors du lancement de l'application!"
    exit 1
fi

echo
echo "Application terminée."

