package com.example.kotlin.network.service

import com.example.kotlin.data.NewsItem
import com.example.kotlin.data.NewsType
import com.example.kotlin.network.api.NewsApiService
import com.example.kotlin.network.config.NewsNetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 新闻网络服务类
 */
class NewsNetworkService {
    
    private val newsApiService: NewsApiService = NewsNetworkConfig.getNewsApiService()
    
    // 天行API密钥（需要替换为您的实际密钥）
    private val apiKey = "7c55a959ad3b1f1d78d455d48519aec7"
    
    /**
     * 获取新闻列表
     */
    suspend fun getNewsList(newsType: NewsType, pageSize: Int = 10): NetworkResult<List<NewsItem>> = withContext(Dispatchers.IO) {
        try {
            val response = newsApiService.getNewsList(
                type = newsType.apiPath,
                key = apiKey,
                num = pageSize
            )
            
            if (response.isSuccessful) {
                val newsResponse = response.body()
                println("DEBUG: API响应: $newsResponse")
                
                if (newsResponse != null && newsResponse.code == 200) {
                    val newsList = newsResponse.result?.newsList
                    println("DEBUG: 新闻列表: $newsList")
                    println("DEBUG: 新闻列表大小: ${newsList?.size}")
                    
                    if (newsList != null) {
                        NetworkResult.Success(newsList)
                    } else {
                        NetworkResult.Error(Exception("API返回的新闻列表为空"))
                    }
                } else {
                    NetworkResult.Error(Exception("API返回错误: ${newsResponse?.message}"))
                }
            } else {
                NetworkResult.Error(Exception("HTTP错误: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            println("DEBUG: 网络请求异常: ${e.message}")
            e.printStackTrace()
            NetworkResult.Error(handleNetworkException(e))
        }
    }
    
    /**
     * 处理网络异常
     */
    private fun handleNetworkException(exception: Exception): Exception {
        return when (exception) {
            is UnknownHostException -> Exception("网络连接失败，请检查网络设置")
            is ConnectException -> Exception("无法连接到服务器，请稍后重试")
            is SocketTimeoutException -> Exception("请求超时，请检查网络连接")
            else -> Exception("网络请求失败: ${exception.message}")
        }
    }
}

/**
 * 网络结果密封类
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Exception) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
} 