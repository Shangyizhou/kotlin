package com.example.kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin.config.ApiConfig
import com.example.kotlin.data.ChatMessage
import com.example.kotlin.network.model.Message
import com.example.kotlin.network.service.NetworkResult
import com.example.kotlin.network.service.ErnieNetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 聊天页面ViewModel
 * 管理聊天消息和ERNIE API交互
 */
class ChatViewModel : ViewModel() {
    
    private val networkService = ErnieNetworkService.getInstance()
    
    // 从配置文件获取百度API凭据
    private val clientId = ApiConfig.BAIDU_API_KEY
    private val clientSecret = ApiConfig.BAIDU_SECRET_KEY
    
    // 聊天消息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
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
        // 直接使用固定的BCEv3 token，跳过API配置检查
        // 添加系统提示消息
        addSystemMessage("你是一个有用的AI助手，请用中文回答问题。")
        
        // 添加欢迎消息
        addWelcomeMessage()
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
        
        // 注释掉API配置检查，现在使用固定的BCEv3 token
        /*
        if (!ApiConfig.isApiConfigured()) {
            _errorMessage.value = "请先配置百度API密钥"
            
            val errorMessage = ChatMessage(
                id = generateMessageId(),
                content = "请在 app/src/main/java/com/example/kotlin/config/ApiConfig.kt 文件中配置你的百度API密钥后重试。",
                isMe = false
            )
            addMessageToUI(errorMessage)
            
            // 清空输入框
            _inputText.value = ""
            return
        }
        */
        
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
     * 添加系统消息到对话历史
     */
    private fun addSystemMessage(content: String) {
        conversationHistory.add(Message("system", content))
    }
    
    /**
     * 添加欢迎消息
     */
    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            id = generateMessageId(),
            content = "您好！我是AI助手，很高兴为您服务。请问有什么可以帮助您的吗？",
            isMe = false
        )
        addMessageToUI(welcomeMessage)
    }
    
    /**
     * 添加配置提示消息
     */
    private fun addConfigurationMessage() {
        val configMessage = ChatMessage(
            id = generateMessageId(),
            content = "⚠️ 百度API密钥未配置\n\n请按照以下步骤配置：\n\n1. 访问 https://console.bce.baidu.com/\n2. 创建千帆大模型应用\n3. 获取API Key和Secret Key\n4. 在 ApiConfig.kt 文件中配置密钥\n5. 重新启动应用\n\n✅ 新版API支持：\n• 千帆HTTP接口 (qianfan.baidubce.com)\n• ERNIE-4.0-8K模型\n• Bearer Token认证\n\n配置完成后即可开始聊天！",
            isMe = false
        )
        addMessageToUI(configMessage)
    }
    
    /**
     * 添加消息到UI列表
     */
    private fun addMessageToUI(message: ChatMessage) {
        _messages.value = _messages.value + message
    }
    
    /**
     * 生成消息ID
     */
    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
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
        _messages.value = emptyList()
        conversationHistory.clear()
        
        // 重新添加系统消息和欢迎消息
        addSystemMessage("你是一个有用的AI助手，请用中文回答问题。")
        addWelcomeMessage()
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