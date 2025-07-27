package com.example.kotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 用户认证数据访问对象
 */
@Dao
interface UserAuthDao {
    
    /**
     * 根据用户ID获取用户认证信息
     */
    @Query("SELECT * FROM user_auth WHERE userId = :userId")
    suspend fun getUserAuthById(userId: String): UserAuthEntity?
    
    /**
     * 根据用户名获取用户认证信息
     */
    @Query("SELECT * FROM user_auth WHERE username = :username")
    suspend fun getUserAuthByUsername(username: String): UserAuthEntity?
    
    /**
     * 根据邮箱获取用户认证信息
     */
    @Query("SELECT * FROM user_auth WHERE email = :email")
    suspend fun getUserAuthByEmail(email: String): UserAuthEntity?
    
    /**
     * 根据手机号获取用户认证信息
     */
    @Query("SELECT * FROM user_auth WHERE phone = :phone")
    suspend fun getUserAuthByPhone(phone: String): UserAuthEntity?
    
    /**
     * 根据微信OpenID获取用户认证信息
     */
    @Query("SELECT * FROM user_auth WHERE wechatOpenId = :openId")
    suspend fun getUserAuthByWechatOpenId(openId: String): UserAuthEntity?
    
    /**
     * 获取当前登录用户
     */
    @Query("SELECT * FROM user_auth WHERE isLoggedIn = 1 LIMIT 1")
    fun getCurrentLoggedInUser(): Flow<UserAuthEntity?>
    
    /**
     * 插入或更新用户认证信息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserAuth(userAuth: UserAuthEntity)
    
    /**
     * 更新用户登录状态
     */
    @Query("UPDATE user_auth SET isLoggedIn = :isLoggedIn, lastLoginTime = :lastLoginTime, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateLoginStatus(userId: String, isLoggedIn: Boolean, lastLoginTime: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新用户信息
     */
    @Query("UPDATE user_auth SET username = :username, email = :email, phone = :phone, avatarUrl = :avatarUrl, nickname = :nickname, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateUserInfo(
        userId: String,
        username: String,
        email: String?,
        phone: String?,
        avatarUrl: String?,
        nickname: String?,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 更新微信信息
     */
    @Query("UPDATE user_auth SET wechatOpenId = :openId, wechatUnionId = :unionId, loginType = 'wechat', updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateWechatInfo(userId: String, openId: String, unionId: String?, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 登出所有用户
     */
    @Query("UPDATE user_auth SET isLoggedIn = 0, updatedAt = :updatedAt")
    suspend fun logoutAllUsers(updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 删除用户认证信息
     */
    @Query("DELETE FROM user_auth WHERE userId = :userId")
    suspend fun deleteUserAuth(userId: String)
    
    /**
     * 检查用户名是否存在
     */
    @Query("SELECT COUNT(*) FROM user_auth WHERE username = :username")
    suspend fun checkUsernameExists(username: String): Int
    
    /**
     * 检查邮箱是否存在
     */
    @Query("SELECT COUNT(*) FROM user_auth WHERE email = :email")
    suspend fun checkEmailExists(email: String): Int
    
    /**
     * 检查手机号是否存在
     */
    @Query("SELECT COUNT(*) FROM user_auth WHERE phone = :phone")
    suspend fun checkPhoneExists(phone: String): Int
} 