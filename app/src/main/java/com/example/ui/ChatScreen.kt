package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AmbientLiquidBackground
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.EmptyChatState
import com.example.ui.components.ExplainDifferentlyBottomSheet
import com.example.ui.components.HistoryDrawerContent
import com.example.ui.components.ImagePreviewDialog
import com.example.ui.components.MessageComposer
import com.example.ui.components.SettingsScreen
import com.example.ui.components.StreamingMessageBubble
import com.example.ui.components.liquidGlass
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidIndigo
import com.example.ui.theme.LiquidSky
import com.example.ui.theme.LiquidViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: LearnMateViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isDark = isSystemInDarkTheme()

    val currentConversationId by viewModel.currentConversationId.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val attachedBitmap by viewModel.attachedBitmap.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val streamingContent by viewModel.streamingContent.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isMemoryEnabled by viewModel.isMemoryEnabled.collectAsStateWithLifecycle()
    val responseStyle by viewModel.responseStyle.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val studentMemories by viewModel.studentMemories.collectAsStateWithLifecycle()
    val explainDifferentlyTarget by viewModel.explainDifferentlyMessage.collectAsStateWithLifecycle()
    val playingAudioMessageId by viewModel.ttsManager.currentPlayingMessageId.collectAsStateWithLifecycle()

    var showSettings by remember { mutableStateOf(false) }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteCurrentChatConfirm by remember { mutableStateOf(false) }
    var showClearCurrentMessagesConfirm by remember { mutableStateOf(false) }
    var messageIdToDelete by remember { mutableStateOf<String?>(null) }
    var chatMenuExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto scroll down when messages change or streaming updates
    LaunchedEffect(messages.size, streamingContent) {
        val totalCount = messages.size + (if (isGenerating) 1 else 0)
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    // Handle Back Press
    BackHandler(enabled = drawerState.isOpen || showSettings || previewImagePath != null || explainDifferentlyTarget != null) {
        when {
            previewImagePath != null -> previewImagePath = null
            explainDifferentlyTarget != null -> viewModel.setExplainDifferentlyTarget(null)
            showSettings -> showSettings = false
            drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
        }
    }

    AmbientLiquidBackground(modifier = modifier) {
        if (showSettings) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                responseStyle = responseStyle,
                onResponseStyleChange = { viewModel.setResponseStyle(it) },
                isMemoryEnabled = isMemoryEnabled,
                onMemoryEnabledChange = { viewModel.setMemoryEnabled(it) },
                speechRate = speechRate,
                onSpeechRateChange = { viewModel.setSpeechRate(it) },
                studentMemories = studentMemories,
                onDeleteMemoryItem = { viewModel.deleteMemoryItem(it) },
                onClearAllMemories = { viewModel.clearAllMemories() },
                conversationsCount = conversations.size,
                onClearAllConversations = { viewModel.clearAllConversations() },
                onBackClick = { showSettings = false }
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    HistoryDrawerContent(
                        conversations = conversations,
                        activeConversationId = currentConversationId,
                        onSelectConversation = { id ->
                            viewModel.selectConversation(id)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onNewChatClick = {
                            viewModel.startNewConversation()
                            coroutineScope.launch { drawerState.close() }
                        },
                        onDeleteConversation = { id ->
                            viewModel.deleteConversation(id)
                        },
                        onRenameConversation = { id, newTitle ->
                            viewModel.renameConversation(id, newTitle)
                        },
                        onClearAllConversations = {
                            viewModel.clearAllConversations()
                        }
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(LiquidCyan, LiquidIndigo)
                                                    )
                                                )
                                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "LearnMate AI",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = (-0.3).sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF10B981))
                                                )
                                                Text(
                                                    text = "Personalized AI Tutor",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    color = if (isDark) LiquidSky else Color(0xFF0284C7)
                                                )
                                            }
                                        }
                                    }
                                },
                                navigationIcon = {
                                    val buttonBg = if (isDark) Color(0x3338BDF8) else Color(0x30E0F2FE)
                                    val buttonBorder = if (isDark) Color(0x4038BDF8) else Color(0x600284C7)
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(buttonBg)
                                            .border(1.dp, buttonBorder, CircleShape)
                                            .clickable { coroutineScope.launch { drawerState.open() } }
                                            .testTag("menu_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Conversation history",
                                            tint = if (isDark) LiquidSky else Color(0xFF0369A1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                actions = {
                                    val buttonBg = if (isDark) Color(0x3338BDF8) else Color(0x30E0F2FE)
                                    val buttonBorder = if (isDark) Color(0x4038BDF8) else Color(0x600284C7)

                                    // Chat Options (Clear / Delete)
                                    if (messages.isNotEmpty() || currentConversationId != null) {
                                        Box {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(buttonBg)
                                                    .border(1.dp, buttonBorder, CircleShape)
                                                    .clickable { chatMenuExpanded = true }
                                                    .testTag("chat_options_button"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Chat options",
                                                    tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = chatMenuExpanded,
                                                onDismissRequest = { chatMenuExpanded = false }
                                            ) {
                                                if (messages.isNotEmpty()) {
                                                    DropdownMenuItem(
                                                        text = { Text("Clear Messages") },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Default.DeleteOutline,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        },
                                                        onClick = {
                                                            chatMenuExpanded = false
                                                            showClearCurrentMessagesConfirm = true
                                                        }
                                                    )
                                                }
                                                if (currentConversationId != null) {
                                                    DropdownMenuItem(
                                                        text = { Text("Delete This Chat", color = MaterialTheme.colorScheme.error) },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Default.DeleteOutline,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        },
                                                        onClick = {
                                                            chatMenuExpanded = false
                                                            showDeleteCurrentChatConfirm = true
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    // New Chat Action
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(buttonBg)
                                            .border(1.dp, buttonBorder, CircleShape)
                                            .clickable { viewModel.startNewConversation() }
                                            .testTag("top_new_chat_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Start new conversation",
                                            tint = if (isDark) LiquidSky else Color(0xFF0369A1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Settings Action
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(buttonBg)
                                            .border(1.dp, buttonBorder, CircleShape)
                                            .clickable { showSettings = true }
                                            .testTag("settings_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Open Settings",
                                            tint = if (isDark) LiquidSky else Color(0xFF0369A1),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = if (isDark) Color(0xD00E1629) else Color(0xD8FFFFFF)
                                )
                            )

                            // Specular Bottom Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            if (isDark) listOf(
                                                Color.Transparent,
                                                LiquidCyan.copy(alpha = 0.4f),
                                                LiquidViolet.copy(alpha = 0.4f),
                                                Color.Transparent
                                            ) else listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.9f),
                                                LiquidCyan.copy(alpha = 0.25f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    },
                    bottomBar = {
                        MessageComposer(
                            text = inputText,
                            onTextChange = { viewModel.updateInputText(it) },
                            attachedBitmap = attachedBitmap,
                            onImageRemoved = { viewModel.clearAttachedImage() },
                            onImageSelected = { bitmap, uri ->
                                viewModel.setAttachedImage(bitmap, uri?.toString())
                            },
                            isGenerating = isGenerating,
                            onSend = { viewModel.sendMessage() },
                            onStop = { viewModel.stopGeneration() }
                        )
                    },
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        if (messages.isEmpty() && !isGenerating) {
                            EmptyChatState(
                                onSelectPrompt = { prompt ->
                                    viewModel.sendMessage(promptOverride = prompt)
                                }
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp)
                            ) {
                                items(messages, key = { it.id }) { msg ->
                                    val isSpeaking = playingAudioMessageId == msg.id
                                    ChatMessageBubble(
                                        message = msg,
                                        isPlayingAudio = isSpeaking,
                                        onSpeakClick = { viewModel.speakMessage(msg) },
                                        onStopAudioClick = { viewModel.stopSpeaking() },
                                        onExplainDifferentlyClick = { viewModel.setExplainDifferentlyTarget(msg) },
                                        onDeleteMessageClick = { msgId -> messageIdToDelete = msgId },
                                        onImageClick = { imgUri -> previewImagePath = imgUri }
                                    )
                                }

                                if (isGenerating) {
                                    item {
                                        StreamingMessageBubble(streamingContent = streamingContent)
                                    }
                                }
                            }
                        }

                        // Liquid Glass Error Banner
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (errorMessage != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(8.dp, RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xE07F1D1D))
                                        .border(1.dp, Color(0xFFF87171).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WifiOff,
                                                contentDescription = null,
                                                tint = Color(0xFFFCA5A5),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = errorMessage ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.clearError() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss error",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Explain Differently Bottom Sheet
    if (explainDifferentlyTarget != null) {
        ExplainDifferentlyBottomSheet(
            targetMessage = explainDifferentlyTarget!!,
            onDismiss = { viewModel.setExplainDifferentlyTarget(null) },
            onSelectStyle = { styleName, promptModifier ->
                viewModel.triggerExplainDifferently(explainDifferentlyTarget!!, promptModifier, styleName)
            }
        )
    }

    // Fullscreen Image Preview Dialog
    if (previewImagePath != null) {
        ImagePreviewDialog(
            imagePath = previewImagePath!!,
            onDismiss = { previewImagePath = null }
        )
    }

    // Delete Entire Conversation Confirmation Dialog
    if (showDeleteCurrentChatConfirm) {
        val convId = currentConversationId
        AlertDialog(
            onDismissRequest = { showDeleteCurrentChatConfirm = false },
            title = { Text("Delete This Conversation?") },
            text = {
                Text(
                    text = "Are you sure you want to delete this entire chat session? All questions and answers will be permanently deleted.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (convId != null) {
                            viewModel.deleteConversation(convId)
                        }
                        showDeleteCurrentChatConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCurrentChatConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Messages in Current Conversation Confirmation Dialog
    if (showClearCurrentMessagesConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCurrentMessagesConfirm = false },
            title = { Text("Clear All Messages?") },
            text = {
                Text(
                    text = "Are you sure you want to clear all messages in this conversation? The conversation topic will remain.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCurrentConversationMessages()
                        showClearCurrentMessagesConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCurrentMessagesConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Single Message Confirmation Dialog
    if (messageIdToDelete != null) {
        val targetId = messageIdToDelete
        AlertDialog(
            onDismissRequest = { messageIdToDelete = null },
            title = { Text("Delete Message?") },
            text = {
                Text(
                    text = "Are you sure you want to delete this message from the conversation?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetId != null) {
                            viewModel.deleteMessage(targetId)
                        }
                        messageIdToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageIdToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
