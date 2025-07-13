import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.kotlin.R
import com.example.kotlin.data.NewsItem
import com.example.kotlin.screens.ChatScreen
import com.example.kotlin.screens.NewsScreen
import com.example.kotlin.screens.NewsDetailScreen
import com.example.kotlin.screens.PersonalScreen
import com.example.kotlin.screens.SquareScreen
import com.example.kotlin.state.AppState
import com.example.kotlin.state.NavigationEvent

// 导航项数据类
data class NavItem(
    val route: String,
    val label: String,
    val iconRes: Int
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // 处理导航事件
    LaunchedEffect(AppState.navigationEvent) {
        AppState.navigationEvent?.let { event ->
            when (event) {
                is NavigationEvent.NavigateToNewsDetail -> {
                    navController.navigate("news_detail/${event.newsItem.id}")
                }
                NavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
            // 清除事件
            AppState.clearNavigationEvent()
        }
    }

    // 定义导航项数据
    val items = listOf(
        NavItem("chat", "Chat", R.drawable.chat),
        NavItem("personal", "Personal", R.drawable.profile),
        NavItem("square", "Square", R.drawable.square),
        NavItem("news", "News", R.drawable.news)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = navController.currentDestination?.route == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                // 避免重复添加相同路由到返回栈
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(24.dp) // 控制图标大小
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(20.dp) // 图标实际大小
                                )
                            }
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1565C0), // 深蓝色（Material Blue 800）
                            unselectedIconColor = Color.Gray,      // 未选中状态颜色
                            indicatorColor = Color.Transparent     // 保持背景透明
                        )
                    )
                }
            }
        }
    )  { padding ->
        NavHost(
            navController = navController,
            startDestination = "chat",
            modifier = Modifier.padding(padding)
        ) {
            composable("chat") { ChatScreen() }
            composable("personal") { PersonalScreen() }
            composable("square") { SquareScreen() }
            composable("news") { NewsScreen() }
            composable("news_detail/{newsId}") { backStackEntry ->
                val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
                
                // 从缓存中获取新闻数据
                val newsItem = AppState.getCachedNewsData(newsId) ?: NewsItem(
                    id = newsId,
                    title = "新闻详情",
                    description = "",
                    source = "",
                    imageUrl = "",
                    articleUrl = "https://www.example.com", // 临时URL
                    publishTime = ""
                )
                
                NewsDetailScreen(newsItem = newsItem)
            }
        }
    }
}