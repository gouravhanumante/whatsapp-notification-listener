package com.gomad.notificationlistener

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.gomad.notificationlistener.data.AppDatabase
import com.gomad.notificationlistener.data.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WhatsAppNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "WhatsAppListener"
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }

    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListener Service Created")
        
        // Initialize database
        database = AppDatabase.getDatabase(this)
        
        // Start foreground service to keep this running
        startForegroundService()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        // Check if notification is from WhatsApp or WhatsApp Business
        if (packageName == WHATSAPP_PACKAGE || packageName == WHATSAPP_BUSINESS_PACKAGE) {
            handleWhatsAppNotification(sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        if (packageName == WHATSAPP_PACKAGE || packageName == WHATSAPP_BUSINESS_PACKAGE) {
            Log.d(TAG, "WhatsApp notification removed: ${sbn.id}")
        }
    }

    private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val extras = notification.extras
            
            // Extract notification details
            val title = extras.getString(Notification.EXTRA_TITLE) ?: "No Title"
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No Text"
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            val timestamp = sbn.postTime
            
            // Filter out summary notifications (e.g., "2 new messages", "3 messages from 2 chats")
            val messageToSave = bigText ?: text
            if (isSummaryNotification(messageToSave)) {
                Log.d(TAG, "⊘ Skipping summary notification: $messageToSave")
                return
            }
            
            // Create notification object
            val whatsAppNotification = WhatsAppNotification(
                id = sbn.id,
                packageName = sbn.packageName,
                title = title,
                text = text,
                bigText = bigText,
                subText = subText,
                timestamp = timestamp
            )
            
            // Log the notification
            Log.d(TAG, whatsAppNotification.toLogString())
            
            // Save to database
            saveToDatabase(title, messageToSave, timestamp)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing WhatsApp notification", e)
        }
    }
    
    private fun isSummaryNotification(message: String): Boolean {
        // Filter out WhatsApp summary notifications like:
        // "2 new messages", "3 messages from 2 chats", etc.
        val summaryPatterns = listOf(
            Regex("\\d+ new messages?", RegexOption.IGNORE_CASE),
            Regex("\\d+ messages? from \\d+ chats?", RegexOption.IGNORE_CASE),
            Regex("messages? from \\d+ chats?", RegexOption.IGNORE_CASE),
            Regex("^\\d+ messages?$", RegexOption.IGNORE_CASE),
            Regex("Checking for new messages", RegexOption.IGNORE_CASE)
        )
        
        return summaryPatterns.any { pattern -> pattern.matches(message) }
    }

    private fun saveToDatabase(name: String, message: String, timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messageEntity = MessageEntity(
                    name = name,
                    message = message,
                    timestamp = timestamp
                )
                database.messageDao().insert(messageEntity)
                Log.d(TAG, "✓ Saved to database: $name - $message")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to database", e)
            }
        }
    }

    private fun startForegroundService() {
        // Start the foreground service that keeps the app running
        val serviceIntent = Intent(this, NotificationMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification Listener Disconnected")
        
        // Request rebind
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(android.content.ComponentName(this, WhatsAppNotificationListener::class.java))
        }
    }
}

