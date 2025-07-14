package com.example.kotlin.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kotlin.R
import com.example.kotlin.util.PermissionManager
import com.example.kotlin.viewmodel.UserProfileViewModel
import com.example.kotlin.viewmodel.UserProfileViewModelFactory
import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen() {
    val context = LocalContext.current
    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(context)
    )
    
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // 权限状态
    val hasPermission = remember { mutableStateOf(PermissionManager.hasImagePermission(context)) }
    
    // 权限申请启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermission.value = allGranted
        
        if (allGranted) {
            // 权限获取成功，打开图片选择器
            showImagePickerDialog = true
        }
    }
    
    // 图片选择启动器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // 保存头像URL到数据库
            val avatarUrl = it.toString()
            android.util.Log.d("PersonalScreen", "Selected image URI: $avatarUrl")
            userProfileViewModel.updateAvatarUrl(avatarUrl)
        }
    }
    
    // 观察用户信息
    val userProfile by userProfileViewModel.userProfile.collectAsState()
    val isLoading by userProfileViewModel.isLoading.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            // 顶部头像区域
            ProfileHeader(
                userProfile = userProfile,
                onImageClick = {
                    if (hasPermission.value) {
                        showImagePickerDialog = true
                    } else {
                        // 申请权限
                        permissionLauncher.launch(PermissionManager.getImagePermissions())
                    }
                }
            )
        }
        
        item {
            // 个人信息列表
            PersonalInfoList(userProfile = userProfile)
        }
        
        item {
            // 设置选项
            SettingsSection()
        }
    }
    
    // 图片选择对话框
    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            onImageSelected = { imageRes ->
                // 这里可以处理默认图片选择
                showImagePickerDialog = false
            },
            onGallerySelected = {
                showImagePickerDialog = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }
}

@Composable
fun ProfileHeader(
    userProfile: com.example.kotlin.data.UserProfileEntity?,
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 圆形头像
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable { onImageClick() }
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            android.util.Log.d("ProfileHeader", "Avatar URL: ${userProfile?.avatarUrl}")
            if (userProfile?.avatarUrl != null) {
                // 显示从数据库加载的头像
                AsyncImage(
                    model = userProfile.avatarUrl,
                    contentDescription = "头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 显示默认头像
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // 编辑图标
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑头像",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 用户名
        Text(
            text = userProfile?.name ?: "张三",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun PersonalInfoList(userProfile: com.example.kotlin.data.UserProfileEntity?) {
    val personalInfo = listOf(
        PersonalInfoItem("姓名", userProfile?.name ?: "张三", Icons.Default.Person),
        PersonalInfoItem("出生日期", userProfile?.birthDate ?: "1995年3月15日", Icons.Default.Star),
        PersonalInfoItem("星座", userProfile?.zodiac ?: "双鱼座", Icons.Default.Star),
        PersonalInfoItem("个性签名", userProfile?.signature ?: "热爱生活，追求梦想", Icons.Default.Edit)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            personalInfo.forEachIndexed { index, item ->
                PersonalInfoRow(
                    item = item,
                    isLast = index == personalInfo.size - 1
                )
            }
        }
    }
}

@Composable
fun PersonalInfoRow(
    item: PersonalInfoItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 信息内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = item.value,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 编辑图标（仅对签名显示）
        if (item.label == "个性签名") {
            Icon(
                Icons.Default.Edit,
                contentDescription = "编辑",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    // 分割线（除了最后一项）
    if (!isLast) {
        Divider(
            modifier = Modifier.padding(start = 56.dp),
            color = Color(0xFFE0E0E0),
            thickness = 1.dp
        )
    }
}

@Composable
fun SettingsSection() {
    val context = LocalContext.current
    Spacer(modifier = Modifier.height(24.dp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            SettingsRow(
                icon = Icons.Default.Settings,
                title = "设置",
                subtitle = "应用设置和偏好",
                onClick = { /* TODO: 跳转到设置页面 */ }
            )
            
            Divider(
                modifier = Modifier.padding(start = 56.dp),
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )
            
            SettingsRow(
                icon = Icons.Default.Share,
                title = "分享APP",
                subtitle = "推荐给朋友",
                onClick = {
                    // 分享APP
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "推荐一个好用的AI聊天APP")
                        putExtra(Intent.EXTRA_TEXT, "我正在使用一个很棒的AI聊天APP，快来试试吧！\n下载地址：https://www.example.com")
                    }
                    context.startActivity(Intent.createChooser(intent, "分享APP"))
                }
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 文本内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        // 箭头图标
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = "跳转",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Int) -> Unit,
    onGallerySelected: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "选择头像",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 从相册选择按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGallerySelected() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "相册",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "从相册选择",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // 默认头像选项
                Text(
                    text = "默认头像",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val imageOptions = listOf(
                        R.drawable.profile,
                        R.drawable.chat,
                        R.drawable.news,
                        R.drawable.square
                    )
                    
                    imageOptions.forEach { imageRes ->
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .clickable { onImageSelected(imageRes) }
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "头像选项",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("取消")
                }
            }
        }
    }
}

// 数据类
data class PersonalInfoItem(
    val label: String,
    val value: String,
    val icon: ImageVector
)

// ==================== PREVIEW 部分 ====================

@Preview(showBackground = true, name = "个人资料页面")
@Composable
fun PersonalScreenPreview() {
    PersonalScreen()
}