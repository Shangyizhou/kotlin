package com.example.kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin.data.NewsItem
import com.example.kotlin.data.NewsType
import com.example.kotlin.network.service.NetworkResult
import com.example.kotlin.network.service.NewsNetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 新闻页面ViewModel
 */
class NewsViewModel : ViewModel() {
    
    private val networkService = NewsNetworkService()
    
    // 当前选中的新闻类型
    private val _selectedNewsType = MutableStateFlow(NewsType.IT_NEWS)
    val selectedNewsType: StateFlow<NewsType> = _selectedNewsType.asStateFlow()
    
    // 新闻列表状态
    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList: StateFlow<List<NewsItem>> = _newsList.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 是否还有更多数据
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    
    init {
        // 初始化时加载IT咨询新闻
        loadNews(NewsType.IT_NEWS, refresh = true)
    }
    
    /**
     * 切换新闻类型
     */
    fun switchNewsType(newsType: NewsType) {
        if (_selectedNewsType.value != newsType) {
            _selectedNewsType.value = newsType
            loadNews(newsType, refresh = true)
        }
    }
    
    /**
     * 加载新闻
     */
    fun loadNews(newsType: NewsType, refresh: Boolean = false) {
        if (!_hasMoreData.value && !refresh) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = networkService.getNewsList(newsType = newsType, pageSize = 10)
                
                when (result) {
                    is NetworkResult.Success -> {
                        val newNewsList = result.data
                        
                        if (refresh) {
                            _newsList.value = newNewsList
                        } else {
                            _newsList.value = _newsList.value + newNewsList
                        }
                        
                        // 检查是否还有更多数据
                        _hasMoreData.value = newNewsList.isNotEmpty()
                    }
                    
                    is NetworkResult.Error -> {
                        _errorMessage.value = result.exception.message
                    }
                    
                    NetworkResult.Loading -> {
                        // 已经在加载中
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 刷新新闻
     */
    fun refreshNews() {
        loadNews(_selectedNewsType.value, refresh = true)
    }
    
    /**
     * 加载更多新闻
     */
    fun loadMoreNews() {
        loadNews(_selectedNewsType.value, refresh = false)
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 获取新闻类型列表
     */
    fun getNewsTypes(): List<NewsType> {
        return NewsType.values().toList()
    }
} 