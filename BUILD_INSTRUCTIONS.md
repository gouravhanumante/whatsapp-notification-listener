# Build Instructions

## Prerequisites

### Required Software
- **Android Studio**: Arctic Fox (2020.3.1) or later
  - Download: https://developer.android.com/studio
- **JDK**: Java 11 or later
- **Android SDK**: API Level 24 (Android 7.0) minimum
- **Gradle**: 8.0+ (included with Android Studio)

### Recommended
- Physical Android device (Android 7.0+) for testing
- Android Emulator with Play Store (for testing without physical device)
- ADB (Android Debug Bridge) installed and configured

## Setup Steps

### 1. Clone/Open Project
```bash
# If cloning from repository
git clone <repository-url>
cd NotificationListener

# Or open the existing project in Android Studio
# File → Open → Select project directory
```

### 2. Sync Gradle
```bash
# Android Studio will automatically prompt to sync
# Or manually sync:
./gradlew --refresh-dependencies
```

### 3. Configure SDK
- Open **Tools → SDK Manager**
- Ensure Android SDK 24-36 are installed
- Verify Build Tools 30.0.3+ is installed

### 4. Update Dependencies (if needed)
```kotlin
// app/build.gradle.kts already configured with:
// - Kotlin
// - Jetpack Compose
// - Material 3
// - Lifecycle components
```

## Build Variants

### Debug Build (Development)
```bash
# Command line
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

**Features:**
- Debugging enabled
- Logging enabled
- No code minification
- Faster build time

### Release Build (Production)
```bash
# Command line
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

**Features:**
- Optimized code
- ProGuard/R8 enabled (if configured)
- Signed APK required for distribution

## Signing the APK (Release Build)

### Method 1: Using Android Studio
1. **Build → Generate Signed Bundle/APK**
2. Select **APK**
3. Choose or create keystore
4. Enter keystore credentials
5. Select **release** build variant
6. Click **Finish**

### Method 2: Using Command Line
```bash
# Create keystore (first time only)
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias notification-listener

# Build and sign
./gradlew assembleRelease

# Sign manually if needed
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore release-key.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  notification-listener

# Verify signature
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release-unsigned.apk
```

## Installation

### Install on Connected Device
```bash
# Check connected devices
adb devices

# Install debug build
adb install app/build/outputs/apk/debug/app-debug.apk

# Install release build
adb install app/build/outputs/apk/release/app-release.apk

# Reinstall (if already installed)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Install via Android Studio
1. Connect device via USB
2. Enable USB Debugging on device
3. Click **Run** (▶) button
4. Select target device
5. App will build and install automatically

## Configuration

### Change Package Name
1. Open `app/build.gradle.kts`
2. Modify `applicationId`:
```kotlin
defaultConfig {
    applicationId = "com.yourcompany.yourapp"
    // ...
}
```
3. Refactor package name in code:
   - Right-click on package → Refactor → Rename

### Change App Name
1. Open `app/src/main/res/values/strings.xml`
2. Modify app_name:
```xml
<string name="app_name">Your App Name</string>
```

### Change Version
1. Open `app/build.gradle.kts`
2. Update version:
```kotlin
defaultConfig {
    versionCode = 2
    versionName = "1.1.0"
    // ...
}
```

### Customize Colors/Theme
1. Open `app/src/main/java/com/gomad/notificationlistener/ui/theme/Color.kt`
2. Modify colors:
```kotlin
val Primary = Color(0xFF6200EE)
val Secondary = Color(0xFF03DAC6)
```

## Testing

### Run Unit Tests
```bash
./gradlew test

# Run specific test
./gradlew test --tests WhatsAppNotificationTest
```

### Run on Emulator
1. **Tools → AVD Manager**
2. Create new virtual device
3. Select system image (API 24+)
4. Start emulator
5. Run app

### Run on Physical Device
1. Enable **Developer Options** on device:
   - Settings → About Phone → Tap Build Number 7 times
2. Enable **USB Debugging**:
   - Settings → Developer Options → USB Debugging
3. Connect device via USB
4. Accept debugging prompt on device
5. Run app from Android Studio

## Troubleshooting

### Build Failures

#### Issue: "SDK location not found"
**Solution:**
```bash
# Create local.properties file
echo "sdk.dir=/path/to/Android/sdk" > local.properties

# Or on Mac/Linux
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

#### Issue: "Gradle sync failed"
**Solution:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

#### Issue: "Manifest merger failed"
**Solution:**
- Check AndroidManifest.xml for errors
- Verify all permissions are correctly formatted
- Check for duplicate declarations

### Installation Failures

#### Issue: "INSTALL_FAILED_UPDATE_INCOMPATIBLE"
**Solution:**
```bash
# Uninstall existing version
adb uninstall com.gomad.notificationlistener

# Install new version
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Issue: "INSTALL_FAILED_INSUFFICIENT_STORAGE"
**Solution:**
- Free up space on device
- Uninstall unused apps

#### Issue: "INSTALL_FAILED_VERIFICATION_FAILURE"
**Solution:**
- Disable Play Protect
- Settings → Google → Security → Play Protect → Disable

### Runtime Issues

#### Issue: "App crashes on startup"
**Solution:**
```bash
# Check logs
adb logcat | grep AndroidRuntime

# Clear app data
adb shell pm clear com.gomad.notificationlistener
```

#### Issue: "Notification access not working"
**Solution:**
1. Verify permission in manifest
2. Check Settings → Apps → Special Access → Notification Access
3. Disable and re-enable permission

## Optimization

### Enable ProGuard/R8 (Release)
1. Open `app/build.gradle.kts`
2. Enable minification:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### Reduce APK Size
1. Enable code shrinking (above)
2. Remove unused resources:
```kotlin
buildTypes {
    release {
        isShrinkResources = true
        isMinifyEnabled = true
    }
}
```

### Enable Multidex (if needed)
```kotlin
defaultConfig {
    multiDexEnabled = true
}

dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
}
```

## CI/CD Setup (Optional)

### GitHub Actions Example
```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## Distribution

### Google Play Store
1. Build signed release APK
2. Create Google Play Console account
3. Create new app
4. Upload APK
5. Fill in store listing
6. Submit for review

### Alternative Distribution
- **Firebase App Distribution**: For beta testing
- **APKPure**: Alternative app store
- **Direct APK**: Share APK file directly
- **F-Droid**: Open source app repository

## Version Management

### Semantic Versioning
```
MAJOR.MINOR.PATCH

Example: 1.2.3
- MAJOR: Breaking changes
- MINOR: New features
- PATCH: Bug fixes
```

### Update Version
```kotlin
// app/build.gradle.kts
versionCode = 2          // Increment for each release
versionName = "1.1.0"    // Semantic version
```

## Build Tips

### Speed Up Builds
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true
android.enableJetifier=true
android.useAndroidX=true
```

### Clean Build (if issues)
```bash
./gradlew clean
./gradlew build --no-build-cache
```

### Build All Variants
```bash
./gradlew assemble
```

## Support

### Logs
```bash
# View all logs
adb logcat

# Filter by app
adb logcat | grep com.gomad.notificationlistener

# Filter by tag
adb logcat -s WhatsAppListener
```

### Debugging
1. Set breakpoints in Android Studio
2. Click **Debug** (🐞) button
3. App will pause at breakpoints
4. Inspect variables and step through code

### Performance Profiling
1. **View → Tool Windows → Profiler**
2. Select running app
3. Monitor CPU, Memory, Network usage

## Checklist Before Release

- [ ] Update version code and name
- [ ] Test on multiple devices
- [ ] Test all features
- [ ] Check logs for errors
- [ ] Verify permissions
- [ ] Test on different Android versions
- [ ] Optimize images and resources
- [ ] Enable ProGuard/R8
- [ ] Sign APK with release keystore
- [ ] Test signed APK
- [ ] Update CHANGELOG.md
- [ ] Create release notes
- [ ] Tag version in git
- [ ] Upload to distribution platform

## Additional Resources

- [Android Developer Documentation](https://developer.android.com)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io)
- [Kotlin Documentation](https://kotlinlang.org/docs)
- [Gradle Build Tool](https://gradle.org)

## Getting Help

If you encounter issues:
1. Check this document
2. Review Android Studio build output
3. Check Logcat for errors
4. Search Stack Overflow
5. Review Android Developer documentation
6. Check GitHub Issues (if applicable)

---

**Happy Building! 🚀**

