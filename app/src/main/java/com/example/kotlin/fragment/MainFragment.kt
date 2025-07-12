package com.example.kotlin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.kotlin.databinding.FragmentMainBinding
import com.example.kotlin.network.example.ErnieNetworkExample
import com.example.kotlin.util.AppPermissionChecker
import com.example.kotlin.util.requestCameraPermission
import com.example.kotlin.util.requestStoragePermission
import kotlinx.coroutines.launch

/**
 * 主Fragment - 展示权限检查和网络库使用示例
 */
class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var permissionChecker: AppPermissionChecker
    private lateinit var networkExample: ErnieNetworkExample
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化权限检查器
        permissionChecker = AppPermissionChecker(this)
        
        // 初始化网络示例
        networkExample = ErnieNetworkExample()
        
        // 开始权限检查
        startPermissionCheck()
        
        // 设置点击事件（可选）
        setupClickListeners()
    }
    
    /**
     * 开始权限检查流程
     */
    private fun startPermissionCheck() {
        binding.tvStatus.text = "🔄 正在检查权限..."
        
        permissionChecker.checkAppPermissions(
            onAllPermissionsGranted = {
                // 所有权限都已授予，应用可以正常运行
                onPermissionsGranted()
            },
            onPermissionDenied = { deniedPermissions ->
                // 有权限被拒绝，可以选择限制功能或显示提示
                onPermissionsDenied(deniedPermissions)
            }
        )
    }
    
    /**
     * 权限授予成功后的处理
     */
    private fun onPermissionsGranted() {
        binding.tvStatus.text = "✅ 权限检查完成，应用可以正常使用"
        Toast.makeText(requireContext(), "权限检查完成，应用可以正常使用", Toast.LENGTH_SHORT).show()
        
        // 可以在这里进行应用的初始化工作
        initializeApp()
    }
    
    /**
     * 权限被拒绝后的处理
     */
    private fun onPermissionsDenied(deniedPermissions: List<String>) {
        binding.tvStatus.text = "⚠️ 部分权限被拒绝，功能受限"
        val message = "以下权限被拒绝，可能影响应用功能：\n${deniedPermissions.joinToString(", ")}"
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        
        // 可以选择限制相关功能或降级使用
        initializeAppWithLimitedFeatures()
    }
    
    /**
     * 应用初始化（完整功能）
     */
    private fun initializeApp() {
        // 在这里进行完整的应用初始化
        println("应用初始化完成 - 完整功能可用")
        
        // 示例：可以直接使用网络功能
        // testNetworkFunction()
    }
    
    /**
     * 应用初始化（受限功能）
     */
    private fun initializeAppWithLimitedFeatures() {
        // 在这里进行受限的应用初始化
        println("应用初始化完成 - 部分功能受限")
    }
    
    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        binding.btnCheckPermissions.setOnClickListener {
            startPermissionCheck()
        }
        
        binding.btnTestNetwork.setOnClickListener {
            checkNetworkStatus()
        }
        
        binding.btnTestErnie.setOnClickListener {
            testNetworkFunction()
        }
    }
    
    /**
     * 测试网络功能（示例）
     */
    private fun testNetworkFunction() {
        lifecycleScope.launch {
            try {
                binding.tvStatus.text = "🔄 正在测试网络功能..."
                
                // 使用ERNIE网络服务发送测试消息
                // 注意：需要先配置正确的API密钥
                // networkExample.sendSimpleMessage("你好，请介绍一下自己")
                
                // 模拟网络测试延迟
                kotlinx.coroutines.delay(1000)
                
                binding.tvStatus.text = "✅ 网络功能测试完成 - 准备就绪"
                Toast.makeText(requireContext(), "网络功能测试完成（请配置API密钥后实际测试）", Toast.LENGTH_SHORT).show()
                
                println("网络功能测试 - 准备就绪")
            } catch (e: Exception) {
                binding.tvStatus.text = "❌ 网络功能测试失败"
                Toast.makeText(requireContext(), "网络功能测试失败: ${e.message}", Toast.LENGTH_SHORT).show()
                println("网络功能测试失败: ${e.message}")
            }
        }
    }
    
    /**
     * 手动申请特定权限的示例方法
     */
    private fun requestSpecificPermissions() {
        lifecycleScope.launch {
            // 申请相机权限示例
            if (requestCameraPermission()) {
                Toast.makeText(requireContext(), "相机权限已授予", Toast.LENGTH_SHORT).show()
                // 可以使用相机功能
            } else {
                Toast.makeText(requireContext(), "相机权限被拒绝", Toast.LENGTH_SHORT).show()
            }
            
            // 申请存储权限示例
            if (requestStoragePermission()) {
                Toast.makeText(requireContext(), "存储权限已授予", Toast.LENGTH_SHORT).show()
                // 可以使用存储功能
            } else {
                Toast.makeText(requireContext(), "存储权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 检查网络状态示例
     */
    private fun checkNetworkStatus() {
        val networkAvailable = com.example.kotlin.util.NetworkUtils.isNetworkAvailable(requireContext())
        if (networkAvailable) {
            binding.tvStatus.text = "🌐 网络连接正常"
            Toast.makeText(requireContext(), "网络连接正常", Toast.LENGTH_SHORT).show()
        } else {
            binding.tvStatus.text = "❌ 网络连接异常"
            Toast.makeText(requireContext(), "网络连接异常", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}

/**
 * 在Activity中使用示例：
 * 
 * class MainActivity : AppCompatActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         
 *         // 设置主Fragment
 *         if (savedInstanceState == null) {
 *             supportFragmentManager.beginTransaction()
 *                 .replace(R.id.fragment_container, MainFragment.newInstance())
 *                 .commit()
 *         }
 *     }
 * }
 */ 