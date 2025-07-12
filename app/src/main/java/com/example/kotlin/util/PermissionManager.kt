package com.example.kotlin.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 动态权限申请管理工具类
 * 用于处理Android 6.0+的运行时权限申请
 */
class PermissionManager {
    
    companion object {
        
        /**
         * 常用危险权限定义
         */
        object Permissions {
            const val CAMERA = Manifest.permission.CAMERA
            const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
            const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
            const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
            const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
            const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
            const val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
            const val CALL_PHONE = Manifest.permission.CALL_PHONE
            const val SEND_SMS = Manifest.permission.SEND_SMS
            const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
            
            // Android 13+ 新增权限
            var READ_MEDIA_IMAGES = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                READ_EXTERNAL_STORAGE
            }
            val READ_MEDIA_VIDEO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                READ_EXTERNAL_STORAGE
            }
            val READ_MEDIA_AUDIO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                READ_EXTERNAL_STORAGE
            }
            val POST_NOTIFICATIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else {
                ""
            }
        }
        
        /**
         * 检查单个权限是否已授予
         */
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        /**
         * 检查多个权限是否都已授予
         */
        fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
            return permissions.all { isPermissionGranted(context, it) }
        }
        
        /**
         * 获取未授予的权限列表
         */
        fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
            return permissions.filter { !isPermissionGranted(context, it) }
        }
        
        /**
         * 检查权限是否应该显示解释对话框
         */
        fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
        
        /**
         * 在Activity中申请权限
         */
        fun requestPermissions(
            activity: Activity,
            permissions: Array<String>,
            requestCode: Int
        ) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
        
        /**
         * 处理权限申请结果
         */
        fun handlePermissionResult(
            grantResults: IntArray,
            onAllGranted: () -> Unit,
            onDenied: (deniedPermissions: List<String>) -> Unit,
            permissions: Array<String>
        ) {
            val deniedPermissions = mutableListOf<String>()
            
            grantResults.forEachIndexed { index, result ->
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[index])
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                onAllGranted()
            } else {
                onDenied(deniedPermissions)
            }
        }
        
        /**
         * 获取适合当前Android版本的存储权限
         */
        fun getStoragePermissions(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Permissions.READ_MEDIA_IMAGES,
                    Permissions.READ_MEDIA_VIDEO,
                    Permissions.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(
                    Permissions.READ_EXTERNAL_STORAGE,
                    Permissions.WRITE_EXTERNAL_STORAGE
                )
            }
        }
        
        /**
         * 获取位置权限
         */
        fun getLocationPermissions(): Array<String> {
            return arrayOf(
                Permissions.ACCESS_FINE_LOCATION,
                Permissions.ACCESS_COARSE_LOCATION
            )
        }
    }
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
    return helper.requestPermissionsAsync(arrayOf(PermissionManager.Companion.Permissions.CAMERA))
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