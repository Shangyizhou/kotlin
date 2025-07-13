package com.example.kotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 聊天消息数据库实体
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val content: String,
    
    val isMe: Boolean,
    
    val timestamp: Long,
    
    val sessionId: String = "default" // 会话ID，用于支持多会话
) 