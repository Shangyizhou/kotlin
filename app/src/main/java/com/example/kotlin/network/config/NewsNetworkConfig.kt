package com.example.kotlin.network.config

import com.example.kotlin.network.api.NewsApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 新闻网络配置类
 */
object NewsNetworkConfig {
    
    // 天行API基础URL
    private const val TIANAPI_BASE_URL = "https://apis.tianapi.com/"
    
    // 连接超时时间
    private const val CONNECT_TIMEOUT = 30L
    
    // 读取超时时间
    private const val READ_TIMEOUT = 60L
    
    /**
     * 创建HTTP日志拦截器
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * 创建OkHttp客户端
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
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
     * 创建Retrofit实例
     */
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TIANAPI_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    /**
     * 获取新闻API服务实例
     */
    fun getNewsApiService(): NewsApiService {
        return createRetrofit().create(NewsApiService::class.java)
    }
} 