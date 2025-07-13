package com.example.kotlin.network.api

import com.example.kotlin.data.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 新闻API服务接口
 */
interface NewsApiService {
    
    /**
     * 获取新闻列表
     * @param type 新闻类型路径 (it/keji/vr)
     * @param key API密钥
     * @param num 每页数量，默认10
     */
    @GET("{type}/index")
    suspend fun getNewsList(
        @Path("type") type: String,
        @Query("key") key: String,
        @Query("num") num: Int = 10
    ): Response<NewsResponse>
} 