# VIZION — instructions de compilation APK pour l'agent

## Objectif
Produire une APK Android installable à partir du projet existant. Il ne faut pas reconstruire l'application ni modifier ses fonctionnalités pendant cette étape.

## Prérequis
- Linux x86_64
- JDK 17
- Gradle 8.13
- Android SDK avec Android Platform 36 et Build Tools 36.0.0
- Accès réseau pour télécharger les dépendances Gradle/Android lors du premier build

Variables d'environnement recommandées :
```bash
export JAVA_HOME=/chemin/vers/jdk-17
export ANDROID_SDK_ROOT=/chemin/vers/android-sdk
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
```

## Compilation en une commande
Depuis la racine du dépôt :
```bash
bash scripts/build-apk.sh
```

Le script vérifie Java, Gradle et le SDK Android, installe les composants SDK requis si `sdkmanager` est disponible, lance `clean assembleDebug`, vérifie que l'APK existe et la copie dans `dist/`.

## Fichier attendu
```
dist/VIZION-debug.apk
```

Le script produit également `dist/VIZION-debug.apk.sha256` lorsque `sha256sum` est disponible.

## Vérifications après compilation
```bash
test -s dist/VIZION-debug.apk
file dist/VIZION-debug.apk
sha256sum -c dist/VIZION-debug.apk.sha256
```

Si Android Build Tools sont installés, vérifier aussi le package :
```bash
"$ANDROID_SDK_ROOT/build-tools/36.0.0/aapt" dump badging dist/VIZION-debug.apk | head
```

## Installation sur un appareil Android
Transférer `dist/VIZION-debug.apk` sur le téléphone ou la tablette, puis ouvrir le fichier et autoriser l'installation depuis cette source si Android le demande.

## Important
- Il s'agit d'une APK **debug**, signée automatiquement par la clé debug créée par Android Gradle Plugin. Elle convient aux tests et à l'installation manuelle, pas à une publication Play Store.
- Ne pas inventer de succès : fournir le journal Gradle complet si la compilation échoue.
- Ne pas intégrer de sources de streaming non autorisées, de contournement DRM, d'abonnements ou de paywalls.
- L'application actuelle utilise une clé API TMDB saisie par l'utilisateur dans l'application. La compilation ne crée pas de clé API.
- L'APK doit être testée sur au moins un téléphone Android et, si possible, une tablette avant livraison.
