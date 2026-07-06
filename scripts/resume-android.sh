#!/usr/bin/env bash
# Resume the Crucified Android dev/test loop: build, install, launch, tail logs.
#
# Usage:
#   scripts/resume-android.sh              # build, install, launch, tail logcat
#   scripts/resume-android.sh --no-build   # skip the Gradle build, just reinstall+launch
#   scripts/resume-android.sh --no-log     # don't tail logcat after launching
#   scripts/resume-android.sh -s <serial>  # target a specific device (default: only connected one)
#
# Env:
#   ANDROID_SERIAL  same as -s, if set

set -euo pipefail

PACKAGE_NAME="de.gamedevbaden.crucified"
ACTIVITY_NAME="de.gamedevbaden.crucified.android.MainActivity"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

DO_BUILD=1
DO_LOG=1
SERIAL="${ANDROID_SERIAL:-}"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --no-build) DO_BUILD=0; shift ;;
        --no-log) DO_LOG=0; shift ;;
        -s) SERIAL="$2"; shift 2 ;;
        -h|--help)
            sed -n '2,15p' "${BASH_SOURCE[0]}"
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            exit 1
            ;;
    esac
done

if [[ -z "$SERIAL" ]]; then
    mapfile -t DEVICES < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
    if [[ ${#DEVICES[@]} -eq 0 ]]; then
        echo "No adb devices/emulators connected. Plug in a device or start an AVD, then re-run." >&2
        exit 1
    elif [[ ${#DEVICES[@]} -gt 1 ]]; then
        echo "Multiple devices connected, pick one with -s <serial>:" >&2
        printf '  %s\n' "${DEVICES[@]}" >&2
        exit 1
    fi
    SERIAL="${DEVICES[0]}"
fi
echo "==> Target device: $SERIAL"

if [[ "$DO_BUILD" -eq 1 ]]; then
    echo "==> Building debug APK"
    ./gradlew assembleDebug --console=plain
fi

APK_PATH="$(find build/outputs/apk/debug -iname '*.apk' | head -1)"
if [[ -z "$APK_PATH" ]]; then
    echo "No debug APK found under build/outputs/apk/debug. Run without --no-build first." >&2
    exit 1
fi
echo "==> Installing $APK_PATH"
adb -s "$SERIAL" install -r "$APK_PATH"

echo "==> Launching $PACKAGE_NAME/$ACTIVITY_NAME"
adb -s "$SERIAL" logcat -c
adb -s "$SERIAL" shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME"

sleep 2
PID="$(adb -s "$SERIAL" shell pidof "$PACKAGE_NAME" || true)"
if [[ -z "$PID" ]]; then
    echo "!! App did not stay running (no pid found). Check logcat for a crash." >&2
else
    echo "==> Running as pid $PID"
fi

if [[ "$DO_LOG" -eq 1 ]]; then
    echo "==> Tailing logcat for pid ${PID:-<unknown>} (Ctrl-C to stop)"
    if [[ -n "$PID" ]]; then
        exec adb -s "$SERIAL" logcat --pid="$PID"
    else
        exec adb -s "$SERIAL" logcat
    fi
fi
