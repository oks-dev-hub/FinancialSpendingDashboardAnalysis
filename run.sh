#!/usr/bin/env bash
set -euo pipefail

cleanup() {
    echo "Stopping emulator..."
    adb emu kill || true
}

trap cleanup EXIT

echo "Starting adb..."
adb start-server

echo "Starting emulator..."

if ! emulator -list-avds | grep -q Pixel_API_34; then
    echo "Missing Pixel_API_34 emulator"
    exit 1
fi

emulator \
    -avd Pixel_API_34 \
    -no-window \
    -no-audio \
    -no-boot-anim \
    -no-snapshot \
    -no-cache \
    -gpu swiftshader_indirect &

echo "Waiting for Android boot..."

timeout=300
elapsed=0

adb wait-for-device

until [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1" ];
do
    sleep 5
    elapsed=$((elapsed + 5))

    if [ "$elapsed" -ge "$timeout" ]; then
        echo "Emulator failed to boot"
        exit 1
    fi
done

until [ "$(adb shell getprop init.svc.bootanim | tr -d '\r')" = "stopped" ];
do
    sleep 2
done

echo "Waiting for package manager..."

until adb shell pm list packages >/dev/null 2>&1;
do
    sleep 2
done

adb shell input keyevent 82

echo "Building APK..."

./gradlew assembleDebug --no-daemon

echo "Installing APK..."

APK=$(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)

adb install -r "$APK"

echo "Launching application..."

adb shell monkey \
    -p com.example.financialspendingdashboardanalysis \
    1

echo "Done"