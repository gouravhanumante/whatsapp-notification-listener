package com.gomad.notificationlistener.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ContactWithCount(
    val name: String,
    val messageCount: Int,
    val lastMessageTime: Long
)

@Dao
interface MessageDao {
    
    @Insert
    suspend fun insert(message: MessageEntity)
    
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE name = :name ORDER BY timestamp DESC")
    fun getMessagesByName(name: String): Flow<List<MessageEntity>>
    
    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getCount(): Int
    
    @Query("DELETE FROM messages")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<MessageEntity>>
    
    @Query("""
        SELECT name, COUNT(*) as messageCount, MAX(timestamp) as lastMessageTime 
        FROM messages 
        GROUP BY name 
        ORDER BY lastMessageTime DESC
    """)
    fun getContactsWithCount(): Flow<List<ContactWithCount>>
}

