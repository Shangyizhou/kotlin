package com.example.kotlin.data

import kotlinx.coroutines.flow.Flow

/**
 * 聊天消息仓库
 */
class ChatRepository(private val chatMessageDao: ChatMessageDao) {
    
    /**
     * 获取所有消息
     */
    fun getAllMessages(): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getAllMessages()
    }
    
    /**
     * 根据会话ID获取消息
     */
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesBySession(sessionId)
    }
    
    /**
     * 插入消息
     */
    suspend fun insertMessage(message: ChatMessageEntity) {
        chatMessageDao.insertMessage(message)
    }
    
    /**
     * 插入多条消息
     */
    suspend fun insertMessages(messages: List<ChatMessageEntity>) {
        chatMessageDao.insertMessages(messages)
    }
    
    /**
     * 删除消息
     */
    suspend fun deleteMessage(message: ChatMessageEntity) {
        chatMessageDao.deleteMessage(message)
    }
    
    /**
     * 删除所有消息
     */
    suspend fun deleteAllMessages() {
        chatMessageDao.deleteAllMessages()
    }
    
    /**
     * 删除指定会话的消息
     */
    suspend fun deleteMessagesBySession(sessionId: String) {
        chatMessageDao.deleteMessagesBySession(sessionId)
    }
    
    /**
     * 获取消息数量
     */
    suspend fun getMessageCount(): Int {
        return chatMessageDao.getMessageCount()
    }
} 