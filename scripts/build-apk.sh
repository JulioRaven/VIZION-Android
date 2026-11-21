#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "== VIZION Android APK build =="

if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is missing. Install JDK 17."
  exit 1
fi
JAVA_VERSION="$(java -version 2>&1 | head -n 1)"
echo "Java: $JAVA_VERSION"

if ! command -v gradle >/dev/null 2>&1; then
  echo "ERROR: Gradle is missing. Install Gradle 8.13 or newer (compatible with AGP 8.13.2)."
  exit 1
fi
gradle --version

SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$SDK_ROOT" ]]; then
  echo "ERROR: Set ANDROID_SDK_ROOT or ANDROID_HOME to your Android SDK directory."
  exit 1
fi
export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_HOME="$SDK_ROOT"

SDKMANAGER=""
for candidate in \
  "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" \
  "$SDK_ROOT/cmdline-tools/bin/sdkmanager" \
  "$SDK_ROOT/tools/bin/sdkmanager"; do
  if [[ -x "$candidate" ]]; then SDKMANAGER="$candidate"; break; fi
done

if [[ -n "$SDKMANAGER" ]]; then
  echo "Accepting Android SDK licenses..."
  yes | "$SDKMANAGER" --licenses >/dev/null || true
  echo "Installing required Android SDK packages..."
  "$SDKMANAGER" "platforms;android-36" "build-tools;36.0.0" "platform-tools"
else
  echo "WARNING: sdkmanager not found; assuming Android 36 and build-tools 36.0.0 are already installed."
fi

echo "Building debug APK..."
gradle --no-daemon --stacktrace clean assembleDebug

APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -s "$APK" ]]; then
  echo "ERROR: Build finished but APK was not found at $APK"
  exit 1
fi

mkdir -p "$ROOT/dist"
cp "$APK" "$ROOT/dist/VIZION-debug.apk"

if command -v sha256sum >/dev/null 2>&1; then
  sha256sum "$ROOT/dist/VIZION-debug.apk" | tee "$ROOT/dist/VIZION-debug.apk.sha256"
fi

echo
echo "SUCCESS: $ROOT/dist/VIZION-debug.apk"
ls -lh "$ROOT/dist/VIZION-debug.apk"
