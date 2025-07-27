package com.example.kotlin

import AppNavigation
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlin.ui.theme.KotlinTheme
import com.example.kotlin.viewmodel.UserAuthViewModel
import com.example.kotlin.viewmodel.UserAuthViewModelFactory

class MainActivity : ComponentActivity() {
    private val TAG: String = this::class.simpleName ?: "MainActivity"
    
    private lateinit var userAuthViewModel: UserAuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            KotlinTheme {
                // 在Composable中初始化ViewModel
                val userAuthViewModel: UserAuthViewModel = viewModel(factory = UserAuthViewModelFactory(this))
                AppNavigation()
            }
        }
    }
    
    // 处理微信登录回调的方法，需要在Activity中实现
    fun handleWechatResponse(intent: Intent) {
        // 这里可以通过其他方式获取ViewModel实例
        // 暂时注释掉，等Navigation中处理
    }
}