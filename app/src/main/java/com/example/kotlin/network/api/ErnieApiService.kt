package com.example.kotlin.network.api

import com.example.kotlin.network.model.AccessTokenResponse
import com.example.kotlin.network.model.ErnieRequest
import com.example.kotlin.network.model.ErnieResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 百度千帆ERNIE API服务接口
 */
interface ErnieApiService {
    
    /**
     * 获取访问令牌（仍使用原接口）
     */
    @POST("oauth/2.0/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<AccessTokenResponse>
    
    /**
     * 发送聊天消息到ERNIE-4.0-8K模型（新版千帆API）
     * URL: /v2/chat/completions?chat-ernie-4.0-8k
     */
    @POST("v2/chat/completions")
    suspend fun chatCompletion(
        @Query("chat-ernie-4.0-8k") model: String? = null,
        @Header("Authorization") authorization: String,
        @Body request: ErnieRequest
    ): Response<ErnieResponse>
    
    /**
     * 流式聊天接口（新版千帆API）
     */
    @POST("v2/chat/completions")
    @Streaming
    suspend fun chatCompletionStream(
        @Query("chat-ernie-4.0-8k") model: String? = null,
        @Header("Authorization") authorization: String,
        @Body request: ErnieRequest
    ): Response<okhttp3.ResponseBody>
} 