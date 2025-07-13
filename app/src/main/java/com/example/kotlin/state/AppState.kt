package com.example.kotlin.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.kotlin.data.NewsItem

/**
 * 全局应用状态管理
 */
object AppState {
    
    // 导航事件
    var navigationEvent by mutableStateOf<NavigationEvent?>(null)
        private set
    
    // 新闻数据缓存
    private var newsDataCache by mutableStateOf<Map<String, NewsItem>>(emptyMap())
    
    /**
     * 触发导航事件
     */
    fun navigateTo(event: NavigationEvent) {
        navigationEvent = event
    }
    
    /**
     * 清除导航事件
     */
    fun clearNavigationEvent() {
        navigationEvent = null
    }
    
    /**
     * 缓存新闻数据
     */
    fun cacheNewsData(newsItem: NewsItem) {
        newsDataCache = newsDataCache + (newsItem.id to newsItem)
    }
    
    /**
     * 获取缓存的新闻数据
     */
    fun getCachedNewsData(newsId: String): NewsItem? {
        return newsDataCache[newsId]
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        newsDataCache = emptyMap()
    }
}

/**
 * 导航事件密封类
 */
sealed class NavigationEvent {
    data class NavigateToNewsDetail(val newsItem: NewsItem) : NavigationEvent()
    object NavigateBack : NavigationEvent()
} 