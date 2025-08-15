#!/bin/bash

# AndrOBD - Debug Deployment Script
# This script builds and deploys the app for debugging

set -e  # Exit on any error

echo "Building and Deploying AndrOBD for Debug..."

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_HOME environment variable is not set."
    echo "Please set ANDROID_HOME to your Android SDK location."
    echo "Example: export ANDROID_HOME=/Users/username/Library/Android/sdk"
    exit 1
fi

# Check if adb command exists
if [ ! -f "$ANDROID_HOME/platform-tools/adb" ]; then
    echo "Error: ADB not found at $ANDROID_HOME/platform-tools/adb"
    echo "Please ensure Android SDK Platform Tools are installed."
    exit 1
fi

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "Error: gradlew not found in current directory."
    echo "Please run this script from the AndrOBD project root."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "Checking for connected devices/emulators..."
"$ANDROID_HOME/platform-tools/adb" devices

# Wait for device to be ready
echo "Waiting for device to be ready..."
"$ANDROID_HOME/platform-tools/adb" wait-for-device

# Check if any device is connected and ready
if ! "$ANDROID_HOME/platform-tools/adb" devices | grep -q "device$"; then
    echo "Error: No Android device or emulator is ready."
    echo "Please start an emulator first using start_emulator.sh or connect a physical device."
    exit 1
fi

echo ""
echo "Device is ready. Building debug APK..."

# Clean and build debug APK
./gradlew clean
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "Error: Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "Build successful! Installing APK..."

# Install the debug APK
./gradlew installDebug

if [ $? -ne 0 ]; then
    echo "Error: Installation failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "Deployment successful! Starting AndrOBD app..."

# Wait a moment for installation to complete
sleep 2

# Start the app
"$ANDROID_HOME/platform-tools/adb" shell am start -n com.fr3ts0n.ecu.gui.androbd/.MainActivity

if [ $? -eq 0 ]; then
    echo "AndrOBD app started successfully!"
    echo "You can now debug the application."
else
    echo "Warning: Could not start the app automatically."
    echo "You can start it manually from the device/emulator."
fi

echo ""
echo "Debug deployment completed!"
echo "Useful commands:"
echo "  - View logs: $ANDROID_HOME/platform-tools/adb logcat"
echo "  - Filter AndrOBD logs: $ANDROID_HOME/platform-tools/adb logcat | grep AndrOBD"
echo "  - Uninstall: $ANDROID_HOME/platform-tools/adb uninstall com.fr3ts0n.ecu.gui.androbd"
echo "  - Restart app: $ANDROID_HOME/platform-tools/adb shell am start -n com.fr3ts0n.ecu.gui.androbd/.MainActivity"
