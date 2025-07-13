package com.example.kotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户信息实体
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String = "default_user", // 默认用户ID
    val avatarUrl: String? = null, // 头像URL
    val name: String = "张三", // 用户名
    val birthDate: String = "1995年3月15日", // 出生日期
    val zodiac: String = "双鱼座", // 星座
    val signature: String = "热爱生活，追求梦想", // 个性签名
    val updatedAt: Long = System.currentTimeMillis() // 更新时间
) 