package com.example.kotlin.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.kotlin.data.AppDatabase
import com.example.kotlin.data.UserProfileEntity
import com.example.kotlin.data.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户信息ViewModel
 */
class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {
    
    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    /**
     * 加载用户信息
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getUserProfile().collect { profile ->
                    if (profile != null) {
                        // 数据库中有记录，使用数据库中的信息
                        android.util.Log.d("UserProfileViewModel", "Loaded profile from database: ${profile.avatarUrl}")
                        _userProfile.value = profile
                    } else {
                        // 数据库中没有记录，创建默认记录
                        android.util.Log.d("UserProfileViewModel", "No profile in database, creating default")
                        val defaultProfile = getDefaultUserProfile()
                        repository.insertOrUpdateUserProfile(defaultProfile)
                        _userProfile.value = defaultProfile
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("UserProfileViewModel", "Error loading profile", e)
                _userProfile.value = getDefaultUserProfile()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新头像URL
     */
    fun updateAvatarUrl(avatarUrl: String) {
        android.util.Log.d("UserProfileViewModel", "Updating avatar URL: $avatarUrl")
        viewModelScope.launch {
            try {
                // 先更新数据库
                repository.updateAvatarUrl("default_user", avatarUrl)
                android.util.Log.d("UserProfileViewModel", "Database updated successfully")
                
                // 如果当前没有用户信息，先创建默认的
                val currentProfile = _userProfile.value
                if (currentProfile == null) {
                    val defaultProfile = getDefaultUserProfile().copy(avatarUrl = avatarUrl)
                    repository.insertOrUpdateUserProfile(defaultProfile)
                    _userProfile.value = defaultProfile
                    android.util.Log.d("UserProfileViewModel", "Created new profile with avatar")
                } else {
                    // 更新本地状态
                    _userProfile.value = currentProfile.copy(avatarUrl = avatarUrl)
                    android.util.Log.d("UserProfileViewModel", "Updated existing profile with avatar")
                }
            } catch (e: Exception) {
                // 处理错误
                android.util.Log.e("UserProfileViewModel", "Error updating avatar", e)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 更新用户基本信息
     */
    fun updateUserInfo(name: String, birthDate: String, zodiac: String, signature: String) {
        viewModelScope.launch {
            try {
                repository.updateUserInfo("default_user", name, birthDate, zodiac, signature)
                // 更新本地状态
                _userProfile.value = _userProfile.value?.copy(
                    name = name,
                    birthDate = birthDate,
                    zodiac = zodiac,
                    signature = signature
                )
            } catch (e: Exception) {
                // 处理错误
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 获取默认用户信息
     */
    private fun getDefaultUserProfile(): UserProfileEntity {
        return UserProfileEntity(
            userId = "default_user",
            name = "张三",
            birthDate = "1995年3月15日",
            zodiac = "双鱼座",
            signature = "热爱生活，追求梦想"
        )
    }
    
    /**
     * 初始化默认用户信息（仅在数据库为空时使用）
     */
    fun initializeDefaultProfile() {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfile.value
                if (currentProfile == null) {
                    android.util.Log.d("UserProfileViewModel", "Initializing default profile")
                    val defaultProfile = getDefaultUserProfile()
                    repository.insertOrUpdateUserProfile(defaultProfile)
                    _userProfile.value = defaultProfile
                } else {
                    android.util.Log.d("UserProfileViewModel", "Profile already exists, skipping initialization")
                }
            } catch (e: Exception) {
                // 处理错误
                android.util.Log.e("UserProfileViewModel", "Error initializing profile", e)
                e.printStackTrace()
            }
        }
    }
}

/**
 * ViewModel工厂
 */
class UserProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = UserProfileRepository(database.userProfileDao())
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 