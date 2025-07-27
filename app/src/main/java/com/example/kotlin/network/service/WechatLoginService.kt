package com.example.kotlin.network.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 微信登录服务
 */
class WechatLoginService(private val context: Context) {
    
    companion object {
        private const val TAG = "WechatLoginService"
        private const val WECHAT_APP_ID = "your_wechat_app_id" // 需要替换为实际的微信AppID
        
        // 微信登录状态
        private var isLoginInProgress = false
        private var loginCallback: ((Result<WechatLoginResult>) -> Unit)? = null
    }
    
    private var wxApi: IWXAPI? = null
    
    init {
        initWechatAPI()
    }
    
    /**
     * 初始化微信API
     */
    private fun initWechatAPI() {
        wxApi = WXAPIFactory.createWXAPI(context, WECHAT_APP_ID, true)
        wxApi?.registerApp(WECHAT_APP_ID)
        Log.d(TAG, "微信API初始化完成")
    }
    
    /**
     * 检查微信是否安装
     */
    fun isWechatInstalled(): Boolean {
        return wxApi?.isWXAppInstalled == true
    }
    
    /**
     * 检查微信API是否支持
     */
    fun isWechatSupported(): Boolean {
        return wxApi?.isWXAppInstalled == true
    }
    
    /**
     * 发起微信登录
     */
    suspend fun loginWithWechat(): Result<WechatLoginResult> {
        return suspendCancellableCoroutine { continuation ->
            if (isLoginInProgress) {
                continuation.resume(Result.failure(Exception("微信登录正在进行中")))
                return@suspendCancellableCoroutine
            }
            
            if (!isWechatInstalled()) {
                continuation.resume(Result.failure(Exception("微信未安装")))
                return@suspendCancellableCoroutine
            }
            
            if (!isWechatSupported()) {
                continuation.resume(Result.failure(Exception("微信版本不支持")))
                return@suspendCancellableCoroutine
            }
            
            try {
                isLoginInProgress = true
                loginCallback = { result ->
                    isLoginInProgress = false
                    loginCallback = null
                    continuation.resume(result)
                }
                
                // 创建微信登录请求
                val req = SendAuth.Req().apply {
                    scope = "snsapi_userinfo"
                    state = "wechat_login_${System.currentTimeMillis()}"
                }
                
                // 发送请求到微信
                val success = wxApi?.sendReq(req) ?: false
                if (!success) {
                    isLoginInProgress = false
                    loginCallback = null
                    continuation.resume(Result.failure(Exception("发送微信登录请求失败")))
                }
                
                Log.d(TAG, "微信登录请求已发送")
                
            } catch (e: Exception) {
                isLoginInProgress = false
                loginCallback = null
                continuation.resume(Result.failure(e))
            }
        }
    }
    
    /**
     * 处理微信登录回调
     * 这个方法需要在Activity的onActivityResult中调用
     */
    fun handleWechatResponse(intent: Intent) {
        wxApi?.handleIntent(intent, object : com.tencent.mm.opensdk.openapi.IWXAPIEventHandler {
            override fun onReq(baseReq: com.tencent.mm.opensdk.modelbase.BaseReq?) {
                // 处理请求
            }
            
            override fun onResp(baseResp: com.tencent.mm.opensdk.modelbase.BaseResp?) {
                baseResp?.let { resp ->
                    when (resp.errCode) {
                        com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_OK -> {
                            // 用户同意授权
                            if (resp is SendAuth.Resp) {
                                val code = resp.code
                                val state = resp.state
                                Log.d(TAG, "微信授权成功，code: $code, state: $state")
                                
                                // 这里应该调用后端API获取用户信息
                                // 为了演示，我们直接返回模拟数据
                                val result = WechatLoginResult(
                                    openId = "wx_openid_${System.currentTimeMillis()}",
                                    unionId = "wx_unionid_${System.currentTimeMillis()}",
                                    nickname = "微信用户",
                                    avatarUrl = null
                                )
                                
                                loginCallback?.invoke(Result.success(result))
                            } else {
                                loginCallback?.invoke(Result.failure(Exception("微信登录响应类型错误")))
                            }
                        }
                        com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_USER_CANCEL -> {
                            // 用户取消
                            Log.d(TAG, "用户取消微信登录")
                            loginCallback?.invoke(Result.failure(Exception("用户取消登录")))
                        }
                        com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                            // 用户拒绝授权
                            Log.d(TAG, "用户拒绝微信授权")
                            loginCallback?.invoke(Result.failure(Exception("用户拒绝授权")))
                        }
                        else -> {
                            // 其他错误
                            Log.e(TAG, "微信登录失败，错误码: ${resp.errCode}")
                            loginCallback?.invoke(Result.failure(Exception("微信登录失败: ${resp.errStr}")))
                        }
                    }
                }
            }
        })
    }
    
    /**
     * 释放资源
     */
    fun release() {
        wxApi?.unregisterApp()
        wxApi = null
        isLoginInProgress = false
        loginCallback = null
    }
}

/**
 * 微信登录结果数据类
 */
data class WechatLoginResult(
    val openId: String,
    val unionId: String?,
    val nickname: String?,
    val avatarUrl: String?
) 