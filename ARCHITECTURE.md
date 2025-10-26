# Project Architecture

## Overview
WhatsApp Notification Listener is a 24/7 Android monitoring app that captures all WhatsApp notifications in real-time.

## Architecture Components

### 1. Services

#### WhatsAppNotificationListener (NotificationListenerService)
- **Purpose**: Intercepts all system notifications
- **Filtering**: Only processes WhatsApp and WhatsApp Business notifications
- **Data Extraction**: Extracts title, text, big text, sub text, timestamp, and ID
- **Lifecycle**: Managed by Android system, auto-rebinds on disconnect
- **Key Methods**:
  - `onNotificationPosted()`: Called when new notification arrives
  - `onNotificationRemoved()`: Called when notification is dismissed
  - `handleWhatsAppNotification()`: Processes WhatsApp-specific notifications

#### NotificationMonitorService (Foreground Service)
- **Purpose**: Keeps the app running 24/7
- **Type**: Foreground service with special use case
- **Notification**: Shows persistent notification in status bar
- **Restart**: Uses `START_STICKY` to auto-restart if killed
- **Lifecycle**: Starts when notification access is granted

### 2. Broadcast Receivers

#### BootReceiver
- **Purpose**: Restarts monitoring after device reboot
- **Triggers**: `BOOT_COMPLETED` and `QUICKBOOT_POWERON`
- **Action**: Starts NotificationMonitorService automatically

### 3. UI Components

#### MainActivity (Jetpack Compose)
- **Design**: Material Design 3 with modern UI
- **Features**:
  - Real-time status indicator (Active/Inactive)
  - Setup instructions
  - Permission handling
  - Service control buttons
- **Permissions**: Requests POST_NOTIFICATIONS for Android 13+

### 4. Data Models

#### WhatsAppNotification (Data Class)
- **Fields**:
  - `id`: Notification identifier
  - `packageName`: WhatsApp or WhatsApp Business
  - `title`: Sender name or group name
  - `text`: Message preview
  - `bigText`: Full message content (if available)
  - `subText`: Additional context
  - `timestamp`: When notification was posted
  - `capturedAt`: When app captured the notification
- **Methods**:
  - `toLogString()`: Formats notification for logging

### 5. Utilities

#### NotificationHelper (Object)
- **Purpose**: Centralized permission and service management
- **Methods**:
  - `isNotificationAccessEnabled()`: Check if notification access is granted
  - `openNotificationAccessSettings()`: Navigate to system settings
  - `isNotificationPermissionGranted()`: Check Android 13+ notification permission
  - `startMonitorService()`: Start the foreground service
  - `stopMonitorService()`: Stop the foreground service

## Data Flow

```
WhatsApp App
    ↓
System Notification
    ↓
WhatsAppNotificationListener.onNotificationPosted()
    ↓
Filter: Is it WhatsApp?
    ↓ (Yes)
handleWhatsAppNotification()
    ↓
Extract notification data
    ↓
Create WhatsAppNotification object
    ↓
Log to Logcat
    ↓
[Your Custom Logic]
```

## Service Lifecycle

```
Device Boot
    ↓
BootReceiver triggered
    ↓
Start NotificationMonitorService
    ↓
Service creates foreground notification
    ↓
Service starts WhatsAppNotificationListener (if permission granted)
    ↓
Listener connects to notification system
    ↓
Monitor notifications 24/7
    ↓
If killed: Auto-restart (START_STICKY)
    ↓
If disconnected: Request rebind
```

## Permission Flow

```
App Launch
    ↓
Check Notification Access
    ↓
Not Granted? → Show Enable Button → Open Settings → User Grants → Return to App
    ↓
Granted? → Check POST_NOTIFICATIONS (Android 13+)
    ↓
Not Granted? → Request Permission → User Grants
    ↓
Granted? → Start Monitoring Service → Show Active Status
```

## File Structure

```
app/src/main/
├── AndroidManifest.xml                    # Permissions and component declarations
├── java/com/gomad/notificationlistener/
│   ├── MainActivity.kt                    # Main UI with Compose
│   ├── WhatsAppNotificationListener.kt    # Notification interceptor
│   ├── NotificationMonitorService.kt      # 24/7 foreground service
│   ├── BootReceiver.kt                    # Auto-start on boot
│   ├── NotificationHelper.kt              # Permission utilities
│   ├── WhatsAppNotification.kt            # Data model
│   └── ui/theme/                          # Material Design theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── res/
│   ├── values/
│   │   ├── colors.xml
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── ... (other resources)
└── ...
```

## Key Technologies

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Service-oriented
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Design**: Material Design 3

## Permissions Explained

### FOREGROUND_SERVICE
- Required to run NotificationMonitorService
- Allows app to continue running in background
- Requires showing a persistent notification

### FOREGROUND_SERVICE_SPECIAL_USE
- Android 14+ requirement for special foreground services
- Notification monitoring is considered a special use case

### POST_NOTIFICATIONS (Android 13+)
- Required to show foreground service notification
- Requested at runtime

### RECEIVE_BOOT_COMPLETED
- Allows BootReceiver to start service on boot
- Ensures monitoring continues after device restart

### BIND_NOTIFICATION_LISTENER_SERVICE
- Special permission for NotificationListenerService
- User must manually grant in system settings
- Cannot be requested programmatically

## Security Considerations

### Privacy
- All notifications are processed locally
- No data transmitted over network
- No external dependencies
- User has full control

### Permissions
- Notification access is sensitive
- App only logs data, doesn't modify notifications
- Clear user consent required

### Data Protection
- Currently logs to Logcat (development)
- Production apps should:
  - Encrypt stored data
  - Implement access controls
  - Provide data deletion
  - Follow privacy regulations

## Performance

### Battery Impact
- Foreground service: Minimal overhead (~1-2% per day)
- NotificationListenerService: Event-driven (no polling)
- UI: Compose with efficient recomposition

### Memory
- Lightweight services
- No data caching by default
- Clean architecture

### Network
- No network usage
- Fully offline operation

## Extensibility

### Adding Database Storage
```kotlin
// Add Room dependency
// Create database, DAO, and entities
// Modify handleWhatsAppNotification() to save data
```

### Adding Network Sync
```kotlin
// Add Retrofit dependency
// Create API interface
// Implement background sync worker
```

### Adding UI Dashboard
```kotlin
// Create new composables
// Display notification history
// Add filters and search
```

### Adding Analytics
```kotlin
// Track notification patterns
// Generate statistics
// Create charts with compose charts
```

## Testing

### Unit Tests
- Test NotificationHelper utility methods
- Test WhatsAppNotification data class
- Mock notification data

### Integration Tests
- Test service lifecycle
- Test notification filtering
- Test permission flows

### Manual Testing
1. Install app on physical device
2. Enable notification access
3. Send test WhatsApp messages
4. Verify logs in Logcat
5. Test reboot scenario
6. Test battery optimization

## Deployment

### Debug Build
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
./gradlew assembleRelease
# Sign the APK
# Distribute via Google Play or other channels
```

### Requirements for Users
- Android 7.0 (API 24) or higher
- WhatsApp or WhatsApp Business installed
- Notification access permission granted
- Recommended: Battery optimization disabled

## Known Limitations

1. **Message Content**: Only captures notification preview, not full message
2. **Media**: Cannot access images, videos, or voice messages
3. **Deleted Messages**: Cannot detect message deletions
4. **Read Status**: Cannot detect read/unread status
5. **Reply Actions**: Cannot trigger reply actions
6. **Group Members**: Limited group member information
7. **Encryption**: WhatsApp messages remain end-to-end encrypted

## Future Enhancements

- [ ] Add SQLite/Room database for persistent storage
- [ ] Implement notification history UI
- [ ] Add search and filter functionality
- [ ] Export notifications to CSV/JSON
- [ ] Add notification statistics and analytics
- [ ] Implement backup and restore
- [ ] Add notification templates
- [ ] Support for other messaging apps (Telegram, Signal, etc.)
- [ ] Web dashboard for remote monitoring
- [ ] Machine learning for message categorization

## Compliance & Legal

⚠️ **Important Notes**:
- This app is for educational and personal use
- Users must comply with local laws (GDPR, CCPA, etc.)
- Respect WhatsApp's Terms of Service
- Do not use for unauthorized surveillance
- Obtain proper consent before monitoring others' devices
- Implement proper data protection measures

## Support & Maintenance

### Logs Location
- Logcat tag: `WhatsAppListener`
- System service logs: `MonitorService`
- Boot logs: `BootReceiver`

### Common Issues
1. Service stops: Check battery optimization
2. Missing notifications: Verify WhatsApp notification settings
3. Permission denied: Re-grant notification access
4. Boot start fails: Check RECEIVE_BOOT_COMPLETED permission

### Debugging Commands
```bash
# Check if service is running
adb shell dumpsys activity services | grep NotificationMonitor

# Check notification listener status
adb shell dumpsys notification_listener

# View logs
adb logcat -s WhatsAppListener MonitorService BootReceiver

# Clear app data
adb shell pm clear com.gomad.notificationlistener
```

## Conclusion

This architecture provides a robust, scalable foundation for monitoring WhatsApp notifications 24/7. The modular design allows for easy customization and extension based on specific requirements.

