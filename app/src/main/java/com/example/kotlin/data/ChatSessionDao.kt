package com.example.kotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 聊天会话数据访问对象
 */
@Dao
interface ChatSessionDao {
    
    /**
     * 获取所有聊天会话，按最后消息时间倒序排列
     */
    @Query("SELECT * FROM chat_sessions ORDER BY lastMessageTime DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>
    
    /**
     * 根据会话ID获取会话
     */
    @Query("SELECT * FROM chat_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?
    
    /**
     * 插入或更新会话
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: ChatSessionEntity)
    
    /**
     * 更新会话的最后消息信息
     */
    @Query("UPDATE chat_sessions SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime, messageCount = messageCount + 1 WHERE sessionId = :sessionId")
    suspend fun updateLastMessage(sessionId: String, lastMessage: String, lastMessageTime: Long)
    
    /**
     * 更新会话标题
     */
    @Query("UPDATE chat_sessions SET title = :title WHERE sessionId = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, title: String)
    
    /**
     * 删除会话
     */
    @Query("DELETE FROM chat_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)
    
    /**
     * 删除所有会话
     */
    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()
} 