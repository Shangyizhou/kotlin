package com.example.kotlin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlin.data.ChatMessage
import com.example.kotlin.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // 顶部状态栏
        ChatTopBar(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onClearChat = { viewModel.clearChat() },
            onRetry = { viewModel.retryLastMessage() },
            onClearError = { viewModel.clearError() }
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
fun ChatTopBar(
    isLoading: Boolean,
    errorMessage: String?,
    onClearChat: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI助手聊天",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                when {
                    isLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI正在思考中...",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    errorMessage != null -> {
                        Text(
                            text = "连接错误",
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }
                    else -> {
                        Text(
                            text = "在线",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            Row {
                if (errorMessage != null) {
                    IconButton(onClick = {
                        onClearError()
                        onRetry()
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重试",
                            tint = Color(0xFF2196F3)
                        )
                    }
                }
                
                IconButton(onClick = onClearChat) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "清空聊天",
                        tint = Color.Gray
                    )
                }
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
        modifier = modifier,
        state = listState,
        reverseLayout = true // 新消息从底部出现
    ) {
        items(messages.reversed()) { message ->
            ChatBubble(
                message = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
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
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                maxLines = 3
            )

            Button(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
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