# 💬 聊天功能配置指南

## 🚀 功能介绍

你的Android应用现在已经集成了完整的AI聊天功能，支持：

- 🤖 **百度ERNIE-4.0-8K**模型对话
- 💬 **实时聊天界面**
- 🔄 **自动重试**和错误处理
- 📱 **现代化UI**设计
- 🌐 **网络状态**监控

## ⚙️ 配置步骤

### 1. 获取百度API密钥

1. 访问 [百度智能云控制台](https://console.bce.baidu.com/)
2. 登录并创建新应用
3. 在应用管理中找到你的应用
4. 复制 **API Key** 和 **Secret Key**

### 2. 配置API密钥

打开文件：`app/src/main/java/com/example/kotlin/config/ApiConfig.kt`

```kotlin
object ApiConfig {
    const val BAIDU_API_KEY = "你的API_KEY"      // 替换这里
    const val BAIDU_SECRET_KEY = "你的SECRET_KEY"  // 替换这里
}
```

### 3. 重新编译运行

配置完成后重新编译并运行应用即可开始聊天！

## 📱 使用说明

### 聊天界面功能

- **发送消息**：在底部输入框输入文本，点击发送按钮
- **查看状态**：顶部显示连接状态和AI思考进度
- **清空聊天**：点击右上角清除按钮
- **重试消息**：发送失败时点击重试按钮

### 界面说明

```
┌─────────────────────────────────┐
│ AI助手聊天        🔄 🗑️     │  ← 状态栏
├─────────────────────────────────┤
│                                 │
│  AI消息气泡          10:30   │
│                                 │
│            用户消息气泡  10:31  │  ← 聊天区域
│                                 │
│  AI回复消息          10:32   │
│                                 │
├─────────────────────────────────┤
│ [输入消息...]        [发送]    │  ← 输入区域
└─────────────────────────────────┘
```

## 🛠️ 高级配置

### 自定义AI角色

在 `ChatViewModel.kt` 中修改系统提示：

```kotlin
addSystemMessage("你是一个专业的编程助手") // 自定义角色
```

### 调整对话参数

在 `ChatViewModel.kt` 中的 `sendChatMessage` 调用中：

```kotlin
val result = networkService.sendChatMessage(
    accessToken = getAccessToken(),
    messages = conversationHistory.toList(),
    temperature = 0.8,    // 创造性 (0.1-1.0)
    topP = 0.9,          // 话题聚焦 (0.1-1.0)
    maxOutputTokens = 2048 // 最大回复长度
)
```

## 🔧 故障排除

### 问题1：API密钥错误
**现象**：显示"获取访问令牌失败"
**解决**：检查ApiConfig.kt中的密钥是否正确

### 问题2：网络连接失败
**现象**：显示"网络连接异常"
**解决**：
1. 检查网络连接
2. 确认已添加网络权限（已自动添加）
3. 检查防火墙设置

### 问题3：聊天无响应
**现象**：发送消息后无AI回复
**解决**：
1. 查看错误日志
2. 检查API配额是否用尽
3. 确认API密钥权限

### 问题4：编译错误
**现象**：构建失败
**解决**：
1. 清理项目：`./gradlew clean`
2. 重新构建：`./gradlew assembleDebug`
3. 检查依赖版本

## 📋 文件结构

```
app/src/main/java/com/example/kotlin/
├── config/
│   └── ApiConfig.kt              # API配置
├── viewmodel/
│   └── ChatViewModel.kt          # 聊天逻辑
├── screens/
│   └── ChatScreen.kt             # 聊天界面
├── network/
│   ├── model/                    # 数据模型
│   ├── api/                      # API接口
│   ├── config/                   # 网络配置
│   └── service/                  # 网络服务
└── data/
    └── ChatMessage.kt            # 消息数据类
```

## 🎉 完成！

配置完成后，你的应用就拥有了强大的AI聊天功能。用户可以：

- ✅ 与AI进行自然对话
- ✅ 获得智能回复
- ✅ 享受流畅的聊天体验

如有问题，请检查上述配置步骤或查看错误日志。 