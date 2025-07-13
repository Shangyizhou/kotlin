package com.example.kotlin.network.service

import com.example.kotlin.network.api.ErnieApiService
import com.example.kotlin.network.config.NetworkConfig
import com.example.kotlin.network.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * ERNIE网络服务类，封装所有网络请求逻辑
 */
class ErnieNetworkService {
    
    private val chatApiService: ErnieApiService = NetworkConfig.getErnieChatService()
    private val oauthApiService: ErnieApiService = NetworkConfig.getOAuthService()
    
    // 缓存访问令牌和过期时间
    private var cachedAccessToken: String? = null
    private var tokenExpirationTime: Long = 0
    
    /**
     * 获取访问令牌（带缓存机制）
     */
    suspend fun getAccessToken(
        clientId: String,
        clientSecret: String
    ): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            // 检查缓存的令牌是否仍然有效
            val currentTime = System.currentTimeMillis()
            if (cachedAccessToken != null && currentTime < tokenExpirationTime) {
                return@withContext NetworkResult.Success(cachedAccessToken!!)
            }
            
            val response = oauthApiService.getAccessToken(
                grantType = "client_credentials",
                clientId = clientId,
                clientSecret = clientSecret
            )
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null && tokenResponse.error == null) {
                    // 缓存令牌，提前5分钟过期以避免边界情况
                    cachedAccessToken = tokenResponse.accessToken
                    tokenExpirationTime = currentTime + (tokenResponse.expiresIn - 300) * 1000L
                    
                    NetworkResult.Success(tokenResponse.accessToken)
                } else {
                    NetworkResult.Error(Exception("获取访问令牌失败: ${tokenResponse?.errorDescription}"))
                }
            } else {
                NetworkResult.Error(Exception("HTTP错误: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(handleNetworkException(e))
        }
    }
    
    /**
     * 发送聊天消息（新版千帆API）
     */
    suspend fun sendChatMessage(
        accessToken: String,
        messages: List<Message>,
        temperature: Double = 0.95,
        topP: Double = 0.8,
        maxOutputTokens: Int = 2048
    ): NetworkResult<ErnieResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ErnieRequest(
                model = "ernie-4.0-8k",
                messages = messages,
                temperature = temperature,
                topP = topP,
                maxOutputTokens = maxOutputTokens,
                webSearch = com.example.kotlin.network.model.WebSearch(
                    enable = false,
                    enableCitation = false,
                    enableTrace = false
                )
            )
            
            // 使用用户提供的BCEv3认证token
            val authHeader = "Bearer bce-v3/ALTAK-GqxbccRpT1Dmnwh1drELr/7647d1bc35260068f85680514d93d1ab555a9df9"
            
            // 调试日志
            println("🚀 发送聊天请求:")
            println("   URL: https://qianfan.baidubce.com/v2/chat/completions?chat-ernie-4.0-8k")
            println("   Authorization: $authHeader")
            println("   消息数量: ${messages.size}")
            println("   最后一条消息: ${messages.lastOrNull()?.content}")
            
            val response = chatApiService.chatCompletion(
                model = null, // 使用默认的chat-ernie-4.0-8k参数
                authorization = authHeader,
                request = request
            )
            
            // 调试日志 - 响应
            println("📥 收到响应:")
            println("   状态码: ${response.code()}")
            println("   响应消息: ${response.message()}")
            
            if (response.isSuccessful) {
                val ernieResponse = response.body()
                if (ernieResponse != null) {
                    println("   ✅ 响应成功")
                    println("   AI回复: ${ernieResponse.getActualResult()}")
                    NetworkResult.Success(ernieResponse)
                } else {
                    println("   ❌ 响应体为空")
                    NetworkResult.Error(Exception("响应体为空"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("   ❌ HTTP错误: ${response.code()} ${response.message()}")
                println("   错误详情: $errorBody")
                NetworkResult.Error(Exception("HTTP错误: ${response.code()} ${response.message()}\n详情: $errorBody"))
            }
        } catch (e: Exception) {
            println("💥 网络请求异常:")
            println("   异常类型: ${e.javaClass.simpleName}")
            println("   异常消息: ${e.message}")
            e.printStackTrace()
            NetworkResult.Error(handleNetworkException(e))
        }
    }
    
    /**
     * 发送简单的文本消息
     */
    suspend fun sendSimpleMessage(
        clientId: String,
        clientSecret: String,
        userMessage: String,
        systemMessage: String? = null
    ): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            // 首先获取访问令牌
            val tokenResult = getAccessToken(clientId, clientSecret)
            if (tokenResult is NetworkResult.Error) {
                return@withContext NetworkResult.Error(tokenResult.exception)
            }
            
            val accessToken = (tokenResult as NetworkResult.Success).data
            
            // 构建消息列表
            val messages = mutableListOf<Message>()
            if (systemMessage != null) {
                messages.add(Message("system", systemMessage))
            }
            messages.add(Message("user", userMessage))
            
            // 发送聊天请求
            val chatResult = sendChatMessage(accessToken, messages)
            
            when (chatResult) {
                is NetworkResult.Success -> {
                    NetworkResult.Success(chatResult.data.getActualResult())
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(chatResult.exception)
                }
                NetworkResult.Loading -> {
                    NetworkResult.Loading
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(handleNetworkException(e))
        }
    }
    
    /**
     * 清除缓存的访问令牌
     */
    fun clearCachedToken() {
        cachedAccessToken = null
        tokenExpirationTime = 0
    }
    
    /**
     * 检查访问令牌是否有效
     */
    fun isTokenValid(): Boolean {
        return cachedAccessToken != null && System.currentTimeMillis() < tokenExpirationTime
    }
    
    /**
     * 处理网络异常，返回更友好的错误信息
     */
    private fun handleNetworkException(exception: Exception): Exception {
        return when (exception) {
            is UnknownHostException -> Exception("网络连接失败，请检查网络设置")
            is ConnectException -> Exception("无法连接到服务器，请稍后重试")
            is SocketTimeoutException -> Exception("请求超时，请检查网络连接")
            else -> Exception("网络请求失败: ${exception.message}")
        }
    }
    
    companion object {
        // 单例模式
        @Volatile
        private var INSTANCE: ErnieNetworkService? = null
        
        fun getInstance(): ErnieNetworkService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ErnieNetworkService().also { INSTANCE = it }
            }
        }
    }
} 