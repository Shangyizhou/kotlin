package com.example.kotlin.data

data class ChatMessage(
    val id: String,
    val content: String,
    val isMe: Boolean, // 区分左右气泡
    val timestamp: Long = System.currentTimeMillis()
)