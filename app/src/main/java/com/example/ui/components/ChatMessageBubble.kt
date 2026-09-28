package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MessageEntity
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidIndigo
import com.example.ui.theme.LiquidSky
import com.example.ui.theme.LiquidUserGradientDark
import com.example.ui.theme.LiquidUserGradientLight
import com.example.ui.theme.LiquidViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    message: MessageEntity,
    isPlayingAudio: Boolean,
    onSpeakClick: () -> Unit,
    onStopAudioClick: () -> Unit,
    onExplainDifferentlyClick: () -> Unit,
    onDeleteMessageClick: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isUser) {
            UserMessageBubble(
                message = message,
                onDeleteMessageClick = onDeleteMessageClick,
                onImageClick = onImageClick
            )
        } else {
            AssistantMessageBubble(
                message = message,
                isPlayingAudio = isPlayingAudio,
                onSpeakClick = onSpeakClick,
                onStopAudioClick = onStopAudioClick,
                onExplainDifferentlyClick = onExplainDifferentlyClick,
                onDeleteMessageClick = onDeleteMessageClick
            )
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: MessageEntity,
    onDeleteMessageClick: ((String) -> Unit)?,
    onImageClick: ((String) -> Unit)?
) {
    val isDark = isSystemInDarkTheme()
    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 22.dp,
        topEnd = 22.dp,
        bottomStart = 22.dp,
        bottomEnd = 5.dp
    )

    Column(
        modifier = Modifier.widthIn(max = 310.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Optional explanation style indicator
        if (!message.explanationStyle.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Color(0x33818CF8) else Color(0x286366F1))
                    .border(1.dp, if (isDark) Color(0x44818CF8) else Color(0x336366F1), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = if (isDark) LiquidSky else LiquidIndigo,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = message.explanationStyle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) LiquidSky else LiquidIndigo,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Liquid User Bubble
        val userGradient = if (isDark) LiquidUserGradientDark else LiquidUserGradientLight
        val specularTop = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.2f),
                Color.Transparent
            ),
            start = Offset(0f, 0f),
            end = Offset(250f, 250f)
        )

        Box(
            modifier = Modifier
                .shadow(6.dp, shape = bubbleShape)
                .clip(bubbleShape)
                .background(userGradient)
                .border(1.2.dp, specularTop, bubbleShape)
                .padding(14.dp)
        ) {
            Column {
                // Attached image if present
                if (!message.imageUri.isNullOrBlank()) {
                    val file = remember(message.imageUri) { File(message.imageUri) }
                    AsyncImage(
                        model = file,
                        contentDescription = "Attached homework or diagram",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { onImageClick?.invoke(message.imageUri) }
                    )
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Timestamp & Delete Action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 3.dp, end = 4.dp)
        ) {
            if (onDeleteMessageClick != null) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete question",
                    tint = if (isDark) Color(0x77EF4444) else Color(0x99DC2626),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onDeleteMessageClick(message.id) }
                )
            }
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isDark) Color(0x8894A3B8) else Color(0x8864748B)
            )
        }
    }
}

@Composable
private fun AssistantMessageBubble(
    message: MessageEntity,
    isPlayingAudio: Boolean,
    onSpeakClick: () -> Unit,
    onStopAudioClick: () -> Unit,
    onExplainDifferentlyClick: () -> Unit,
    onDeleteMessageClick: ((String) -> Unit)?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    var isCopied by remember { mutableStateOf(false) }

    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 5.dp,
        topEnd = 22.dp,
        bottomStart = 22.dp,
        bottomEnd = 22.dp
    )

    Column(
        modifier = Modifier.fillMaxWidth(0.94f),
        horizontalAlignment = Alignment.Start
    ) {
        // AI Header with glowing badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF06B6D4), Color(0xFF6366F1))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = "LearnMate AI",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) LiquidSky else Color(0xFF0369A1)
            )

            if (!message.explanationStyle.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0x3038BDF8) else Color(0x200284C7))
                        .border(1.dp, if (isDark) Color(0x4038BDF8) else Color(0x300284C7), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = message.explanationStyle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isDark) LiquidSky else Color(0xFF0284C7),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Frosted Liquid Glass Assistant Bubble
        Box(
            modifier = Modifier
                .liquidGlass(
                    shape = bubbleShape,
                    isDark = isDark,
                    elevation = 4.dp
                )
                .padding(15.dp)
        ) {
            MarkdownContent(
                content = message.content,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        }

        // Frosted Glass Action Toolbar: [🔊 Listen] [🔄 Explain differently] [📋 Copy]
        val glassChipBg = if (isDark) Color(0x2038BDF8) else Color(0x180284C7)
        val glassChipBorder = if (isDark) Color(0x3038BDF8) else Color(0x240284C7)

        Row(
            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Read Aloud / Stop button
            val audioTint by animateColorAsState(
                targetValue = if (isPlayingAudio) (if (isDark) LiquidSky else LiquidIndigo) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                label = "AudioTint"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(glassChipBg)
                    .border(1.dp, glassChipBorder, RoundedCornerShape(10.dp))
                    .clickable {
                        if (isPlayingAudio) onStopAudioClick() else onSpeakClick()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isPlayingAudio) "Stop audio" else "Read aloud",
                    tint = audioTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isPlayingAudio) "Stop" else "Listen",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = audioTint
                )
            }

            // Explain differently button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(glassChipBg)
                    .border(1.dp, glassChipBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onExplainDifferentlyClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Explain differently",
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Explain differently",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            // Copy button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(glassChipBg)
                    .border(1.dp, glassChipBorder, RoundedCornerShape(10.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("LearnMate AI Answer", message.content)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy message",
                    tint = if (isCopied) Color(0xFF10B981) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isCopied) "Copied" else "Copy",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isCopied) Color(0xFF10B981) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                )
            }

            // Delete message button
            if (onDeleteMessageClick != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0x20EF4444) else Color(0x18EF4444))
                        .border(1.dp, if (isDark) Color(0x35EF4444) else Color(0x25EF4444), RoundedCornerShape(10.dp))
                        .clickable { onDeleteMessageClick(message.id) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete explanation",
                        tint = if (isDark) Color(0xAAEF4444) else Color(0xCCDC2626),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDark) Color(0xAAEF4444) else Color(0xCCDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Timestamp
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isDark) Color(0x6694A3B8) else Color(0x6664748B)
            )
        }
    }
}

@Composable
fun StreamingMessageBubble(
    streamingContent: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Breathing glow animation for streaming bubble
    val transition = rememberInfiniteTransition(label = "StreamingGlow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    val streamingBorder = Brush.linearGradient(
        listOf(
            LiquidCyan.copy(alpha = glowAlpha),
            LiquidViolet.copy(alpha = glowAlpha * 0.7f),
            LiquidSky.copy(alpha = glowAlpha)
        )
    )

    val bubbleShape = RoundedCornerShape(
        topStart = 5.dp,
        topEnd = 22.dp,
        bottomStart = 22.dp,
        bottomEnd = 22.dp
    )

    Column(
        modifier = modifier
            .fillMaxWidth(0.94f)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // AI Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF06B6D4), Color(0xFF6366F1))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = "LearnMate AI is thinking...",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) LiquidSky else LiquidIndigo
            )
            CircularProgressIndicator(
                modifier = Modifier.size(13.dp),
                strokeWidth = 2.dp,
                color = if (isDark) LiquidSky else LiquidIndigo
            )
        }

        // Assistant Bubble with animated border
        Box(
            modifier = Modifier
                .shadow(6.dp, shape = bubbleShape)
                .clip(bubbleShape)
                .background(
                    if (isDark) Color(0xA6111C33) else Color(0xD8FFFFFF),
                    bubbleShape
                )
                .border(1.5.dp, streamingBorder, bubbleShape)
                .padding(15.dp)
        ) {
            if (streamingContent.isNotBlank()) {
                MarkdownContent(
                    content = streamingContent,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = "Understanding your question and preparing explanation...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
