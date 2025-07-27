package com.example.kotlin.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotlin.data.AppDatabase
import com.example.kotlin.data.UserAuthEntity
import com.example.kotlin.data.UserAuthRepository
import com.example.kotlin.network.service.WechatLoginService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户认证ViewModel
 */
class UserAuthViewModel(
    private val repository: UserAuthRepository,
    private val wechatLoginService: WechatLoginService
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<UserAuthEntity?>(null)
    val currentUser: StateFlow<UserAuthEntity?> = _currentUser.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    /**
     * 加载当前用户
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentLoggedInUser().collect { user ->
                _currentUser.value = user
                _isLoggedIn.value = user != null
            }
        }
    }
    
    /**
     * 注册用户
     */
    fun registerUser(
        username: String,
        password: String,
        email: String? = null,
        phone: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = repository.registerUser(username, password, email, phone)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isLoggedIn.value = true
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "注册失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 用户名密码登录
     */
    fun loginWithUsername(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = repository.loginWithUsername(username, password)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isLoggedIn.value = true
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "登录失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 微信登录
     */
    fun loginWithWechat() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // 检查微信是否可用
                if (!wechatLoginService.isWechatInstalled()) {
                    _errorMessage.value = "微信未安装，请先安装微信"
                    return@launch
                }
                
                if (!wechatLoginService.isWechatSupported()) {
                    _errorMessage.value = "微信版本不支持，请更新微信"
                    return@launch
                }
                
                // 发起微信登录
                val wechatResult = wechatLoginService.loginWithWechat()
                wechatResult.fold(
                    onSuccess = { wechatData ->
                        // 微信登录成功，保存到本地数据库
                        val result = repository.loginWithWechat(
                            wechatData.openId,
                            wechatData.unionId,
                            wechatData.nickname,
                            wechatData.avatarUrl
                        )
                        result.fold(
                            onSuccess = { user ->
                                _currentUser.value = user
                                _isLoggedIn.value = true
                            },
                            onFailure = { exception ->
                                _errorMessage.value = exception.message
                            }
                        )
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "微信登录失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 处理微信登录回调
     */
    fun handleWechatResponse(intent: android.content.Intent) {
        wechatLoginService.handleWechatResponse(intent)
    }
    
    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
                _currentUser.value = null
                _isLoggedIn.value = false
            } catch (e: Exception) {
                _errorMessage.value = "登出失败: ${e.message}"
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 设置测试登录状态（跳过验证）
     */
    fun setTestLoginState() {
        viewModelScope.launch {
            try {
                // 创建一个测试用户
                val testUser = UserAuthEntity(
                    userId = "test_user_${System.currentTimeMillis()}",
                    username = "测试用户",
                    email = "test@example.com",
                    phone = "13800138000",
                    password = null, // 测试用户不需要密码
                    wechatOpenId = null,
                    wechatUnionId = null,
                    avatarUrl = null,
                    nickname = "测试用户",
                    loginType = "test", // 标记为测试登录
                    isLoggedIn = true,
                    lastLoginTime = System.currentTimeMillis()
                )
                
                // 保存测试用户到数据库
                repository.saveUser(testUser)
                
                // 更新状态
                _currentUser.value = testUser
                _isLoggedIn.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "测试登录失败: ${e.message}"
            }
        }
    }
    
    /**
     * 更新用户信息
     */
    fun updateUserInfo(
        username: String,
        email: String?,
        phone: String?,
        avatarUrl: String?,
        nickname: String?
    ) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    repository.updateUserInfo(
                        currentUser.userId,
                        username,
                        email,
                        phone,
                        avatarUrl,
                        nickname
                    )
                    // 更新本地状态
                    _currentUser.value = currentUser.copy(
                        username = username,
                        email = email,
                        phone = phone,
                        avatarUrl = avatarUrl,
                        nickname = nickname
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "更新用户信息失败: ${e.message}"
            }
        }
    }
}

/**
 * ViewModel工厂
 */
class UserAuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserAuthViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = UserAuthRepository(database.userAuthDao())
            val wechatLoginService = WechatLoginService(context)
            @Suppress("UNCHECKED_CAST")
            return UserAuthViewModel(repository, wechatLoginService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 