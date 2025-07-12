package com.example.kotlin.network.config

import com.example.kotlin.network.api.ErnieApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络配置类，负责创建和配置HTTP客户端
 */
object NetworkConfig {
    
    // 百度千帆API基础URL
    private const val BAIDU_BASE_URL = "https://qianfan.baidubce.com/"
    
    // 百度OAuth基础URL（用于获取访问令牌）
    private const val BAIDU_OAUTH_BASE_URL = "https://aip.baidubce.com/"
    
    // 连接超时时间
    private const val CONNECT_TIMEOUT = 30L
    
    // 读取超时时间
    private const val READ_TIMEOUT = 60L
    
    // 写入超时时间
    private const val WRITE_TIMEOUT = 60L
    
    /**
     * 创建HTTP日志拦截器
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * 创建通用请求头拦截器
     */
    private fun createHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Kotlin-ERNIE-Client/1.0")
            
            chain.proceed(requestBuilder.build())
        }
    }
    
    /**
     * 创建OkHttp客户端
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(createHeaderInterceptor())
            .addInterceptor(createLoggingInterceptor())
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * 创建Gson解析器
     */
    private fun createGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()
    }
    
    /**
     * 创建Retrofit实例（聊天API）
     */
    private fun createChatRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BAIDU_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    /**
     * 创建Retrofit实例（OAuth API）
     */
    private fun createOAuthRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BAIDU_OAUTH_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    /**
     * 获取ERNIE聊天API服务实例
     */
    fun getErnieChatService(): ErnieApiService {
        return createChatRetrofit().create(ErnieApiService::class.java)
    }
    
    /**
     * 获取OAuth API服务实例
     */
    fun getOAuthService(): ErnieApiService {
        return createOAuthRetrofit().create(ErnieApiService::class.java)
    }
    
    /**
     * 创建带有自定义配置的OkHttp客户端
     */
    fun createCustomOkHttpClient(
        connectTimeout: Long = CONNECT_TIMEOUT,
        readTimeout: Long = READ_TIMEOUT,
        writeTimeout: Long = WRITE_TIMEOUT,
        enableLogging: Boolean = true,
        additionalInterceptors: List<Interceptor> = emptyList()
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .addInterceptor(createHeaderInterceptor())
            .retryOnConnectionFailure(true)
        
        if (enableLogging) {
            builder.addInterceptor(createLoggingInterceptor())
        }
        
        additionalInterceptors.forEach { interceptor ->
            builder.addInterceptor(interceptor)
        }
        
        return builder.build()
    }
} 