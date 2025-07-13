package com.example.kotlin.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * 应用权限检查器
 * 在应用启动时统一检查和申请必要权限
 */
class AppPermissionChecker(private val fragment: Fragment) {
    
    private val permissionHelper = FragmentPermissionHelper(fragment)
    
    /**
     * 执行应用启动权限检查
     * @param onAllPermissionsGranted 所有权限授予后的回调
     * @param onPermissionDenied 权限被拒绝后的回调
     */
    fun checkAppPermissions(
        onAllPermissionsGranted: () -> Unit,
        onPermissionDenied: (List<String>) -> Unit = {}
    ) {
        fragment.lifecycleScope.launch {
            // 检查网络状态（虽然不需要动态申请，但可以检查网络可用性）
            if (!NetworkUtils.isNetworkAvailable(fragment.requireContext())) {
                showNetworkDialog()
                return@launch
            }
            
            // 定义应用需要的危险权限（根据实际需求调整）
            val requiredPermissions = getRequiredPermissions()
            
            if (requiredPermissions.isEmpty()) {
                // 没有需要申请的权限，直接执行成功回调
                onAllPermissionsGranted()
                return@launch
            }
            
            // 检查是否已经拥有所有权限
            val deniedPermissions = getDeniedPermissions(
                fragment.requireContext(), 
                requiredPermissions
            )
            
            if (deniedPermissions.isEmpty()) {
                // 所有权限都已授予
                onAllPermissionsGranted()
                return@launch
            }
            
            // 检查是否需要显示权限说明
            val shouldShowRationale = deniedPermissions.any { permission ->
                shouldShowRequestPermissionRationale(
                    fragment.requireActivity(), 
                    permission
                )
            }
            
            if (shouldShowRationale) {
                // 显示权限说明对话框
                showPermissionRationaleDialog(deniedPermissions) {
                    fragment.lifecycleScope.launch {
                        requestPermissions(requiredPermissions, onAllPermissionsGranted, onPermissionDenied)
                    }
                }
            } else {
                // 直接申请权限
                requestPermissions(requiredPermissions, onAllPermissionsGranted, onPermissionDenied)
            }
        }
    }
    
    /**
     * 获取被拒绝的权限列表
     */
    private fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查是否应该显示权限说明
     */
    private fun shouldShowRequestPermissionRationale(activity: android.app.Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * 获取应用需要的危险权限
     * 根据你的应用功能调整这个列表
     */
    private fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        
        // 示例：如果应用需要相机功能
        // permissions.add(Manifest.permission.CAMERA)
        
        // 示例：如果应用需要访问存储
        // permissions.addAll(PermissionManager.getStoragePermissions())
        
        // 示例：如果应用需要位置服务
        // permissions.addAll(PermissionManager.getLocationPermissions())
        
        // 示例：如果应用需要通知权限（Android 13+）
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //     permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        // }
        
        return permissions.toTypedArray()
    }
    
    /**
     * 申请权限
     */
    private suspend fun requestPermissions(
        permissions: Array<String>,
        onAllGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        val result = permissionHelper.requestPermissionsAsync(permissions)
        val deniedPermissions = result.filter { !it.value }.keys.toList()
        
        if (deniedPermissions.isEmpty()) {
            onAllGranted()
        } else {
            // 检查是否有权限被永久拒绝
            val permanentlyDenied = deniedPermissions.filter { permission ->
                !shouldShowRequestPermissionRationale(
                    fragment.requireActivity(), 
                    permission
                )
            }
            
            if (permanentlyDenied.isNotEmpty()) {
                showPermanentlyDeniedDialog(permanentlyDenied)
            } else {
                onDenied(deniedPermissions)
            }
        }
    }
    
    /**
     * 显示权限说明对话框
     */
    private fun showPermissionRationaleDialog(
        permissions: List<String>,
        onConfirm: () -> Unit
    ) {
        val dialog = CommonDialog.newInstance(
            title = "权限申请",
            message = "为了正常使用应用功能，需要申请以下权限：\n${getPermissionDescription(permissions)}",
            positiveText = "授权",
            negativeText = "取消"
        )
        
        dialog.onPositiveClick = onConfirm
        dialog.onNegativeClick = {
            // 用户拒绝授权，可以选择退出应用或限制功能
            showPermissionDeniedDialog()
        }
        
        dialog.show(fragment.parentFragmentManager, "permission_rationale")
    }
    
    /**
     * 显示权限被永久拒绝的对话框
     */
    private fun showPermanentlyDeniedDialog(permissions: List<String>) {
        val dialog = CommonDialog.newInstance(
            title = "权限被拒绝",
            message = "应用需要以下权限才能正常运行：\n${getPermissionDescription(permissions)}\n\n请到设置中手动开启权限。",
            positiveText = "去设置",
            negativeText = "取消"
        )
        
        dialog.onPositiveClick = {
            openAppSettings()
        }
        
        dialog.show(fragment.parentFragmentManager, "permission_permanently_denied")
    }
    
    /**
     * 显示权限被拒绝的对话框
     */
    private fun showPermissionDeniedDialog() {
        val dialog = CommonDialog.newInstance(
            title = "权限被拒绝",
            message = "没有必要权限，部分功能可能无法正常使用。",
            positiveText = "确定"
        )
        
        dialog.show(fragment.parentFragmentManager, "permission_denied")
    }
    
    /**
     * 显示网络连接对话框
     */
    private fun showNetworkDialog() {
        val dialog = CommonDialog.newInstance(
            title = "网络连接",
            message = "应用需要网络连接才能正常使用，请检查网络设置。",
            positiveText = "去设置",
            negativeText = "取消"
        )
        
        dialog.onPositiveClick = {
            openNetworkSettings()
        }
        
        dialog.show(fragment.parentFragmentManager, "network_dialog")
    }
    
    /**
     * 打开应用设置页面
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", fragment.requireContext().packageName, null)
        }
        fragment.startActivity(intent)
    }
    
    /**
     * 打开网络设置页面
     */
    private fun openNetworkSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        fragment.startActivity(intent)
    }
    
    /**
     * 获取权限描述文本
     */
    private fun getPermissionDescription(permissions: List<String>): String {
        return permissions.joinToString("\n") { permission ->
            when (permission) {
                Manifest.permission.CAMERA -> "• 相机权限 - 用于拍照和扫码"
                Manifest.permission.RECORD_AUDIO -> "• 录音权限 - 用于语音功能"
                Manifest.permission.ACCESS_FINE_LOCATION -> "• 位置权限 - 用于定位服务"
                Manifest.permission.ACCESS_COARSE_LOCATION -> "• 位置权限 - 用于定位服务"
                Manifest.permission.READ_EXTERNAL_STORAGE -> "• 存储权限 - 用于读取文件"
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> "• 存储权限 - 用于保存文件"
                Manifest.permission.READ_MEDIA_IMAGES -> "• 图片权限 - 用于访问图片"
                Manifest.permission.READ_MEDIA_VIDEO -> "• 视频权限 - 用于访问视频"
                Manifest.permission.READ_MEDIA_AUDIO -> "• 音频权限 - 用于访问音频"
                Manifest.permission.POST_NOTIFICATIONS -> "• 通知权限 - 用于推送消息"
                else -> "• ${permission.substringAfterLast(".")}"
            }
        }
    }
}

/**
 * 网络工具类
 */
object NetworkUtils {
    
    /**
     * 检查网络是否可用
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as android.net.ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo?.isConnected == true
        }
    }
} 