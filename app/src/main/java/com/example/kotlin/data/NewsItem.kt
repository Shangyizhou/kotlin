package com.example.kotlin.data

import com.google.gson.annotations.SerializedName

/**
 * IT咨询新闻数据模型
 */
data class NewsItem(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("ctime")
    val publishTime: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("source")
    val source: String,
    
    @SerializedName("picUrl")
    val imageUrl: String,
    
    @SerializedName("url")
    val articleUrl: String
)

/**
 * 天行API响应模型
 */
data class NewsResponse(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("msg")
    val message: String,
    
    @SerializedName("result")
    val result: NewsResult
)

data class NewsResult(
    @SerializedName("curpage")
    val currentPage: Int,
    
    @SerializedName("allnum")
    val totalCount: Int,
    
    @SerializedName("newslist")
    val newsList: List<NewsItem>
)

/**
 * 新闻类型枚举
 */
enum class NewsType(val apiPath: String, val displayName: String) {
    IT_NEWS("it", "IT咨询"),
    TECH_NEWS("keji", "科技新闻"),
    VR_TECH("vr", "VR科技")
} 