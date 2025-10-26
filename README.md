# WhatsApp Notification Listener

An Android app that runs 24/7 and monitors all WhatsApp notifications on your device.

## Features

- 🔔 **Real-time Notification Monitoring**: Listens to all WhatsApp and WhatsApp Business notifications
- 🚀 **24/7 Background Service**: Runs continuously using a foreground service
- 🔄 **Auto-restart**: Automatically restarts after device reboot
- 📱 **Modern UI**: Beautiful Material Design 3 interface built with Jetpack Compose
- 📊 **Detailed Logging**: Logs all notification details including title, text, timestamp, and more

## How It Works

The app consists of several key components:

1. **WhatsAppNotificationListener**: A `NotificationListenerService` that intercepts WhatsApp notifications
2. **NotificationMonitorService**: A foreground service that keeps the app running 24/7
3. **BootReceiver**: Restarts the service after device reboot
4. **MainActivity**: Provides a user-friendly interface for setup and management

## Setup Instructions

1. **Install the app** on your Android device
2. **Open the app** and tap "Enable Notification Access"
3. **Find "Notification Listener"** in the settings list
4. **Toggle the switch** to enable notification access
5. **Return to the app** and tap "Start Monitoring Service"
6. The app will now run in the background and monitor all WhatsApp notifications

## Permissions Required

- `FOREGROUND_SERVICE`: To run the monitoring service continuously
- `POST_NOTIFICATIONS`: To display the foreground service notification (Android 13+)
- `RECEIVE_BOOT_COMPLETED`: To restart the service after device reboot
- `BIND_NOTIFICATION_LISTENER_SERVICE`: To access notifications from other apps

## Technical Details

### Supported WhatsApp Versions
- WhatsApp (com.whatsapp)
- WhatsApp Business (com.whatsapp.w4b)

### Android Version Support
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36 (Latest)

### Notification Data Captured
- Title
- Text content
- Big text (for long messages)
- Sub text
- Timestamp
- Notification ID
- Package name

## Viewing Logs

All WhatsApp notifications are logged to Android Logcat with the tag `WhatsAppListener`. To view logs:

```bash
# Using ADB
adb logcat -s WhatsAppListener

# Filter for specific information
adb logcat | grep "WhatsApp Notification"
```

## Customization

You can customize the notification handling in `WhatsAppNotificationListener.kt`:

```kotlin
private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // Add your custom logic here:
    // - Store notifications in a database
    // - Send to a server
    // - Process message content
    // - Trigger automated responses
    // - etc.
}
```

## Important Notes

⚠️ **Privacy & Security**
- This app has access to all WhatsApp notifications
- Use responsibly and ensure compliance with privacy laws
- Never share sensitive notification data without user consent

⚠️ **Battery Usage**
- The foreground service may increase battery consumption
- The service is optimized to minimize battery drain

⚠️ **Service Reliability**
- The app uses `START_STICKY` to automatically restart if killed
- The foreground notification prevents the system from killing the service
- Boot receiver ensures the service starts after device reboot

## Troubleshooting

### Service Not Running
1. Check if notification access is enabled
2. Restart the app
3. Check if the foreground notification is visible
4. Check device battery optimization settings

### Not Receiving Notifications
1. Ensure WhatsApp is installed
2. Verify notification access permission is granted
3. Check Logcat for error messages
4. Try disabling and re-enabling notification access

### Service Stops After Some Time
1. Disable battery optimization for this app
2. Check if your device has aggressive power management
3. Add the app to the device's "protected apps" list (varies by manufacturer)

## Building the Project

```bash
# Clone the repository
git clone <repository-url>

# Open in Android Studio
# Build and run on your device or emulator

# Or build from command line
./gradlew assembleDebug
```

## License

This project is for educational purposes. Ensure you comply with all applicable laws and regulations when using notification listener services.

## Disclaimer

This app is designed for legitimate use cases such as notification logging, automation, and productivity enhancement. Users are responsible for ensuring their use complies with all applicable laws and WhatsApp's Terms of Service.

