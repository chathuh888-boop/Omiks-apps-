package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudentMemoryEntity
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidIndigo
import com.example.ui.theme.LiquidSky
import com.example.ui.theme.LiquidViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    responseStyle: String,
    onResponseStyleChange: (String) -> Unit,
    isMemoryEnabled: Boolean,
    onMemoryEnabledChange: (Boolean) -> Unit,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    studentMemories: List<StudentMemoryEntity>,
    onDeleteMemoryItem: (String) -> Unit,
    onClearAllMemories: () -> Unit,
    conversationsCount: Int = 0,
    onClearAllConversations: () -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var showClearMemoryConfirm by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                                .clickable(onClick = onBackClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to chat",
                                tint = if (isDark) LiquidSky else Color(0xFF0369A1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDark) Color(0xD00E1629) else Color(0xD8FFFFFF)
                    )
                )

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
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Appearance Section
            SettingsSectionCard(title = "Appearance", icon = Icons.Default.Brightness4, isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (mode, label) ->
                            val isSelected = themeMode == mode
                            val itemBg = if (isSelected) {
                                if (isDark) Color(0x5538BDF8) else Color(0x55BAE6FD)
                            } else {
                                if (isDark) Color(0x201E293B) else Color(0x20E2E8F0)
                            }
                            val itemBorder = if (isSelected) {
                                if (isDark) Color(0x9038BDF8) else Color(0x900284C7)
                            } else {
                                if (isDark) Color(0x18FFFFFF) else Color(0x35000000)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(itemBg)
                                    .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                                    .clickable { onThemeModeChange(mode) }
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) (if (isDark) LiquidSky else Color(0xFF0369A1)) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2. AI Response Style Section
            SettingsSectionCard(title = "AI Tutor Style", icon = Icons.Default.AutoAwesome, isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Explanation Style",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val styles = listOf(
                        "Clear & Detailed Tutor" to "Explains concepts thoroughly with steps and understanding.",
                        "Balanced Assistant" to "Direct answers with concise explanations.",
                        "Direct & Concise" to "Fast, brief answers with minimal elaboration."
                    )

                    styles.forEach { (style, desc) ->
                        val isSelected = responseStyle == style
                        val itemBg = if (isSelected) {
                            if (isDark) Color(0x4038BDF8) else Color(0x40BAE6FD)
                        } else {
                            if (isDark) Color(0x181E293B) else Color(0x18E2E8F0)
                        }
                        val itemBorder = if (isSelected) {
                            if (isDark) Color(0x8038BDF8) else Color(0x800284C7)
                        } else {
                            if (isDark) Color(0x18FFFFFF) else Color(0x30000000)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(itemBg)
                                .border(1.dp, itemBorder, RoundedCornerShape(14.dp))
                                .clickable { onResponseStyleChange(style) }
                                .padding(13.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = style,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0x9994A3B8) else Color(0x9964748B)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (isDark) LiquidSky else Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Voice Settings Section
            SettingsSectionCard(title = "Voice & Read Aloud", icon = Icons.Default.RecordVoiceOver, isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speech Rate",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format("%.1f", speechRate)}x",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) LiquidSky else Color(0xFF0284C7)
                        )
                    }

                    Slider(
                        value = speechRate,
                        onValueChange = onSpeechRateChange,
                        valueRange = 0.75f..1.5f,
                        steps = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Slower", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Normal (1.0x)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Faster", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 4. Memory Section
            SettingsSectionCard(title = "Student Memory", icon = Icons.Default.Psychology, isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Adaptive Student Memory",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Remembers grade level or subjects you need extra help with to tailor explanations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0x9994A3B8) else Color(0x9964748B)
                            )
                        }
                        Switch(
                            checked = isMemoryEnabled,
                            onCheckedChange = onMemoryEnabledChange,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    if (isMemoryEnabled) {
                        if (studentMemories.isNotEmpty()) {
                            Text(
                                text = "Learned Preferences:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            studentMemories.forEach { mem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDark) Color(0x2838BDF8) else Color(0x20BAE6FD))
                                        .border(1.dp, if (isDark) Color(0x3838BDF8) else Color(0x350284C7), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mem.key,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isDark) LiquidSky else Color(0xFF0284C7)
                                        )
                                        Text(
                                            text = mem.value,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteMemoryItem(mem.key) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete memory",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = { showClearMemoryConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Clear All Memory")
                            }
                        } else {
                            Text(
                                text = "No preferences stored yet. Memory updates quietly as you chat (e.g. \"I'm in Grade 10\").",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // 5. Chat History & Local Storage
            SettingsSectionCard(title = "Chat History & Storage", icon = Icons.Default.ChatBubbleOutline, isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Saved Conversations",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (conversationsCount > 0) "$conversationsCount chat session(s) stored in local Room database" else "No saved conversations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0x3038BDF8) else Color(0x200284C7))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$conversationsCount",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) LiquidSky else Color(0xFF0284C7)
                            )
                        }
                    }

                    if (conversationsCount > 0) {
                        OutlinedButton(
                            onClick = { showClearHistoryConfirm = true },
                            modifier = Modifier.fillMaxWidth().testTag("clear_all_chats_settings_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear All Chat History")
                        }
                    }
                }
            }

            // 6. About Section
            SettingsSectionCard(title = "About", icon = Icons.Default.Info, isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "LearnMate AI",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Learn. Understand. Solve.",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) LiquidSky else Color(0xFF0284C7)
                    )
                    Text(
                        text = "Version 1.0.0 • Liquid Glass Edition",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Color(0x8894A3B8) else Color(0x8864748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "LearnMate AI is a student's AI world — a place to learn, understand, create, talk, play and discover.",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Powered by Google Gemini AI with multimodal photo homework understanding, adaptive student memory, and text-to-speech audio explanations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0x9994A3B8) else Color(0x9964748B)
                    )
                }
            }
        }
    }

    // Confirmation dialog for clearing memory
    if (showClearMemoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearMemoryConfirm = false },
            title = { Text("Clear All Memory?") },
            text = {
                Text("This will remove all remembered student facts, grade level, and subject preferences.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllMemories()
                        showClearMemoryConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearMemoryConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog for clearing all chat history
    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear All Chat History?") },
            text = {
                Text("Are you sure you want to permanently delete all $conversationsCount saved conversations and their messages? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllConversations()
                        showClearHistoryConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                isDark = isDark,
                elevation = 3.dp
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(LiquidCyan, LiquidIndigo)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}
