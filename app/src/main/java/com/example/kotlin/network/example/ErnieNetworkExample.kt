package com.example.kotlin.network.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin.network.model.Message
import com.example.kotlin.network.model.NetworkResult
import com.example.kotlin.network.service.ErnieNetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ERNIE网络库使用示例
 * 展示如何在ViewModel中使用网络服务
 */
class ErnieNetworkExample : ViewModel() {
    
    private val networkService = ErnieNetworkService.getInstance()
    
    // UI状态管理
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // 你的百度API凭据（实际使用时应该从安全存储中获取）
    private val clientId = "27fhgYXGXRFg0KRv1zehNPYI"
    private val clientSecret = "6vLQhMbzNOqXfoRcVnNB33iynoHktvE6"
    
    /**
     * 发送简单消息的示例
     */
    fun sendSimpleMessage(userMessage: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = networkService.sendSimpleMessage(
                clientId = clientId,
                clientSecret = clientSecret,
                userMessage = userMessage,
                systemMessage = "你是一个有用的AI助手，请用中文回答问题。"
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        response = result.data,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    /**
     * 发送多轮对话的示例
     */
    fun sendConversation(messages: List<Message>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 先获取访问令牌
                val tokenResult = networkService.getAccessToken(clientId, clientSecret)
                if (tokenResult is NetworkResult.Error) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = tokenResult.exception.message
                    )
                    return@launch
                }
                
                val accessToken = (tokenResult as NetworkResult.Success).data
                
                // 发送聊天消息
                val chatResult = networkService.sendChatMessage(
                    accessToken = accessToken,
                    messages = messages,
                    temperature = 0.8,
                    topP = 0.9,
                    maxOutputTokens = 1024
                )
                
                when (chatResult) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            response = chatResult.data.result,
                            fullResponse = chatResult.data,
                            error = null
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = chatResult.exception.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "发生未知错误: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 清除响应
     */
    fun clearResponse() {
        _uiState.value = _uiState.value.copy(response = null, fullResponse = null)
    }
    
    /**
     * 检查网络服务状态
     */
    fun checkServiceStatus() {
        val isTokenValid = networkService.isTokenValid()
        println("访问令牌状态: ${if (isTokenValid) "有效" else "无效或已过期"}")
    }
}

/**
 * UI状态数据类
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val response: String? = null,
    val fullResponse: com.example.kotlin.network.model.ErnieResponse? = null,
    val error: String? = null
)

/**
 * 在Activity或Fragment中使用的示例代码注释
 */
/*
class MainActivity : ComponentActivity() {
    private val viewModel: ErnieNetworkExample by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 观察UI状态
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when {
                    state.isLoading -> {
                        // 显示加载状态
                        showLoading()
                    }
                    state.error != null -> {
                        // 显示错误信息
                        showError(state.error)
                    }
                    state.response != null -> {
                        // 显示响应结果
                        showResponse(state.response)
                    }
                }
            }
        }
        
        // 发送消息示例
        viewModel.sendSimpleMessage("你好，请介绍一下自己")
        
        // 多轮对话示例
        val conversation = listOf(
            Message("system", "你是一个专业的编程助手"),
            Message("user", "请解释什么是Kotlin协程"),
            Message("assistant", "Kotlin协程是一种并发设计模式..."),
            Message("user", "协程比线程有什么优势？")
        )
        viewModel.sendConversation(conversation)
    }
    
    private fun showLoading() {
        // 实现加载UI
    }
    
    private fun showError(error: String) {
        // 实现错误UI
    }
    
    private fun showResponse(response: String) {
        // 实现响应UI
    }
}
*/ 