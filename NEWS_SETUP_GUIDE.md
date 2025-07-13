# 📰 新闻模块配置指南

## 🚀 功能介绍

您的Android应用现在已经集成了完整的新闻阅读功能，支持：

- 📱 **三个新闻分类**：VR科技、IT资讯、科技新闻
- 🔄 **实时数据更新**：从天行API获取最新新闻
- 📖 **WebView阅读**：点击新闻进入详情页面
- 🎨 **现代化UI**：Material Design 3界面设计
- 📱 **响应式布局**：适配不同屏幕尺寸

## ⚙️ 配置步骤

### 1. 获取天行API密钥

1. 访问 [天行数据官网](https://www.tianapi.com/)
2. 注册并登录账号
3. 在控制台创建应用
4. 获取API密钥

### 2. 配置API密钥

打开文件：`app/src/main/java/com/example/kotlin/network/service/NewsNetworkService.kt`

```kotlin
// 天行API密钥（需要替换为您的实际密钥）
private val apiKey = "your_tianapi_key_here"
```

将 `your_tianapi_key_here` 替换为您的实际API密钥。

### 3. 支持的新闻类型

根据天行API文档，支持以下新闻类型：

- **VR科技** (API ID: 19)
- **IT资讯** (API ID: 20)  
- **科技新闻** (API ID: 10)

## 📱 使用说明

### 新闻列表功能

- **切换分类**：点击顶部Tab栏切换不同新闻类型
- **查看新闻**：点击新闻卡片查看详情
- **刷新数据**：下拉刷新获取最新新闻
- **错误处理**：网络错误时显示友好提示

### 新闻详情功能

- **WebView阅读**：在应用内直接阅读新闻内容
- **返回列表**：点击返回按钮回到新闻列表
- **加载状态**：显示加载进度和错误重试

### 界面说明

```
┌─────────────────────────────────┐
│ [VR科技] [IT资讯] [科技新闻]     │  ← Tab栏
├─────────────────────────────────┤
│                                 │
│  ┌─────────────────────────────┐ │
│  │ 新闻标题                    │ │
│  │ 新闻描述...                 │ │  ← 新闻卡片
│  │ 来源 | 发布时间             │ │
│  └─────────────────────────────┘ │
│                                 │
│  ┌─────────────────────────────┐ │
│  │ 新闻标题                    │ │
│  │ 新闻描述...                 │ │
│  │ 来源 | 发布时间             │ │
│  └─────────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

## 🛠️ 高级配置

### 自定义新闻类型

在 `NewsType.kt` 中添加新的新闻类型：

```kotlin
enum class NewsType(val apiKey: String, val displayName: String) {
    VR_TECH("19", "VR科技"),
    IT_NEWS("20", "IT资讯"),
    TECH_NEWS("10", "科技新闻"),
    // 添加新的新闻类型
    NEW_CATEGORY("25", "新分类")
}
```

### 调整分页参数

在 `NewsViewModel.kt` 中修改分页设置：

```kotlin
val result = networkService.getNewsList(
    newsType = newsType,
    page = currentPage,
    pageSize = 20  // 修改每页数量
)
```

### 自定义UI样式

在 `NewsScreen.kt` 中修改界面样式：

```kotlin
// 修改卡片样式
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),  // 修改圆角
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)  // 修改阴影
) {
    // 内容
}
```

## 🔧 故障排除

### 问题1：API密钥错误
**现象**：显示"API返回错误"
**解决**：检查NewsNetworkService.kt中的API密钥是否正确

### 问题2：网络连接失败
**现象**：显示"网络连接失败"
**解决**：
1. 检查网络连接
2. 确认已添加网络权限
3. 检查防火墙设置

### 问题3：新闻加载失败
**现象**：新闻列表为空
**解决**：
1. 查看错误日志
2. 检查API配额是否用尽
3. 确认API密钥权限

### 问题4：WebView无法加载
**现象**：新闻详情页面空白
**解决**：
1. 检查新闻URL是否有效
2. 确认WebView权限设置
3. 检查JavaScript是否启用

## 📋 文件结构

```
app/src/main/java/com/example/kotlin/
├── data/
│   └── NewsItem.kt              # 新闻数据模型
├── network/
│   ├── api/
│   │   └── NewsApiService.kt    # 新闻API接口
│   ├── config/
│   │   └── NewsNetworkConfig.kt # 网络配置
│   └── service/
│       └── NewsNetworkService.kt # 网络服务
├── viewmodel/
│   └── NewsViewModel.kt         # 新闻ViewModel
└── screens/
    ├── NewsScreen.kt            # 新闻列表界面
    └── NewsDetailScreen.kt      # 新闻详情界面
```

## 🎯 技术特点

- **MVVM架构**：清晰的代码分层
- **响应式编程**：使用StateFlow管理状态
- **协程支持**：异步网络请求处理
- **Material Design 3**：现代化UI设计
- **WebView集成**：内置浏览器功能

配置完成后即可开始使用新闻阅读功能！ 