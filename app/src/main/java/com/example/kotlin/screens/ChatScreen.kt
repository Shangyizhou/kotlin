package com.example.kotlin.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlin.data.ChatMessage
import com.example.kotlin.data.ChatSessionEntity
import com.example.kotlin.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    // 状态收集
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    
    // 侧边栏状态
    var showSidebar by remember { mutableStateOf(false) }
    
    // 键盘控制器
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // 错误消息处理
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // 可以在这里显示错误提示
        }
    }
    
    // 自动滚动到最新消息
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // 顶部状态栏
            ChatTopBar(
                onMenuClick = { showSidebar = true },
                onNewChat = { viewModel.createNewSession() },
                currentSession = sessions.find { it.sessionId == currentSessionId }
            )
            
            // 消息列表区域
            MessageList(
                messages = messages,
                listState = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // 推荐问题气泡（在AI回复后显示，或聊天为空时显示初始问题）
            if (!isLoading && (
                messages.isEmpty() || 
                (messages.isNotEmpty() && messages.last().isMe.not())
            )) {
                RecommendedQuestions(
                    isInitialQuestions = messages.isEmpty(),
                    onQuestionClick = { question ->
                        viewModel.sendMessage(question)
                    }
                )
            }

            // 输入框区域
            ChatInput(
                text = inputText,
                isLoading = isLoading,
                onTextChange = viewModel::updateInputText,
                onSend = { 
                    keyboardController?.hide()
                    viewModel.sendMessage(inputText) 
                }
            )
        }
        
        // 侧边栏
        if (showSidebar) {
            ChatSidebar(
                sessions = sessions,
                currentSessionId = currentSessionId,
                onSessionClick = { sessionId ->
                    viewModel.switchToSession(sessionId)
                    showSidebar = false
                },
                onDeleteSession = { sessionId ->
                    viewModel.deleteSession(sessionId)
                },
                onDismiss = { showSidebar = false }
            )
        }
    }
}

@Composable
fun ChatTopBar(
    onMenuClick: () -> Unit,
    onNewChat: () -> Unit,
    currentSession: ChatSessionEntity?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧菜单按钮
        IconButton(onClick = onMenuClick) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "历史记录",
                tint = Color(0xFF2196F3)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 中间标题
        Text(
            text = currentSession?.title ?: "AI聊天机器人",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 右侧新建聊天按钮
        IconButton(onClick = onNewChat) {
            Icon(
                Icons.Default.Add,
                contentDescription = "新建聊天",
                tint = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun ChatSidebar(
    sessions: List<ChatSessionEntity>,
    currentSessionId: String,
    onSessionClick: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() }
        )
        
        // 侧边栏内容
        Card(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart),
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题
                Text(
                    text = "聊天记录",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 会话列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { session ->
                        SessionItem(
                            session = session,
                            isSelected = session.sessionId == currentSessionId,
                            onClick = { onSessionClick(session.sessionId) },
                            onDelete = { onDeleteSession(session.sessionId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItem(
    session: ChatSessionEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (session.lastMessage.isNotEmpty()) {
                    Text(
                        text = session.lastMessage,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Text(
                    text = dateFormat.format(Date(session.lastMessageTime)),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun RecommendedQuestions(
    isInitialQuestions: Boolean = false,
    onQuestionClick: (String) -> Unit
) {
    // 初始问题列表
    val initialQuestions = remember {
        listOf(
            "你好，请介绍一下你自己",
            "什么是人工智能？",
            "如何学习编程？",
            "推荐一些好书",
            "今天天气怎么样？",
            "帮我写一个简单的程序"
        )
    }
    
    // 后续问题列表
    val followUpQuestions = remember {
        listOf(
            "请详细解释一下",
            "能举个例子吗？",
            "还有其他方法吗？",
            "这个有什么优缺点？",
            "如何应用到实际中？",
            "可以推荐相关资源吗？",
            "能总结一下要点吗？",
            "有什么注意事项？",
            "如何进一步学习？",
            "还有其他问题吗？"
        )
    }
    
    // 根据状态选择问题列表
    val questionPool = if (isInitialQuestions) initialQuestions else followUpQuestions
    
    // 随机选择3个问题
    val currentQuestions = remember {
        questionPool.shuffled().take(3)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // 问题气泡横向排列
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentQuestions.forEach { question ->
                QuestionBubble(
                    question = question,
                    onClick = { onQuestionClick(question) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        reverseLayout = true,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(messages.reversed()) { message ->
            ChatBubble(
                message = message,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun QuestionBubble(
    question: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = question,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = if (message.isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isMe) 16.dp else 4.dp,
                bottomEnd = if (message.isMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isMe) Color(0xFF2196F3) else Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (message.isMe) Color.White else Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(message.timestamp)),
                    fontSize = 10.sp,
                    color = if (message.isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    text: String,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { 
                    Text(
                        text = if (isLoading) "AI正在回复中..." else "输入消息...",
                        color = Color.Gray
                    ) 
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                enabled = !isLoading,
                maxLines = 1,
                singleLine = true
            )

            Button(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                modifier = Modifier.height(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("发送")
                }
            }
        }
    }
}

// ==================== PREVIEW 部分 ====================

@Preview(showBackground = true, name = "聊天页面 - 空状态")
@Composable
fun ChatScreenEmptyPreview() {
    ChatScreen()
}