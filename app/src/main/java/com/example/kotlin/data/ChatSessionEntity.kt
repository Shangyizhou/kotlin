package com.example.kotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 聊天会话实体
 */
@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val sessionId: String,
    
    val title: String, // 会话标题，通常是第一条用户消息的摘要
    
    val lastMessage: String, // 最后一条消息内容
    
    val lastMessageTime: Long, // 最后一条消息时间
    
    val messageCount: Int = 0, // 消息数量
    
    val createdAt: Long = System.currentTimeMillis() // 创建时间
) 