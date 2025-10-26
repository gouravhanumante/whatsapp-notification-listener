package com.gomad.notificationlistener

/**
 * Data class to represent a WhatsApp notification
 */
data class WhatsAppNotification(
    val id: Int,
    val packageName: String,
    val title: String,
    val text: String,
    val bigText: String?,
    val subText: String?,
    val timestamp: Long,
    val capturedAt: Long = System.currentTimeMillis()
) {
    fun toLogString(): String {
        return buildString {
            appendLine("=== WhatsApp Notification ===")
            appendLine("ID: $id")
            appendLine("Package: $packageName")
            appendLine("Title: $title")
            appendLine("Text: $text")
            if (bigText != null) {
                appendLine("Big Text: $bigText")
            }
            if (subText != null) {
                appendLine("Sub Text: $subText")
            }
            appendLine("Timestamp: $timestamp")
            appendLine("Captured At: $capturedAt")
            appendLine("===========================")
        }
    }
}

