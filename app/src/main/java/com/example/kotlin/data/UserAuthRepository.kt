package com.example.kotlin.data

import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

/**
 * 用户认证仓库
 */
class UserAuthRepository(private val userAuthDao: UserAuthDao) {
    
    /**
     * 获取当前登录用户
     */
    fun getCurrentLoggedInUser(): Flow<UserAuthEntity?> {
        return userAuthDao.getCurrentLoggedInUser()
    }
    
    /**
     * 根据用户ID获取用户认证信息
     */
    suspend fun getUserAuthById(userId: String): UserAuthEntity? {
        return userAuthDao.getUserAuthById(userId)
    }
    
    /**
     * 根据用户名获取用户认证信息
     */
    suspend fun getUserAuthByUsername(username: String): UserAuthEntity? {
        return userAuthDao.getUserAuthByUsername(username)
    }
    
    /**
     * 根据邮箱获取用户认证信息
     */
    suspend fun getUserAuthByEmail(email: String): UserAuthEntity? {
        return userAuthDao.getUserAuthByEmail(email)
    }
    
    /**
     * 根据手机号获取用户认证信息
     */
    suspend fun getUserAuthByPhone(phone: String): UserAuthEntity? {
        return userAuthDao.getUserAuthByPhone(phone)
    }
    
    /**
     * 根据微信OpenID获取用户认证信息
     */
    suspend fun getUserAuthByWechatOpenId(openId: String): UserAuthEntity? {
        return userAuthDao.getUserAuthByWechatOpenId(openId)
    }
    
    /**
     * 注册新用户
     */
    suspend fun registerUser(
        username: String,
        password: String,
        email: String? = null,
        phone: String? = null
    ): Result<UserAuthEntity> {
        return try {
            // 检查用户名是否已存在
            if (userAuthDao.checkUsernameExists(username) > 0) {
                return Result.failure(Exception("用户名已存在"))
            }
            
            // 检查邮箱是否已存在
            if (email != null && userAuthDao.checkEmailExists(email) > 0) {
                return Result.failure(Exception("邮箱已被注册"))
            }
            
            // 检查手机号是否已存在
            if (phone != null && userAuthDao.checkPhoneExists(phone) > 0) {
                return Result.failure(Exception("手机号已被注册"))
            }
            
            // 创建新用户
            val userId = generateUserId()
            val hashedPassword = hashPassword(password)
            val userAuth = UserAuthEntity(
                userId = userId,
                username = username,
                email = email,
                phone = phone,
                password = hashedPassword,
                loginType = "local",
                isLoggedIn = true
            )
            
            userAuthDao.insertOrUpdateUserAuth(userAuth)
            Result.success(userAuth)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 用户名密码登录
     */
    suspend fun loginWithUsername(username: String, password: String): Result<UserAuthEntity> {
        return try {
            val userAuth = userAuthDao.getUserAuthByUsername(username)
            if (userAuth == null) {
                return Result.failure(Exception("用户不存在"))
            }
            
            if (userAuth.password != hashPassword(password)) {
                return Result.failure(Exception("密码错误"))
            }
            
            // 更新登录状态
            userAuthDao.updateLoginStatus(userAuth.userId, true)
            val updatedUserAuth = userAuth.copy(isLoggedIn = true, lastLoginTime = System.currentTimeMillis())
            Result.success(updatedUserAuth)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 微信登录
     */
    suspend fun loginWithWechat(openId: String, unionId: String?, nickname: String?, avatarUrl: String?): Result<UserAuthEntity> {
        return try {
            var userAuth = userAuthDao.getUserAuthByWechatOpenId(openId)
            
            if (userAuth == null) {
                // 新用户，创建账号
                val userId = generateUserId()
                val username = "wx_${openId.takeLast(8)}" // 生成用户名
                userAuth = UserAuthEntity(
                    userId = userId,
                    username = username,
                    wechatOpenId = openId,
                    wechatUnionId = unionId,
                    nickname = nickname,
                    avatarUrl = avatarUrl,
                    loginType = "wechat",
                    isLoggedIn = true
                )
                userAuthDao.insertOrUpdateUserAuth(userAuth)
            } else {
                // 更新微信信息
                userAuthDao.updateWechatInfo(userAuth.userId, openId, unionId)
                userAuthDao.updateLoginStatus(userAuth.userId, true)
                userAuth = userAuth.copy(
                    wechatOpenId = openId,
                    wechatUnionId = unionId,
                    nickname = nickname,
                    avatarUrl = avatarUrl,
                    isLoggedIn = true,
                    lastLoginTime = System.currentTimeMillis()
                )
            }
            
            Result.success(userAuth)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 登出
     */
    suspend fun logout() {
        userAuthDao.logoutAllUsers()
    }
    
    /**
     * 保存用户（用于测试登录）
     */
    suspend fun saveUser(userAuth: UserAuthEntity) {
        userAuthDao.insertOrUpdateUserAuth(userAuth)
    }
    
    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo(
        userId: String,
        username: String,
        email: String?,
        phone: String?,
        avatarUrl: String?,
        nickname: String?
    ) {
        userAuthDao.updateUserInfo(userId, username, email, phone, avatarUrl, nickname)
    }
    
    /**
     * 生成用户ID
     */
    private fun generateUserId(): String {
        return "user_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * 密码加密
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
} 