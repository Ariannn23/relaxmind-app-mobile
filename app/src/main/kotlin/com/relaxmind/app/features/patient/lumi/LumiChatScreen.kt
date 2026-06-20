package com.relaxmind.app.features.patient.lumi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import com.relaxmind.app.data.model.LumiMessage
import com.relaxmind.app.ui.components.FullScreenLoadingScreen
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LumiBlue = Color(0xFF38A9F2)
private val LumiBlueDark = Color(0xFF0B5C99)
private val LumiBlueSoft = Color(0xFFEAF7FF)
private val LumiBubbleBlue = Color(0xFFCFEAFF)
private val LumiBackground = Color(0xFFF4FAFF)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val BubbleWhite = Color(0xFFFFFFFF)
private val PlaceholderGray = Color(0xFFA0A5B1)

@Composable
fun LumiChatScreen(
    sessionId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: LumiChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showNewChatDialog by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(sessionId) {
        viewModel.initSession(sessionId)
    }

    LaunchedEffect(uiState.messages.size, uiState.currentStreamingText, uiState.isTyping) {
        val count = uiState.messages.size + if (uiState.isTyping) 1 else 0
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    fun sendCurrentMessage() {
        val message = inputText.trim()
        if (message.isNotBlank() && !uiState.isTyping) {
            viewModel.sendMessage(message)
            inputText = ""
            focusManager.clearFocus()
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onConfirm = {
                showNewChatDialog = false
                inputText = ""
                viewModel.startNewChat()
            }
        )
    }

    LumiChatBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                LumiChatHeader(
                    onNavigateBack = onNavigateBack,
                    onNewChatClick = { showNewChatDialog = true },
                    onHistoryClick = onNavigateToHistory,
                    showNewChat = true
                )
            },
            bottomBar = {
                LumiChatInputBar(
                    text = inputText,
                    onTextChange = { inputText = it },
                    onSendClick = ::sendCurrentMessage,
                    enabled = !uiState.isTyping
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.isLoading) {
                    FullScreenLoadingScreen(text = "Cargando Lumi...")
                } else {
                    ChatMessagesList(
                        messages = uiState.messages,
                        streamingText = uiState.currentStreamingText,
                        isTyping = uiState.isTyping,
                        listState = listState,
                        onPromptSelected = { prompt ->
                            inputText = prompt
                        }
                    )
                }
                
                // Error message
                if (uiState.error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                                .shadow(2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LumiChatBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, LumiBackground, Color(0xFFEFF8FF))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFDFF2FF).copy(alpha = 0.62f), Color.Transparent)
                    )
                )
        )
        content()
    }
}

@Composable
private fun LumiChatHeader(
    onNavigateBack: () -> Unit,
    onNewChatClick: () -> Unit,
    onHistoryClick: () -> Unit,
    showNewChat: Boolean
) {
    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(52.dp)
                    .semantics { contentDescription = "Volver" }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = LumiBlueDark,
                    modifier = Modifier.size(30.dp)
                )
            }

            LumiAvatar(size = 64)
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lumi",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "Asistente de bienestar",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = LumiBlueDark.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (showNewChat) {
                CircularIconButton(
                    icon = Icons.Default.AddComment,
                    contentDescription = "Nuevo chat",
                    backgroundColor = Color.White,
                    iconColor = LumiBlueDark,
                    shadow = true,
                    modifier = Modifier.size(46.dp),
                    onClick = onNewChatClick
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            CircularIconButton(
                icon = Icons.Outlined.History,
                contentDescription = "Historial de chats",
                backgroundColor = Color.White,
                iconColor = LumiBlueDark,
                shadow = true,
                modifier = Modifier.size(46.dp),
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
fun LumiAvatar(
    modifier: Modifier = Modifier,
    size: Int = 56
) {
    Image(
        painter = painterResource(id = com.relaxmind.app.R.drawable.lumi),
        contentDescription = "Avatar de Lumi",
        modifier = modifier
            .size(size.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = LumiBlueDark.copy(alpha = 0.12f),
                spotColor = LumiBlueDark.copy(alpha = 0.16f)
            )
            .clip(CircleShape)
    )
}

@Composable
private fun ChatMessagesList(
    messages: List<LumiMessage>,
    streamingText: String,
    isTyping: Boolean,
    listState: LazyListState,
    onPromptSelected: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (messages.isEmpty() && !isTyping) {
            item {
                LumiEmptyState(onPromptSelected = onPromptSelected)
            }
        } else {
            items(messages, key = { it.id.ifBlank { "${it.role}-${it.timestamp}-${it.text.hashCode()}" } }) { message ->
                MessageAppear {
                    if (message.role == "user") {
                        UserChatBubble(message = message)
                    } else {
                        LumiChatBubble(message = message)
                    }
                }
            }
        }

        if (isTyping) {
            item(key = "typing") {
                MessageAppear {
                    if (streamingText.isNotBlank()) {
                        LumiChatBubble(
                            message = LumiMessage(role = "model", text = streamingText, timestamp = System.currentTimeMillis()),
                            showTime = false
                        )
                    } else {
                        TypingIndicatorBubble()
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageAppear(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { it / 5 }),
        exit = fadeOut(tween(120))
    ) {
        content()
    }
}

@Composable
private fun UserChatBubble(message: LumiMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(28.dp, 28.dp, 6.dp, 28.dp),
                    ambientColor = LumiBlue.copy(alpha = 0.14f),
                    spotColor = LumiBlue.copy(alpha = 0.16f)
                ),
            shape = RoundedCornerShape(28.dp, 28.dp, 6.dp, 28.dp),
            color = LumiBubbleBlue
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize()
                    .padding(start = 22.dp, top = 18.dp, end = 18.dp, bottom = 13.dp)
            ) {
                Text(
                    text = message.text,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    color = LumiBlueDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        fontFamily = LexendFontFamily,
                        fontSize = 12.sp,
                        color = LumiBlueDark.copy(alpha = 0.70f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LumiChatBubble(
    message: LumiMessage,
    showTime: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        LumiAvatar(size = 42)
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(28.dp, 28.dp, 28.dp, 8.dp),
                    ambientColor = Color(0xFF7EB9DF).copy(alpha = 0.10f),
                    spotColor = Color(0xFF7EB9DF).copy(alpha = 0.12f)
                ),
            shape = RoundedCornerShape(28.dp, 28.dp, 28.dp, 8.dp),
            color = BubbleWhite
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize()
                    .padding(start = 22.dp, top = 18.dp, end = 18.dp, bottom = 13.dp)
            ) {
                Text(
                    text = message.text,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    color = TextPrimary
                )
                if (showTime) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatTime(message.timestamp),
                        modifier = Modifier.align(Alignment.End),
                        fontFamily = LexendFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        LumiAvatar(size = 42)
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.58f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(28.dp, 28.dp, 28.dp, 8.dp),
                    ambientColor = Color(0xFF7EB9DF).copy(alpha = 0.10f),
                    spotColor = Color(0xFF7EB9DF).copy(alpha = 0.12f)
                ),
            shape = RoundedCornerShape(28.dp, 28.dp, 28.dp, 8.dp),
            color = BubbleWhite
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Lumi está escribiendo...",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = TextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypingDot(delayMillis = 0)
                    TypingDot(delayMillis = 150)
                    TypingDot(delayMillis = 300)
                }
            }
        }
    }
}

@Composable
private fun TypingDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "TypingDots")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TypingDotScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TypingDotAlpha"
    )

    Box(
        modifier = Modifier
            .size(9.dp)
            .scale(scale)
            .background(LumiBlue.copy(alpha = alpha), CircleShape)
    )
}

@Composable
private fun LumiChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean
) {
    val canSend = text.isNotBlank() && enabled
    val sendScale by animateFloatAsState(
        targetValue = if (canSend) 1f else 0.94f,
        animationSpec = tween(180),
        label = "SendButtonScale"
    )

    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = LumiBlueDark.copy(alpha = 0.12f),
                        spotColor = LumiBlueDark.copy(alpha = 0.16f)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    placeholder = {
                        Text(
                            text = "Escribe tu mensaje...",
                            fontFamily = LexendFontFamily,
                            color = PlaceholderGray,
                            fontSize = 16.sp
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = LexendFontFamily,
                        fontSize = 16.sp,
                        color = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = LumiBlue
                    )
                )
            }

            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Enviar mensaje",
                backgroundColor = if (canSend) LumiBlue else LumiBlue.copy(alpha = 0.34f),
                iconColor = Color.White,
                modifier = Modifier.scale(sendScale),
                enabled = canSend,
                shadow = canSend,
                onClick = onSendClick
            )
        }
    }
}

@Composable
private fun CircularIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shadow: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .then(
                if (shadow) {
                    Modifier.shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        ambientColor = LumiBlue.copy(alpha = 0.28f),
                        spotColor = LumiBlue.copy(alpha = 0.28f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun LumiEmptyState(onPromptSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LumiAvatar(size = 94)
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Hola, soy Lumi",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Estoy aquí para acompañarte cuando necesites hablar.",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 22.dp)
        )
        Spacer(modifier = Modifier.height(26.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickPromptChip(text = "Me siento ansioso", onClick = onPromptSelected)
                QuickPromptChip(text = "Quiero respirar", onClick = onPromptSelected)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickPromptChip(text = "Necesito hablar", onClick = onPromptSelected)
                QuickPromptChip(text = "No sé cómo me siento", onClick = onPromptSelected)
            }
        }
    }
}

@Composable
private fun QuickPromptChip(
    text: String,
    onClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = LumiBlueSoft,
        border = BorderStroke(1.dp, BorderSoft.copy(alpha = 0.72f)),
        onClick = { onClick(text) }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = LumiBlueDark
        )
    }
}

@Composable
private fun NewChatDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "¿Iniciar nueva conversación?",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "La conversación actual quedará guardada en el historial.",
                fontFamily = LexendFontFamily,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Confirmar",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = LumiBlueDark
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    fontFamily = LexendFontFamily,
                    color = TextSecondary
                )
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}
