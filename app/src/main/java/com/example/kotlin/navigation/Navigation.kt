import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
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
import com.example.kotlin.screens.NewsScreen
import com.example.kotlin.screens.ProfileScreen
import com.example.kotlin.screens.SquareScreen

// 导航项数据类
data class NavItem(
    val route: String,
    val label: String,
    val iconRes: Int
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 定义导航项数据
    val items = listOf(
        NavItem("chat", "Chat", R.drawable.chat),
        NavItem("profile", "Profile", R.drawable.profile),
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
            composable("profile") { ProfileScreen() }
            composable("square") { SquareScreen() }
            composable("news") { NewsScreen() }
        }
    }
}