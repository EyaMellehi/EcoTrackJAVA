# 🔧 Guide Installation Maven & Lancement

## ❌ Problème
```
mvn : Le terme «mvn» n'est pas reconnu...
```

Cela signifie que **Maven n'est pas installé** ou **n'est pas dans le PATH** de votre système.

---

## ✅ Solution 1 : **Utiliser IntelliJ** (LA PLUS SIMPLE - RECOMMANDÉ)

### Étapes :
1. **Ouvrez IntelliJ IDEA**
2. **File → Open** 
3. Sélectionnez le dossier : `C:\Users\ademc\OneDrive\Desktop\EcoTrackJAVA-main`
4. Cliquez sur **"Open as Project"**
5. Attendez que IntelliJ indexe le projet (quelques secondes)
6. **Right-click** sur `src/main/java/org/example/MainFX.java`
7. Sélectionnez **"Run 'MainFX'"** (ou appuyez sur **Shift+F10**)

✅ L'application devrait démarrer !

---

## ✅ Solution 2 : **Installer Maven** (RECOMMANDÉ pour ligne de commande)

### Étape 1 : Télécharger Maven

Allez sur : https://maven.apache.org/download.cgi

Téléchargez : **apache-maven-3.9.6-bin.zip**

### Étape 2 : Extraire

```powershell
# Extraire dans C:\
# Vous devriez avoir: C:\apache-maven-3.9.6\bin\mvn.cmd
```

### Étape 3 : Ajouter au PATH (IMPORTANT!)

**Sur Windows 10/11** :

1. **Appuyez sur** `Windows + X`
2. Sélectionnez **"System (Paramètres)"** ou **"Paramètres"**
3. **Allez à :** Paramètres avancés du système → Variables d'environnement
4. **Bouton :** "Variables d'environnement..."
5. **Sous "Variables système", cliquez sur "Path"** → **Éditer**
6. **Cliquez sur "Nouveau"** et ajoutez : `C:\apache-maven-3.9.6\bin`
7. **OK, OK, OK**
8. **REDÉMARREZ PowerShell** (fermez et réouvrez)

### Étape 4 : Vérifier

```powershell
mvn -v
```

Vous devriez voir :
```
Apache Maven 3.9.6 (...)
Java version: 21.0.10
```

### Étape 5 : Lancer l'app

```powershell
cd C:\Users\ademc\OneDrive\Desktop\EcoTrackJAVA-main
mvn javafx:run
```

---

## ✅ Solution 3 : **Script de lancement rapide**

Nous avons créé `run.bat` pour vous. **Double-cliquez sur** `run.bat` depuis l'explorateur de fichiers.

```powershell
# Ou depuis PowerShell :
cd C:\Users\ademc\OneDrive\Desktop\EcoTrackJAVA-main
.\run.bat
```

---

## 🐛 Dépannage

### Problème : "mvn is not recognized" après installation

**Solution** :
```powershell
# PowerShell : Ajouter temporairement au PATH
$env:Path += ";C:\apache-maven-3.9.6\bin"
mvn -v
```

### Problème : "Java version 1.8 is not supported"

**Solution** : Vérifiez que JDK 21 est utilisé
```powershell
java -version
# Doit afficher: openjdk version "21.0.10"
```

Si non, ajoutez JDK 21 au PATH :
```powershell
$env:Path = "C:\Program Files\OpenJDK\jdk-21.0.10\bin;" + $env:Path
java -version
```

### Problème : Build échoue avec "Cannot find resource"

**Solution** : Compilez d'abord
```powershell
mvn clean compile
mvn javafx:run
```

---

## 📋 Checklist finale

- [ ] JDK 21 installé : `java -version`
- [ ] Maven installé : `mvn -v`
- [ ] MySQL en cours d'exécution
- [ ] Table `events` créée dans la BDD
- [ ] IntelliJ IDEA Ultimate (avec JavaFX configuré)
- [ ] Projet ouvert et indexé dans IntelliJ

---

## 🎯 **Recommandation finale**

**UTILISEZ INTELLIJ** ! C'est le plus simple :
1. File → Open → Sélectionnez le projet
2. Attendez l'indexation
3. Right-click sur MainFX.java → Run

---

**Besoin d'aide ?** Consultez `README_INTEGRATION.md` pour plus de détails.

