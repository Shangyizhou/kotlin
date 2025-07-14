package com.example.kotlin.data

import kotlinx.coroutines.flow.Flow

/**
 * 聊天会话仓库
 */
class ChatSessionRepository(private val chatSessionDao: ChatSessionDao) {
    
    /**
     * 获取所有聊天会话
     */
    fun getAllSessions(): Flow<List<ChatSessionEntity>> {
        return chatSessionDao.getAllSessions()
    }
    
    /**
     * 根据会话ID获取会话
     */
    suspend fun getSessionById(sessionId: String): ChatSessionEntity? {
        return chatSessionDao.getSessionById(sessionId)
    }
    
    /**
     * 插入或更新会话
     */
    suspend fun insertOrUpdateSession(session: ChatSessionEntity) {
        chatSessionDao.insertOrUpdateSession(session)
    }
    
    /**
     * 更新会话的最后消息信息
     */
    suspend fun updateLastMessage(sessionId: String, lastMessage: String, lastMessageTime: Long) {
        chatSessionDao.updateLastMessage(sessionId, lastMessage, lastMessageTime)
    }
    
    /**
     * 更新会话标题
     */
    suspend fun updateSessionTitle(sessionId: String, title: String) {
        chatSessionDao.updateSessionTitle(sessionId, title)
    }
    
    /**
     * 删除会话
     */
    suspend fun deleteSession(sessionId: String) {
        chatSessionDao.deleteSession(sessionId)
    }
    
    /**
     * 删除所有会话
     */
    suspend fun deleteAllSessions() {
        chatSessionDao.deleteAllSessions()
    }
    
    /**
     * 创建新会话
     */
    suspend fun createNewSession(sessionId: String, title: String = "新对话"): ChatSessionEntity {
        val session = ChatSessionEntity(
            sessionId = sessionId,
            title = title,
            lastMessage = "",
            lastMessageTime = System.currentTimeMillis(),
            messageCount = 0
        )
        chatSessionDao.insertOrUpdateSession(session)
        return session
    }
} 