#!/bin/bash

# AndrOBD - Android Device Emulator Startup Script
# This script starts an Android emulator for development and debugging

set -e  # Exit on any error

echo "Starting Android Device Emulator for AndrOBD..."

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_HOME environment variable is not set."
    echo "Please set ANDROID_HOME to your Android SDK location."
    echo "Example: export ANDROID_HOME=/Users/username/Library/Android/sdk"
    exit 1
fi

# Check if emulator command exists
if [ ! -f "$ANDROID_HOME/emulator/emulator" ]; then
    echo "Error: Android emulator not found at $ANDROID_HOME/emulator/emulator"
    echo "Please ensure Android SDK is properly installed."
    exit 1
fi

# List available AVDs (Android Virtual Devices)
echo "Available Android Virtual Devices:"
"$ANDROID_HOME/emulator/emulator" -list-avds

echo ""
echo "Starting emulator..."

# Start emulator with common development settings
# You can modify the AVD name below to match your preferred device
AVD_NAME="Medium_Phone_API_36"  # Default AVD name

# Check if the specified AVD exists
if ! "$ANDROID_HOME/emulator/emulator" -list-avds | grep -q "$AVD_NAME"; then
    echo "Warning: AVD '$AVD_NAME' not found. Starting with first available AVD..."
    AVD_NAME=$("$ANDROID_HOME/emulator/emulator" -list-avds | head -n 1)
    if [ -z "$AVD_NAME" ]; then
        echo "Error: No AVDs found. Please create an AVD first using Android Studio or AVD Manager."
        exit 1
    fi
fi

echo "Starting AVD: $AVD_NAME"

# Start emulator with optimized settings for development
"$ANDROID_HOME/emulator/emulator" \
    -avd "$AVD_NAME" \
    -gpu host \
    -no-snapshot-load \
    -no-snapshot-save \
    -memory 2048 \
    -cores 2 \
    -skin 1080x2400 \
    -dpi 420 \
    -verbose

echo "Emulator started successfully!"
echo "You can now run the deployment script to install AndrOBD for debugging."
