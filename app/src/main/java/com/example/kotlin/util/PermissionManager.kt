package com.example.kotlin.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 权限管理器
 */
object PermissionManager {
    
    /**
     * 检查是否有读取图片的权限
     */
    fun hasImagePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取需要的权限列表
     */
    fun getImagePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 获取存储权限
     */
    fun getStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 获取位置权限
     */
    fun getLocationPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    /**
     * 获取相机权限
     */
    fun getCameraPermissions(): Array<String> {
        return arrayOf(Manifest.permission.CAMERA)
    }
}

/**
 * 权限状态
 */
@Composable
fun rememberPermissionState(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
): MutableState<Boolean> {
    val hasPermission = remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermission.value = allGranted
        
        if (allGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }
    
    return hasPermission
}

/**
 * Fragment扩展函数，使用ActivityResultLauncher申请权限
 */
class FragmentPermissionHelper(private val fragment: Fragment) {
    
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var onResult: ((Map<String, Boolean>) -> Unit)? = null
    
    init {
        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            onResult?.invoke(permissions)
        }
    }
    
    /**
     * 申请权限
     */
    fun requestPermissions(
        permissions: Array<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ) {
        this.onResult = onResult
        permissionLauncher?.launch(permissions)
    }
    
    /**
     * 申请单个权限
     */
    fun requestPermission(
        permission: String,
        onResult: (Boolean) -> Unit
    ) {
        requestPermissions(arrayOf(permission)) { results ->
            onResult(results[permission] == true)
        }
    }
    
    /**
     * 协程方式申请权限
     */
    suspend fun requestPermissionsAsync(permissions: Array<String>): Map<String, Boolean> {
        return suspendCancellableCoroutine { continuation ->
            requestPermissions(permissions) { result ->
                continuation.resume(result)
            }
        }
    }
}

/**
 * 使用示例扩展函数
 */

/**
 * Fragment扩展函数：快速申请相机权限
 */
suspend fun Fragment.requestCameraPermission(): Boolean {
    val helper = FragmentPermissionHelper(this)
    return helper.requestPermissionsAsync(PermissionManager.getCameraPermissions())
        .values.all { it }
}

/**
 * Fragment扩展函数：快速申请存储权限
 */
suspend fun Fragment.requestStoragePermission(): Boolean {
    val helper = FragmentPermissionHelper(this)
    return helper.requestPermissionsAsync(PermissionManager.getStoragePermissions())
        .values.all { it }
}

/**
 * Fragment扩展函数：快速申请位置权限
 */
suspend fun Fragment.requestLocationPermission(): Boolean {
    val helper = FragmentPermissionHelper(this)
    return helper.requestPermissionsAsync(PermissionManager.getLocationPermissions())
        .values.all { it }
} 