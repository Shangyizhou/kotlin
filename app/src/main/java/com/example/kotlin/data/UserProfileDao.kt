package com.example.kotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 用户信息数据访问对象
 */
@Dao
interface UserProfileDao {
    
    /**
     * 获取用户信息
     */
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfile(userId: String = "default_user"): Flow<UserProfileEntity?>
    
    /**
     * 插入或更新用户信息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(userProfile: UserProfileEntity)
    
    /**
     * 更新头像URL
     */
    @Query("UPDATE user_profile SET avatarUrl = :avatarUrl, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateAvatarUrl(userId: String, avatarUrl: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新用户基本信息
     */
    @Query("UPDATE user_profile SET name = :name, birthDate = :birthDate, zodiac = :zodiac, signature = :signature, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateUserInfo(
        userId: String,
        name: String,
        birthDate: String,
        zodiac: String,
        signature: String,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 删除用户信息
     */
    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String = "default_user")
} 