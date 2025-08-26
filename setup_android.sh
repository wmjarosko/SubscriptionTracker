#!/bin/bash
# Corrected Android SDK Setup Script

set -e # Exit immediately if a command fails

# --- Setup Paths ---
# Use ANDROID_HOME as it is the standard environment variable
export ANDROID_HOME="$HOME/Android/sdk"
TOOLS_ZIP_PATH="/tmp/tools.zip"
TOOLS_EXTRACT_PATH="/tmp/tools"

# --- Download and Extract ---
echo "Downloading command-line tools..."
# Using curl for better compatibility (installed by default on macOS and most Linux distros)
curl -L "https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip" -o "$TOOLS_ZIP_PATH"

echo "Setting up SDK directory structure..."
unzip -q "$TOOLS_ZIP_PATH" -d "$TOOLS_EXTRACT_PATH"
mkdir -p "$ANDROID_HOME/cmdline-tools/latest"
# The unzipped folder contains a 'cmdline-tools' directory inside it
mv "$TOOLS_EXTRACT_PATH/cmdline-tools/"* "$ANDROID_HOME/cmdline-tools/latest/"

# --- Cleanup Download Files ---
rm -rf "$TOOLS_EXTRACT_PATH"
rm "$TOOLS_ZIP_PATH"

# --- Set Environment Variables for Current Session ---
# This ensures the sdkmanager command can be found immediately
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
export PATH="$PATH:$ANDROID_HOME/platform-tools"

# --- IMPORTANT: Accept Licenses FIRST ---
echo "Accepting SDK licenses..."
yes | sdkmanager --licenses > /dev/null

# --- Install SDK Packages ---
echo "Installing platform-tools, build-tools, and platform..."
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.1"

echo "✅ Environment setup complete for this session."
echo "You should add the PATH and ANDROID_HOME exports to your .bashrc or .zshrc file to make them permanent."
