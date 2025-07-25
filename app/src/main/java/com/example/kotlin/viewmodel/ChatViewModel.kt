package com.example.kotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin.config.ApiConfig
import com.example.kotlin.data.ChatMessage
import com.example.kotlin.data.ChatMessageEntity
import com.example.kotlin.data.ChatRepository
import com.example.kotlin.data.ChatSessionEntity
import com.example.kotlin.data.ChatSessionRepository
import com.example.kotlin.data.AppDatabase
import com.example.kotlin.network.model.Message
import com.example.kotlin.network.service.NetworkResult
import com.example.kotlin.network.service.ErnieNetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 聊天页面ViewModel
 * 管理聊天消息和ERNIE API交互，支持本地数据库存储和多会话管理
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val networkService = ErnieNetworkService.getInstance()
    
    // 数据库相关
    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database.chatMessageDao())
    private val sessionRepository = ChatSessionRepository(database.chatSessionDao())
    
    // 从配置文件获取百度API凭据
    private val clientId = ApiConfig.BAIDU_API_KEY
    private val clientSecret = ApiConfig.BAIDU_SECRET_KEY
    
    // 当前会话ID
    private val _currentSessionId = MutableStateFlow<String>("default")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()
    
    // 聊天消息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    // 会话列表
    private val _sessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    val sessions: StateFlow<List<ChatSessionEntity>> = _sessions.asStateFlow()
    
    // 输入框文本
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 对话历史（用于API调用）
    private val conversationHistory = mutableListOf<Message>()
    
    init {
        // 加载会话列表
        loadSessions()
        
        // 加载默认会话
        loadChatHistory("default")
    }
    
    /**
     * 加载会话列表
     */
    private fun loadSessions() {
        viewModelScope.launch {
            sessionRepository.getAllSessions().collect { sessions ->
                _sessions.value = sessions
            }
        }
    }
    
    /**
     * 加载指定会话的聊天历史记录
     */
    private fun loadChatHistory(sessionId: String) {
        viewModelScope.launch {
            _currentSessionId.value = sessionId
            
            repository.getMessagesBySession(sessionId).collect { entities ->
                val chatMessages = entities.map { entity ->
                    ChatMessage(
                        id = entity.id.toString(),
                        content = entity.content,
                        isMe = entity.isMe,
                        timestamp = entity.timestamp
                    )
                }
                _messages.value = chatMessages
                
                // 重建对话历史
                conversationHistory.clear()
                conversationHistory.add(Message("system", "你是一个有用的AI助手，请用中文回答问题。"))
                chatMessages.forEach { message ->
                    val role = if (message.isMe) "user" else "assistant"
                    conversationHistory.add(Message(role, message.content))
                }
                
                // 如果没有历史记录，添加欢迎消息
                if (chatMessages.isEmpty()) {
                    addWelcomeMessage()
                }
            }
        }
    }
    
    /**
     * 切换到指定会话
     */
    fun switchToSession(sessionId: String) {
        loadChatHistory(sessionId)
    }
    
    /**
     * 创建新会话
     */
    fun createNewSession() {
        viewModelScope.launch {
            val newSessionId = UUID.randomUUID().toString()
            val session = sessionRepository.createNewSession(newSessionId, "新对话")
            
            // 切换到新会话
            loadChatHistory(newSessionId)
        }
    }
    
    /**
     * 删除会话
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
            
            // 如果删除的是当前会话，切换到默认会话
            if (sessionId == _currentSessionId.value) {
                loadChatHistory("default")
            }
        }
    }
    
    /**
     * 保存消息到数据库
     */
    private suspend fun saveMessageToDatabase(message: ChatMessage) {
        val entity = ChatMessageEntity(
            content = message.content,
            isMe = message.isMe,
            timestamp = message.timestamp,
            sessionId = _currentSessionId.value
        )
        repository.insertMessage(entity)
        
        // 更新会话的最后消息信息
        if (message.isMe) {
            val title = if (message.content.length > 20) {
                message.content.substring(0, 20) + "..."
            } else {
                message.content
            }
            sessionRepository.updateSessionTitle(_currentSessionId.value, title)
        }
        sessionRepository.updateLastMessage(_currentSessionId.value, message.content, message.timestamp)
    }
    
    /**
     * 更新输入文本
     */
    fun updateInputText(text: String) {
        _inputText.value = text
    }
    
    /**
     * 发送消息
     */
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank() || _isLoading.value) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 添加用户消息到UI
                val userChatMessage = ChatMessage(
                    id = generateMessageId(),
                    content = userMessage,
                    isMe = true
                )
                addMessageToUI(userChatMessage)
                
                // 保存用户消息到数据库
                saveMessageToDatabase(userChatMessage)
                
                // 添加用户消息到对话历史
                conversationHistory.add(Message("user", userMessage))
                
                // 清空输入框
                _inputText.value = ""
                
                // 调用ERNIE API（使用固定的BCEv3 token）
                val result = networkService.sendChatMessage(
                    accessToken = "dummy", // 现在token已在service中硬编码
                    messages = conversationHistory.toList()
                )
                
                when (result) {
                    is NetworkResult.Success -> {
                        val aiResponse = result.data.getActualResult()
                        
                        // 添加AI回复到UI
                        val aiChatMessage = ChatMessage(
                            id = generateMessageId(),
                            content = aiResponse,
                            isMe = false
                        )
                        addMessageToUI(aiChatMessage)
                        
                        // 保存AI回复到数据库
                        saveMessageToDatabase(aiChatMessage)
                        
                        // 添加AI回复到对话历史
                        conversationHistory.add(Message("assistant", aiResponse))
                    }
                    
                    is NetworkResult.Error -> {
                        _errorMessage.value = "发送失败: ${result.exception.message}"
                        
                        // 添加错误提示消息
                        val errorMessage = ChatMessage(
                            id = generateMessageId(),
                            content = "抱歉，我暂时无法回复您的消息。请稍后重试。",
                            isMe = false
                        )
                        addMessageToUI(errorMessage)
                        
                        // 保存错误消息到数据库
                        saveMessageToDatabase(errorMessage)
                    }
                    
                    NetworkResult.Loading -> {
                        // 已经在处理中
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "发送失败: ${e.message}"
                
                val errorMessage = ChatMessage(
                    id = generateMessageId(),
                    content = "抱歉，发生了未知错误。请稍后重试。",
                    isMe = false
                )
                addMessageToUI(errorMessage)
                
                // 保存错误消息到数据库
                saveMessageToDatabase(errorMessage)
                
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 获取访问令牌
     */
    private suspend fun getAccessToken(): String {
        val tokenResult = networkService.getAccessToken(clientId, clientSecret)
        return when (tokenResult) {
            is NetworkResult.Success -> tokenResult.data
            is NetworkResult.Error -> throw Exception("获取访问令牌失败: ${tokenResult.exception.message}")
            NetworkResult.Loading -> throw Exception("正在获取访问令牌...")
        }
    }
    
    /**
     * 添加系统消息
     */
    private fun addSystemMessage(content: String) {
        conversationHistory.add(Message("system", content))
    }
    
    /**
     * 添加欢迎消息
     */
    private suspend fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            id = generateMessageId(),
            content = "你好！我是AI聊天机器人，有什么可以帮助你的吗？",
            isMe = false
        )
        addMessageToUI(welcomeMessage)
        saveMessageToDatabase(welcomeMessage)
    }
    
    /**
     * 添加消息到UI
     */
    private fun addMessageToUI(message: ChatMessage) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
    
    /**
     * 生成消息ID
     */
    private fun generateMessageId(): String {
        return System.currentTimeMillis().toString()
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 清空聊天记录
     */
    fun clearChat() {
        viewModelScope.launch {
            // 清空数据库
            repository.deleteMessagesBySession(_currentSessionId.value)
            
            // 清空UI
            _messages.value = emptyList()
            conversationHistory.clear()
            
            // 重新添加系统消息和欢迎消息
            addSystemMessage("你是一个有用的AI助手，请用中文回答问题。")
            addWelcomeMessage()
        }
    }
    
    /**
     * 重试发送消息
     */
    fun retryLastMessage() {
        val lastUserMessage = _messages.value.lastOrNull { it.isMe }
        lastUserMessage?.let {
            sendMessage(it.content)
        }
    }
    
    /**
     * 获取对话历史大小
     */
    fun getConversationSize(): Int {
        return conversationHistory.size
    }
    
    /**
     * 检查是否可以发送消息
     */
    fun canSendMessage(): Boolean {
        return !_isLoading.value && _inputText.value.isNotBlank()
    }
} 