# Code Examples & Extensions

This document provides ready-to-use code examples for extending the WhatsApp Notification Listener app.

## Table of Contents
1. [Database Storage](#database-storage)
2. [File Storage](#file-storage)
3. [Network Sync](#network-sync)
4. [Notification Filtering](#notification-filtering)
5. [Statistics & Analytics](#statistics--analytics)
6. [Export Features](#export-features)

---

## Database Storage

### Using Room Database

#### 1. Add Dependencies
```kotlin
// app/build.gradle.kts
dependencies {
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    // For Kotlin use kapt instead of annotationProcessor
    // kapt("androidx.room:room-compiler:$roomVersion")
}
```

#### 2. Create Entity
```kotlin
// WhatsAppNotificationEntity.kt
package com.gomad.notificationlistener.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_notifications")
data class WhatsAppNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationId: Int,
    val packageName: String,
    val title: String,
    val text: String,
    val bigText: String?,
    val subText: String?,
    val timestamp: Long,
    val capturedAt: Long
)
```

#### 3. Create DAO
```kotlin
// WhatsAppNotificationDao.kt
package com.gomad.notificationlistener.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppNotificationDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: WhatsAppNotificationEntity)
    
    @Query("SELECT * FROM whatsapp_notifications ORDER BY capturedAt DESC")
    fun getAllNotifications(): Flow<List<WhatsAppNotificationEntity>>
    
    @Query("SELECT * FROM whatsapp_notifications WHERE title = :title ORDER BY capturedAt DESC")
    fun getNotificationsByTitle(title: String): Flow<List<WhatsAppNotificationEntity>>
    
    @Query("SELECT * FROM whatsapp_notifications WHERE capturedAt >= :startTime")
    fun getNotificationsSince(startTime: Long): Flow<List<WhatsAppNotificationEntity>>
    
    @Query("DELETE FROM whatsapp_notifications")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM whatsapp_notifications")
    suspend fun getCount(): Int
}
```

#### 4. Create Database
```kotlin
// AppDatabase.kt
package com.gomad.notificationlistener.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WhatsAppNotificationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun whatsAppNotificationDao(): WhatsAppNotificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whatsapp_notification_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

#### 5. Use in Service
```kotlin
// In WhatsAppNotificationListener.kt
private lateinit var database: AppDatabase

override fun onCreate() {
    super.onCreate()
    database = AppDatabase.getDatabase(this)
}

private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... existing code ...
    
    // Save to database
    CoroutineScope(Dispatchers.IO).launch {
        val entity = WhatsAppNotificationEntity(
            notificationId = whatsAppNotification.id,
            packageName = whatsAppNotification.packageName,
            title = whatsAppNotification.title,
            text = whatsAppNotification.text,
            bigText = whatsAppNotification.bigText,
            subText = whatsAppNotification.subText,
            timestamp = whatsAppNotification.timestamp,
            capturedAt = whatsAppNotification.capturedAt
        )
        database.whatsAppNotificationDao().insert(entity)
    }
}
```

---

## File Storage

### Save Notifications to JSON File

```kotlin
// FileStorageHelper.kt
package com.gomad.notificationlistener.utils

import android.content.Context
import com.gomad.notificationlistener.WhatsAppNotification
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object FileStorageHelper {
    
    private const val FILENAME = "whatsapp_notifications.json"
    
    fun saveNotification(context: Context, notification: WhatsAppNotification) {
        try {
            val file = File(context.getExternalFilesDir(null), FILENAME)
            val jsonArray = if (file.exists()) {
                JSONArray(file.readText())
            } else {
                JSONArray()
            }
            
            val jsonObject = JSONObject().apply {
                put("id", notification.id)
                put("packageName", notification.packageName)
                put("title", notification.title)
                put("text", notification.text)
                put("bigText", notification.bigText)
                put("subText", notification.subText)
                put("timestamp", notification.timestamp)
                put("capturedAt", notification.capturedAt)
            }
            
            jsonArray.put(jsonObject)
            file.writeText(jsonArray.toString(2))
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getAllNotifications(context: Context): List<WhatsAppNotification> {
        try {
            val file = File(context.getExternalFilesDir(null), FILENAME)
            if (!file.exists()) return emptyList()
            
            val jsonArray = JSONArray(file.readText())
            val notifications = mutableListOf<WhatsAppNotification>()
            
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                notifications.add(
                    WhatsAppNotification(
                        id = json.getInt("id"),
                        packageName = json.getString("packageName"),
                        title = json.getString("title"),
                        text = json.getString("text"),
                        bigText = json.optString("bigText", null),
                        subText = json.optString("subText", null),
                        timestamp = json.getLong("timestamp"),
                        capturedAt = json.getLong("capturedAt")
                    )
                )
            }
            
            return notifications
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    fun clearAll(context: Context) {
        val file = File(context.getExternalFilesDir(null), FILENAME)
        file.delete()
    }
}

// Usage in WhatsAppNotificationListener
private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... existing code ...
    FileStorageHelper.saveNotification(this, whatsAppNotification)
}
```

### Save as CSV

```kotlin
// CSVExporter.kt
package com.gomad.notificationlistener.utils

import android.content.Context
import com.gomad.notificationlistener.WhatsAppNotification
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CSVExporter {
    
    private const val CSV_HEADER = "ID,Package,Title,Text,Timestamp,Captured At\n"
    
    fun exportToCSV(context: Context, notifications: List<WhatsAppNotification>): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val filename = "whatsapp_export_${dateFormat.format(Date())}.csv"
        val file = File(context.getExternalFilesDir(null), filename)
        
        val csvContent = buildString {
            append(CSV_HEADER)
            notifications.forEach { notification ->
                append("${notification.id},")
                append("${notification.packageName},")
                append("\"${notification.title.replace("\"", "\"\"")}\",")
                append("\"${notification.text.replace("\"", "\"\"")}\",")
                append("${notification.timestamp},")
                append("${notification.capturedAt}\n")
            }
        }
        
        file.writeText(csvContent)
        return file
    }
}
```

---

## Network Sync

### Send to Server with Retrofit

#### 1. Add Dependencies
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}

// Also add internet permission to AndroidManifest.xml
// <uses-permission android:name="android.permission.INTERNET" />
```

#### 2. Create API Interface
```kotlin
// NotificationApi.kt
package com.gomad.notificationlistener.network

import com.gomad.notificationlistener.WhatsAppNotification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApi {
    
    @POST("api/notifications")
    suspend fun uploadNotification(
        @Body notification: WhatsAppNotification
    ): Response<Unit>
}
```

#### 3. Create Retrofit Instance
```kotlin
// RetrofitClient.kt
package com.gomad.notificationlistener.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    
    private const val BASE_URL = "https://your-server.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    val api: NotificationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApi::class.java)
    }
}
```

#### 4. Upload in Service
```kotlin
// In WhatsAppNotificationListener.kt
private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... existing code ...
    
    // Upload to server
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.api.uploadNotification(whatsAppNotification)
            if (response.isSuccessful) {
                Log.d(TAG, "Notification uploaded successfully")
            } else {
                Log.e(TAG, "Upload failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
        }
    }
}
```

---

## Notification Filtering

### Filter by Contact

```kotlin
// ContactFilter.kt
package com.gomad.notificationlistener.filters

object ContactFilter {
    
    private val allowedContacts = setOf(
        "John Doe",
        "Jane Smith",
        "Important Group"
    )
    
    fun shouldProcess(title: String): Boolean {
        return allowedContacts.any { contact ->
            title.contains(contact, ignoreCase = true)
        }
    }
}

// Usage
private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... extract data ...
    
    if (!ContactFilter.shouldProcess(title)) {
        Log.d(TAG, "Skipping notification from: $title")
        return
    }
    
    // Continue processing...
}
```

### Filter by Keywords

```kotlin
// KeywordFilter.kt
package com.gomad.notificationlistener.filters

object KeywordFilter {
    
    private val importantKeywords = listOf(
        "urgent", "important", "asap", "emergency",
        "meeting", "deadline", "payment"
    )
    
    fun isImportant(text: String): Boolean {
        return importantKeywords.any { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
    }
    
    fun containsKeyword(text: String, keyword: String): Boolean {
        return text.contains(keyword, ignoreCase = true)
    }
}

// Usage
private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... extract data ...
    
    if (KeywordFilter.isImportant(text)) {
        Log.d(TAG, "⚠️ IMPORTANT MESSAGE: $text")
        // Trigger special handling for important messages
    }
}
```

### Time-Based Filtering

```kotlin
// TimeFilter.kt
package com.gomad.notificationlistener.filters

import java.util.*

object TimeFilter {
    
    fun isWorkingHours(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Monday to Friday, 9 AM to 5 PM
        return dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY && hour in 9..17
    }
    
    fun isNightTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour < 7 || hour >= 22
    }
}

// Usage
private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... extract data ...
    
    if (TimeFilter.isNightTime()) {
        Log.d(TAG, "Night time notification: $title")
        // Handle night notifications differently
    }
}
```

---

## Statistics & Analytics

### Notification Counter

```kotlin
// NotificationStats.kt
package com.gomad.notificationlistener.stats

import android.content.Context
import android.content.SharedPreferences

class NotificationStats(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_stats",
        Context.MODE_PRIVATE
    )
    
    fun incrementCount(contact: String) {
        val currentCount = getCount(contact)
        prefs.edit().putInt(contact, currentCount + 1).apply()
    }
    
    fun getCount(contact: String): Int {
        return prefs.getInt(contact, 0)
    }
    
    fun getTotalCount(): Int {
        return prefs.all.values.sumOf { it as? Int ?: 0 }
    }
    
    fun getTopContacts(limit: Int = 10): List<Pair<String, Int>> {
        return prefs.all
            .mapNotNull { (key, value) -> 
                if (value is Int) key to value else null 
            }
            .sortedByDescending { it.second }
            .take(limit)
    }
    
    fun reset() {
        prefs.edit().clear().apply()
    }
}

// Usage in Service
private lateinit var stats: NotificationStats

override fun onCreate() {
    super.onCreate()
    stats = NotificationStats(this)
}

private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... extract data ...
    stats.incrementCount(title)
}
```

### Daily Report

```kotlin
// DailyReport.kt
package com.gomad.notificationlistener.stats

import java.text.SimpleDateFormat
import java.util.*

data class DailyReport(
    val date: String,
    val totalNotifications: Int,
    val contactBreakdown: Map<String, Int>,
    val peakHour: Int,
    val avgPerHour: Double
) {
    
    fun toFormattedString(): String {
        return buildString {
            appendLine("=== Daily Report: $date ===")
            appendLine("Total Notifications: $totalNotifications")
            appendLine("Average per Hour: %.2f".format(avgPerHour))
            appendLine("Peak Hour: ${peakHour}:00")
            appendLine("\nTop Contacts:")
            contactBreakdown.entries
                .sortedByDescending { it.value }
                .take(5)
                .forEachIndexed { index, (contact, count) ->
                    appendLine("${index + 1}. $contact: $count")
                }
            appendLine("=======================")
        }
    }
}

// Generate report
fun generateDailyReport(notifications: List<WhatsAppNotification>): DailyReport {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayNotifications = notifications.filter { 
        // Filter notifications from today
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(it.capturedAt))
        date == today
    }
    
    val contactCount = todayNotifications
        .groupBy { it.title }
        .mapValues { it.value.size }
    
    val hourlyCount = todayNotifications
        .groupBy { 
            Calendar.getInstance().apply { 
                timeInMillis = it.capturedAt 
            }.get(Calendar.HOUR_OF_DAY) 
        }
    
    val peakHour = hourlyCount.maxByOrNull { it.value }?.key ?: 0
    
    return DailyReport(
        date = today,
        totalNotifications = todayNotifications.size,
        contactBreakdown = contactCount,
        peakHour = peakHour,
        avgPerHour = todayNotifications.size / 24.0
    )
}
```

---

## Export Features

### Share Notifications

```kotlin
// ShareHelper.kt
package com.gomad.notificationlistener.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.gomad.notificationlistener.WhatsAppNotification
import java.io.File

object ShareHelper {
    
    fun shareAsText(context: Context, notifications: List<WhatsAppNotification>) {
        val text = notifications.joinToString("\n\n") { it.toLogString() }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "WhatsApp Notifications Export")
        }
        
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
    
    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share file"))
    }
}

// Add FileProvider to AndroidManifest.xml:
/*
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
*/

// Create res/xml/file_paths.xml:
/*
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="files" path="." />
</paths>
*/
```

---

## Performance Optimization

### Batch Processing

```kotlin
// BatchProcessor.kt
package com.gomad.notificationlistener.utils

import com.gomad.notificationlistener.WhatsAppNotification
import kotlinx.coroutines.*

class BatchProcessor(
    private val batchSize: Int = 10,
    private val batchDelay: Long = 5000L, // 5 seconds
    private val processor: suspend (List<WhatsAppNotification>) -> Unit
) {
    
    private val batch = mutableListOf<WhatsAppNotification>()
    private var job: Job? = null
    
    fun add(notification: WhatsAppNotification) {
        synchronized(batch) {
            batch.add(notification)
            
            if (batch.size >= batchSize) {
                processBatch()
            } else if (job == null || job?.isActive == false) {
                scheduleBatch()
            }
        }
    }
    
    private fun scheduleBatch() {
        job = CoroutineScope(Dispatchers.IO).launch {
            delay(batchDelay)
            processBatch()
        }
    }
    
    private fun processBatch() {
        synchronized(batch) {
            if (batch.isNotEmpty()) {
                val copy = batch.toList()
                batch.clear()
                CoroutineScope(Dispatchers.IO).launch {
                    processor(copy)
                }
            }
        }
    }
}

// Usage
private val batchProcessor = BatchProcessor { notifications ->
    // Process batch of notifications
    database.insertAll(notifications)
}

private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
    // ... extract data ...
    batchProcessor.add(whatsAppNotification)
}
```

---

These examples should help you extend the app with various features. Feel free to mix and match based on your requirements!

