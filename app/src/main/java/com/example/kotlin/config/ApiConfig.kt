package com.example.kotlin.config

/**
 * API配置类
 * 请在这里配置你的百度API凭据
 */
object ApiConfig {
    
    /**
     * 百度API Key (Client ID)
     * 获取方式：
     * 1. 访问 https://console.bce.baidu.com/
     * 2. 创建应用
     * 3. 获取 API Key 和 Secret Key
     */
    const val BAIDU_API_KEY = "27fhgYXGXRFg0KRv1zehNPYI"  // 替换为你的API Key
    
    /**
     * 百度Secret Key (Client Secret)
     */
    const val BAIDU_SECRET_KEY = "6vLQhMbzNOqXfoRcVnNB33iynoHktvE6"  // 替换为你的Secret Key
    
    /**
     * 检查API密钥是否已配置
     */
    fun isApiConfigured(): Boolean {
        return BAIDU_API_KEY != "YOUR_API_KEY" && 
               BAIDU_SECRET_KEY != "YOUR_SECRET_KEY" &&
               BAIDU_API_KEY.isNotBlank() && 
               BAIDU_SECRET_KEY.isNotBlank()
    }
    
    /**
     * 获取API配置状态消息
     */
    fun getConfigStatusMessage(): String {
        return if (isApiConfigured()) {
            "API配置已完成"
        } else {
            "请在 ApiConfig.kt 中配置你的百度API密钥"
        }
    }
} 