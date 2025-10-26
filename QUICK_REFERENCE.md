# Quick Reference Guide

## 🚀 Quick Start Commands

### Build & Install
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or reinstall if already installed
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Build and install in one step (via Android Studio)
# Click Run (▶) button
```

### View Logs
```bash
# View WhatsApp notification logs
adb logcat -s WhatsAppListener

# View service logs
adb logcat -s MonitorService

# View boot receiver logs
adb logcat -s BootReceiver

# View all app logs
adb logcat | grep com.gomad.notificationlistener

# Clear logs before testing
adb logcat -c
```

### Device Management
```bash
# Check connected devices
adb devices

# Check if app is installed
adb shell pm list packages | grep notification

# Uninstall app
adb uninstall com.gomad.notificationlistener

# Clear app data (reset)
adb shell pm clear com.gomad.notificationlistener

# Force stop app
adb shell am force-stop com.gomad.notificationlistener
```

---

## 🔍 Debugging Commands

### Check Service Status
```bash
# Check if service is running
adb shell dumpsys activity services | grep NotificationMonitor

# Check notification listener status
adb shell dumpsys notification_listener

# Check if app is in protected apps (some devices)
adb shell dumpsys deviceidle whitelist
```

### Permission Checking
```bash
# List all app permissions
adb shell dumpsys package com.gomad.notificationlistener | grep permission

# Check notification access
adb shell cmd notification allow_listener com.gomad.notificationlistener/.WhatsAppNotificationListener
```

### Testing
```bash
# Simulate device boot (requires root)
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED

# Send test notification (won't work for WhatsApp, but good for testing)
adb shell cmd notification post -S bigtext -t 'Test Title' 'Tag' 'Test message'

# Check battery optimization status
adb shell dumpsys deviceidle whitelist | grep notification
```

---

## 📱 Common Use Cases

### First Time Setup
```bash
# 1. Build and install
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# 2. Open app on device
adb shell am start -n com.gomad.notificationlistener/.MainActivity

# 3. Enable notification access (manually in settings)

# 4. Start monitoring logs
adb logcat -s WhatsAppListener

# 5. Send test WhatsApp message
```

### Daily Usage
```bash
# Check if monitoring
adb logcat -s WhatsAppListener | grep "WhatsApp Notification"

# View recent notifications only
adb logcat -t 50 -s WhatsAppListener

# Save logs to file
adb logcat -s WhatsAppListener > whatsapp_logs.txt

# Monitor in real-time with timestamps
adb logcat -v time -s WhatsAppListener
```

### Troubleshooting
```bash
# App not working? Check if service is running
adb shell dumpsys activity services | grep Notification

# Check for crashes
adb logcat | grep AndroidRuntime

# Check permission status
adb shell dumpsys notification_listener | grep notification

# Reinstall fresh
adb uninstall com.gomad.notificationlistener
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛠️ Development Commands

### Clean & Rebuild
```bash
# Clean build
./gradlew clean

# Clean and rebuild
./gradlew clean assembleDebug

# Build all variants
./gradlew assemble

# Refresh dependencies
./gradlew --refresh-dependencies
```

### Release Build
```bash
# Build release APK (unsigned)
./gradlew assembleRelease

# Build release bundle for Play Store
./gradlew bundleRelease
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run with logs
./gradlew test --info

# Test specific class
./gradlew test --tests NotificationHelperTest
```

---

## 📊 Monitoring Commands

### Real-Time Monitoring
```bash
# Monitor with all details
adb logcat -v threadtime -s WhatsAppListener

# Monitor with colors (Linux/Mac)
adb logcat -s WhatsAppListener | grep --color=auto "WhatsApp"

# Monitor multiple tags
adb logcat -s WhatsAppListener:D MonitorService:D BootReceiver:D

# Follow logs in real-time
adb logcat -s WhatsAppListener -f /tmp/whatsapp.log
```

### Export Logs
```bash
# Export last 1000 lines
adb logcat -t 1000 -s WhatsAppListener > notifications.log

# Export with date
adb logcat -v time -t 1000 -s WhatsAppListener > "notifications_$(date +%Y%m%d).log"

# Export all app logs
adb logcat -d > full_log.txt
```

---

## 🔧 Configuration Commands

### Battery Optimization (ADB)
```bash
# Disable battery optimization (requires special permission)
adb shell dumpsys deviceidle whitelist +com.gomad.notificationlistener

# Check battery stats
adb shell dumpsys batterystats | grep notification
```

### Grant Permissions (Testing Only)
```bash
# Grant POST_NOTIFICATIONS (Android 13+)
adb shell pm grant com.gomad.notificationlistener android.permission.POST_NOTIFICATIONS

# Note: Notification Listener access must be granted manually
```

---

## 📱 App Control Commands

### Launch App
```bash
# Start main activity
adb shell am start -n com.gomad.notificationlistener/.MainActivity

# Start with flags
adb shell am start -W -n com.gomad.notificationlistener/.MainActivity
```

### Service Control
```bash
# Start service manually (for testing)
adb shell am startservice com.gomad.notificationlistener/.NotificationMonitorService

# Stop service
adb shell am stopservice com.gomad.notificationlistener/.NotificationMonitorService

# Send broadcast (simulate boot)
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p com.gomad.notificationlistener
```

---

## 🎯 Quick Testing Workflow

### Complete Test Cycle
```bash
# 1. Clean build
./gradlew clean assembleDebug

# 2. Uninstall old version
adb uninstall com.gomad.notificationlistener

# 3. Install new version
adb install app/build/outputs/apk/debug/app-debug.apk

# 4. Clear logs
adb logcat -c

# 5. Start app
adb shell am start -n com.gomad.notificationlistener/.MainActivity

# 6. Monitor logs
adb logcat -s WhatsAppListener

# 7. Send test WhatsApp message (manually)

# 8. Verify in logs
```

---

## 📝 Log Formats

### Standard Output
```
D/WhatsAppListener: === WhatsApp Notification ===
D/WhatsAppListener: Package: com.whatsapp
D/WhatsAppListener: Title: John Doe
D/WhatsAppListener: Text: Hey, how are you?
D/WhatsAppListener: Timestamp: 1729872000000
D/WhatsAppListener: ID: 12345
D/WhatsAppListener: ===========================
```

### Filtered Output
```bash
# Only show notification details
adb logcat -s WhatsAppListener | grep "Title\|Text"

# Only show timestamps
adb logcat -s WhatsAppListener | grep "Timestamp"

# Count notifications
adb logcat -s WhatsAppListener | grep "WhatsApp Notification" | wc -l
```

---

## 🚨 Emergency Commands

### App Misbehaving
```bash
# Force stop immediately
adb shell am force-stop com.gomad.notificationlistener

# Clear all data
adb shell pm clear com.gomad.notificationlistener

# Reinstall
adb uninstall com.gomad.notificationlistener
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Device Issues
```bash
# Restart ADB
adb kill-server
adb start-server

# Reconnect device
adb reconnect

# Check device status
adb shell getprop ro.build.version.release
```

---

## 💡 Pro Tips

### Efficiency Commands
```bash
# Alias for quick log viewing (add to ~/.bashrc or ~/.zshrc)
alias wlog='adb logcat -s WhatsAppListener'

# Watch logs continuously
watch -n 1 'adb logcat -t 10 -s WhatsAppListener'

# Background monitoring
nohup adb logcat -s WhatsAppListener > whatsapp.log &

# Real-time notification counter
adb logcat -s WhatsAppListener | grep -c "WhatsApp Notification"
```

### Multi-Device Testing
```bash
# List devices with serial numbers
adb devices -l

# Target specific device
adb -s DEVICE_SERIAL install app-debug.apk

# Run command on all devices
for device in $(adb devices | grep -v "List" | awk '{print $1}'); do
    adb -s $device install app-debug.apk
done
```

---

## 📚 Additional Resources

### Android Studio Shortcuts
- **Build**: Cmd/Ctrl + F9
- **Run**: Cmd/Ctrl + R
- **Debug**: Cmd/Ctrl + D
- **Logcat**: Cmd/Ctrl + 6

### Useful Logcat Filters
```
# In Android Studio Logcat:
tag:WhatsAppListener
package:com.gomad.notificationlistener
level:debug
```

---

## ✅ Quick Verification

### Is Everything Working?
```bash
# Run all checks
echo "1. Checking device connection..."
adb devices

echo "2. Checking if app is installed..."
adb shell pm list packages | grep notificationlistener

echo "3. Checking service status..."
adb shell dumpsys activity services | grep NotificationMonitor

echo "4. Checking recent logs..."
adb logcat -t 20 -s WhatsAppListener

echo "✓ Verification complete!"
```

---

**Last Updated**: October 25, 2025  
**For**: WhatsApp Notification Listener v1.0.0

📖 For more details, see:
- README.md - Project overview
- USAGE_GUIDE.md - Detailed usage
- BUILD_INSTRUCTIONS.md - Build details
- VERIFICATION_CHECKLIST.md - Testing guide

