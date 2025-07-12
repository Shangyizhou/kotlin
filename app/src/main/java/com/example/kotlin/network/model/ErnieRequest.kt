package com.example.kotlin.network.model

import com.google.gson.annotations.SerializedName

/**
 * ERNIE API 请求数据模型（新版千帆API）
 */
data class ErnieRequest(
    @SerializedName("model")
    val model: String = "ernie-4.0-8k",
    
    @SerializedName("messages")
    val messages: List<Message>,
    
    @SerializedName("temperature")
    val temperature: Double = 0.95,
    
    @SerializedName("top_p")
    val topP: Double = 0.8,
    
    @SerializedName("penalty_score")
    val penaltyScore: Double = 1.0,
    
    @SerializedName("max_output_tokens")
    val maxOutputTokens: Int = 2048,
    
    @SerializedName("web_search")
    val webSearch: WebSearch = WebSearch()
)

/**
 * 网络搜索配置
 */
data class WebSearch(
    @SerializedName("enable")
    val enable: Boolean = false,
    
    @SerializedName("enable_citation")
    val enableCitation: Boolean = false,
    
    @SerializedName("enable_trace")
    val enableTrace: Boolean = false
)

/**
 * 消息数据模型
 */
data class Message(
    @SerializedName("role")
    val role: String, // "user", "assistant", "system"
    
    @SerializedName("content")
    val content: String
)

/**
 * 访问令牌请求模型
 */
data class AccessTokenRequest(
    @SerializedName("grant_type")
    val grantType: String = "client_credentials",
    
    @SerializedName("client_id")
    val clientId: String,
    
    @SerializedName("client_secret")
    val clientSecret: String
) 