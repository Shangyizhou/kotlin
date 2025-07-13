package com.example.kotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 聊天消息数据访问对象
 */
@Dao
interface ChatMessageDao {
    
    /**
     * 获取所有聊天消息
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>
    
    /**
     * 根据会话ID获取消息
     */
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>>
    
    /**
     * 插入消息
     */
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)
    
    /**
     * 插入多条消息
     */
    @Insert
    suspend fun insertMessages(messages: List<ChatMessageEntity>)
    
    /**
     * 删除消息
     */
    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)
    
    /**
     * 删除所有消息
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
    
    /**
     * 删除指定会话的消息
     */
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: String)
    
    /**
     * 获取消息数量
     */
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
} 