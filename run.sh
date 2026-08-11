#!/usr/bin/env bash
set -euo pipefail

AVD_NAME="Pixel_API_34"
PACKAGE_NAME="com.example.financialspendingdashboardanalysis"

cleanup() {
    echo "Stopping emulator..."
    adb emu kill >/dev/null 2>&1 || true
}

trap cleanup EXIT

echo "======================================"
echo " Financial Dashboard"
echo "======================================"

echo "Starting ADB..."
adb start-server

echo "Checking emulator..."

if ! emulator -list-avds | grep -Fxq "$AVD_NAME"; then
    echo "Missing $AVD_NAME emulator"
    exit 1
fi

echo "Starting emulator..."

emulator \
    -avd "$AVD_NAME" \
    -no-window \
    -no-audio \
    -no-boot-anim \
    -no-snapshot \
    -gpu swiftshader_indirect &

echo "Waiting for Android boot..."

timeout=300
elapsed=0

adb wait-for-device

until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    sleep 5
    elapsed=$((elapsed + 5))

    if [ "$elapsed" -ge "$timeout" ]; then
        echo "Emulator failed to boot"
        exit 1
    fi

    echo "Still waiting... ${elapsed}s"
done

echo "Android boot completed."

until [ "$(adb shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r')" = "stopped" ]; do
    sleep 2
done

echo "Waiting for package manager..."

until adb shell pm list packages >/dev/null 2>&1; do
    sleep 2
done

echo "Android is ready."

adb shell input keyevent 82 || true

echo "Building APK..."

./gradlew assembleDebug --no-daemon

APK="app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK" ]; then
    echo "APK not found: $APK"
    exit 1
fi

echo "Installing APK..."

adb install -r "$APK"

echo "Launching application..."

adb shell monkey \
    -p "$PACKAGE_NAME" \
    -c android.intent.category.LAUNCHER \
    1

echo ""
echo "======================================"
echo " Financial Dashboard is running!"
echo "======================================"
echo ""

# Keep container running while emulator is alive
wait