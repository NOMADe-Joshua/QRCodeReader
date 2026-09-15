#!/usr/bin/env bash
# Builds the signed release APK and installs/updates it on a USB-connected
# phone (Entwickleroptionen + USB-Debugging muss auf dem Handy aktiv sein).
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

if [ -z "${JAVA_HOME:-}" ]; then
    export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
fi

./gradlew.bat installRelease --console=plain
