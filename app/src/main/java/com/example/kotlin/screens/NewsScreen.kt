package com.example.kotlin.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlin.R
import com.example.kotlin.data.NewsItem
import com.example.kotlin.data.NewsType
import com.example.kotlin.state.AppState
import com.example.kotlin.state.NavigationEvent
import com.example.kotlin.viewmodel.NewsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = viewModel()
) {
    val selectedNewsType by viewModel.selectedNewsType.collectAsState()
    val newsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 标题栏
        TopAppBar(
            title = { Text("新闻资讯") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2196F3),
                titleContentColor = Color.White
            )
        )
        
        // Tab栏
        NewsTabBar(
            selectedNewsType = selectedNewsType,
            onNewsTypeSelected = { viewModel.switchNewsType(it) }
        )
        
        // Banner轮播图
        BannerCarousel()
        
        // 错误提示
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = error,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // 新闻列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(newsList ?: emptyList()) { newsItem ->
                NewsCard(
                    newsItem = newsItem,
                    onClick = { 
                        // 缓存新闻数据并触发导航事件
                        AppState.cacheNewsData(newsItem)
                        AppState.navigateTo(NavigationEvent.NavigateToNewsDetail(newsItem))
                    }
                )
            }
        }
        
        // 加载状态
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun BannerCarousel() {
    val bannerImages = listOf(
        R.drawable.banner_1,
        R.drawable.banner_2,
        R.drawable.banner_3,
        R.drawable.banner_4
    )
    
    var currentIndex by remember { mutableStateOf(0) }
    
    // 自动轮播
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // 3秒切换一次
            currentIndex = (currentIndex + 1) % bannerImages.size
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    ) {
        // Banner图片
        androidx.compose.foundation.Image(
            painter = painterResource(id = bannerImages[currentIndex]),
            contentDescription = "Banner ${currentIndex + 1}",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        // 索引点指示器
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bannerImages.forEachIndexed { index, _ ->
                val isSelected = index == currentIndex
                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = tween(durationMillis = 300),
                    label = "dot_alpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                        .clickable {
                            currentIndex = index
                        }
                )
            }
        }
    }
}

@Composable
fun NewsTabBar(
    selectedNewsType: NewsType,
    onNewsTypeSelected: (NewsType) -> Unit
) {
    val newsTypes = listOf(NewsType.IT_NEWS, NewsType.TECH_NEWS, NewsType.VR_TECH)
    
    TabRow(selectedTabIndex = newsTypes.indexOf(selectedNewsType)) {
        newsTypes.forEach { newsType ->
            Tab(
                selected = selectedNewsType == newsType,
                onClick = { onNewsTypeSelected(newsType) },
                text = { Text(newsType.displayName) }
            )
        }
    }
}

@Composable
fun NewsCard(
    newsItem: NewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = newsItem.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = newsItem.description,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = newsItem.source,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = newsItem.publishTime,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}