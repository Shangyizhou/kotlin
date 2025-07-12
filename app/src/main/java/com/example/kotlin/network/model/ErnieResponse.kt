package com.example.kotlin.network.model

import com.google.gson.annotations.SerializedName

/**
 * ERNIE API 响应数据模型（新版千帆API）
 */
data class ErnieResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("object")
    val objectType: String,
    
    @SerializedName("created")
    val created: Long,
    
    @SerializedName("model")
    val model: String? = null,
    
    @SerializedName("choices")
    val choices: List<Choice>? = null,
    
    // 兼容旧格式字段
    @SerializedName("result")
    val result: String? = null,
    
    @SerializedName("is_end")
    val isEnd: Boolean = true,
    
    @SerializedName("is_truncated")
    val isTruncated: Boolean = false,
    
    @SerializedName("need_clear_history")
    val needClearHistory: Boolean = false,
    
    @SerializedName("usage")
    val usage: Usage,
    
    @SerializedName("function_call")
    val functionCall: FunctionCall? = null,
    
    @SerializedName("ban_round")
    val banRound: Int = 0
) {
    // 获取实际的回复内容，兼容新旧格式
    fun getActualResult(): String {
        return result ?: choices?.firstOrNull()?.message?.content ?: ""
    }
}

/**
 * 选择项（新版API格式）
 */
data class Choice(
    @SerializedName("index")
    val index: Int,
    
    @SerializedName("message")
    val message: ResponseMessage,
    
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

/**
 * 响应消息（新版API格式）
 */
data class ResponseMessage(
    @SerializedName("role")
    val role: String,
    
    @SerializedName("content")
    val content: String
)

/**
 * 使用统计信息
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    
    @SerializedName("total_tokens")
    val totalTokens: Int
)

/**
 * 函数调用信息
 */
data class FunctionCall(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("thoughts")
    val thoughts: String,
    
    @SerializedName("arguments")
    val arguments: String
)

/**
 * 访问令牌响应模型
 */
data class AccessTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("expires_in")
    val expiresIn: Int,
    
    @SerializedName("error")
    val error: String? = null,
    
    @SerializedName("error_description")
    val errorDescription: String? = null
)

/**
 * 网络请求结果封装
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: Throwable) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
} 