package com.example.kotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户认证实体
 */
@Entity(tableName = "user_auth")
data class UserAuthEntity(
    @PrimaryKey
    val userId: String,
    
    val username: String, // 用户名
    
    val email: String? = null, // 邮箱
    
    val phone: String? = null, // 手机号
    
    val password: String? = null, // 密码（加密存储）
    
    val wechatOpenId: String? = null, // 微信OpenID
    
    val wechatUnionId: String? = null, // 微信UnionID
    
    val avatarUrl: String? = null, // 头像URL
    
    val nickname: String? = null, // 昵称
    
    val loginType: String = "local", // 登录类型：local(本地), wechat(微信), phone(手机)
    
    val isLoggedIn: Boolean = false, // 是否已登录
    
    val lastLoginTime: Long = System.currentTimeMillis(), // 最后登录时间
    
    val createdAt: Long = System.currentTimeMillis(), // 创建时间
    
    val updatedAt: Long = System.currentTimeMillis() // 更新时间
) 