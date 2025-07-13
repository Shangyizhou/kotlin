package com.example.kotlin.data

import kotlinx.coroutines.flow.Flow

/**
 * 用户信息仓库
 */
class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    
    /**
     * 获取用户信息
     */
    fun getUserProfile(userId: String = "default_user"): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfile(userId)
    }
    
    /**
     * 插入或更新用户信息
     */
    suspend fun insertOrUpdateUserProfile(userProfile: UserProfileEntity) {
        userProfileDao.insertOrUpdateUserProfile(userProfile)
    }
    
    /**
     * 更新头像URL
     */
    suspend fun updateAvatarUrl(userId: String, avatarUrl: String) {
        userProfileDao.updateAvatarUrl(userId, avatarUrl)
    }
    
    /**
     * 更新用户基本信息
     */
    suspend fun updateUserInfo(
        userId: String,
        name: String,
        birthDate: String,
        zodiac: String,
        signature: String
    ) {
        userProfileDao.updateUserInfo(userId, name, birthDate, zodiac, signature)
    }
    
    /**
     * 删除用户信息
     */
    suspend fun deleteUserProfile(userId: String = "default_user") {
        userProfileDao.deleteUserProfile(userId)
    }
} 