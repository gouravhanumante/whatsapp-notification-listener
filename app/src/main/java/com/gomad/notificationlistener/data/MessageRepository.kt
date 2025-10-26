package com.gomad.notificationlistener.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow

class MessageRepository(context: Context) {
    
    private val messageDao = AppDatabase.getDatabase(context).messageDao()
    
    companion object {
        private const val TAG = "MessageRepository"
    }
    
    fun getAllMessages(): Flow<List<MessageEntity>> {
        return messageDao.getAllMessages()
    }
    
    fun getMessagesByName(name: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByName(name)
    }
    
    fun getRecentMessages(limit: Int = 50): Flow<List<MessageEntity>> {
        return messageDao.getRecentMessages(limit)
    }
    
    fun getContactsWithCount(): Flow<List<ContactWithCount>> {
        return messageDao.getContactsWithCount()
    }
    
    suspend fun getCount(): Int {
        return try {
            messageDao.getCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting count", e)
            0
        }
    }
    
    suspend fun deleteAll() {
        try {
            messageDao.deleteAll()
            Log.d(TAG, "All messages deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting messages", e)
        }
    }
}

