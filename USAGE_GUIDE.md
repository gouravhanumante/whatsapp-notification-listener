# Quick Start Guide

## Getting Started with WhatsApp Notification Listener

### Step 1: Install the App
Build and install the app on your Android device using Android Studio or the APK.

### Step 2: Enable Notification Access
1. Open the app
2. You'll see a status card showing "Inactive"
3. Tap the "Enable Notification Access" button
4. In the settings screen, find "Notification Listener"
5. Toggle the switch ON
6. Confirm when prompted
7. Return to the app

### Step 3: Start the Service
1. Once notification access is enabled, the status will change to "Active"
2. Tap "Start Monitoring Service"
3. You'll see a persistent notification indicating the service is running
4. The app is now monitoring all WhatsApp notifications!

### Step 4: View Logs
To see the captured notifications, use Android Studio's Logcat or ADB:

```bash
# View all WhatsApp notification logs
adb logcat -s WhatsAppListener

# Continuous monitoring
adb logcat | grep "WhatsApp Notification"

# Save logs to a file
adb logcat -s WhatsAppListener > whatsapp_logs.txt
```

## Understanding the Logs

Each WhatsApp notification will be logged with the following information:

```
=== WhatsApp Notification ===
Package: com.whatsapp
Title: John Doe
Text: Hey, how are you?
Big Text: Hey, how are you? Long message...
Sub Text: WhatsApp
Timestamp: 1729872000000
ID: 12345
===========================
```

## Features Breakdown

### 1. Real-Time Monitoring
- Captures notifications as soon as they arrive
- Works for both individual and group messages
- Supports WhatsApp and WhatsApp Business

### 2. Foreground Service
- Runs continuously in the background
- Shows a persistent notification
- Won't be killed by the system
- Minimal battery impact

### 3. Auto-Restart
- Automatically restarts after device reboot
- Recovers if the service is stopped
- Ensures 24/7 monitoring

### 4. Privacy-Focused
- All data stays on your device
- No internet connection required
- No data is sent to external servers
- You have full control

## Customization Options

### Option 1: Store Notifications in SharedPreferences
Add this to `WhatsAppNotificationListener.kt`:

```kotlin
private fun saveNotification(notification: WhatsAppNotification) {
    val prefs = getSharedPreferences("whatsapp_notifications", MODE_PRIVATE)
    val json = JSONObject()
    json.put("title", notification.title)
    json.put("text", notification.text)
    json.put("timestamp", notification.timestamp)
    
    val key = "notification_${System.currentTimeMillis()}"
    prefs.edit().putString(key, json.toString()).apply()
}
```

### Option 2: Save to File
```kotlin
private fun saveToFile(notification: WhatsAppNotification) {
    val file = File(getExternalFilesDir(null), "whatsapp_notifications.txt")
    file.appendText(notification.toLogString() + "\n")
}
```

### Option 3: Send to Server
```kotlin
private fun sendToServer(notification: WhatsAppNotification) {
    // Implement your API call here
    // Example: POST to your server endpoint
}
```

## Battery Optimization Settings

To ensure the app runs continuously, disable battery optimization:

### Samsung Devices
1. Settings → Apps → Notification Listener
2. Battery → Optimize battery usage
3. Select "All" and find the app
4. Toggle OFF

### Xiaomi/MIUI Devices
1. Settings → Apps → Manage apps
2. Find Notification Listener
3. Battery saver → No restrictions
4. Autostart → Enable

### Stock Android
1. Settings → Apps → Notification Listener
2. Battery → Battery optimization
3. Select "Not optimized" → All apps
4. Find the app and select "Don't optimize"

## Troubleshooting

### Problem: Notifications Not Being Captured
**Solution:**
1. Verify notification access is enabled
2. Check if WhatsApp is installed and active
3. Send a test message to yourself
4. Check Logcat for errors

### Problem: Service Stops After Screen Lock
**Solution:**
1. Disable battery optimization (see above)
2. Enable "Autostart" in your device settings
3. Add app to "Protected apps" list

### Problem: Missing Some Notifications
**Solution:**
1. WhatsApp notifications might be grouped
2. Check if WhatsApp has notification permission
3. Ensure WhatsApp is not in battery saver mode
4. Check if notification listener service is still connected

### Problem: High Battery Drain
**Solution:**
1. The foreground service is already optimized
2. Reduce logging frequency if you've added custom code
3. Check if other apps are causing issues
4. The battery impact should be minimal (<1-2% per day)

## Advanced Usage

### Filter Specific Contacts
Modify `handleWhatsAppNotification()` to filter by title:

```kotlin
if (title.contains("John Doe")) {
    // Only process notifications from John Doe
    Log.d(TAG, "Notification from John Doe: $text")
}
```

### Process Group Messages Differently
```kotlin
if (title.contains("(") || title.contains("participants")) {
    // This is likely a group message
    Log.d(TAG, "Group message: $text")
}
```

### Auto-Reply (Advanced)
⚠️ Note: Auto-reply requires additional permissions and may violate WhatsApp's Terms of Service

```kotlin
// This is for educational purposes only
// Implementing auto-reply requires notification action extraction
// and is not recommended due to WhatsApp's policies
```

## Data Privacy & Legal

**Important Considerations:**
- This app can access all WhatsApp notifications
- Use only for personal, legal purposes
- Do not share captured data without consent
- Ensure compliance with local privacy laws (GDPR, CCPA, etc.)
- Do not use for unauthorized monitoring
- Respect WhatsApp's Terms of Service

## Support

For issues or questions:
1. Check the logs using Logcat
2. Review the troubleshooting section
3. Verify all permissions are granted
4. Try reinstalling the app

## Next Steps

Now that your app is running, you can:
1. Monitor the logs to see captured notifications
2. Customize the notification handling logic
3. Add database storage for persistence
4. Implement notification statistics
5. Build a dashboard to view captured data
6. Add filters for specific contacts or keywords

Happy monitoring! 🎉

