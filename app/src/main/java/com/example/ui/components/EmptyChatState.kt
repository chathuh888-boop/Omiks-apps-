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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidIndigo
import com.example.ui.theme.LiquidSky
import com.example.ui.theme.LiquidViolet

data class ExamplePrompt(
    val title: String,
    val icon: ImageVector,
    val prompt: String,
    val iconGradient: List<Color>
)

@Composable
fun EmptyChatState(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val examplePrompts = listOf(
        ExamplePrompt(
            "Explain black holes simply.",
            Icons.Default.Psychology,
            "Explain black holes simply.",
            listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
        ),
        ExamplePrompt(
            "Help me solve this equation.",
            Icons.Default.Functions,
            "Help me solve 3x + 5 = 20 step by step.",
            listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4))
        ),
        ExamplePrompt(
            "Why is the sky blue?",
            Icons.Default.WbSunny,
            "Why is the sky blue? Explain the science simply.",
            listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
        ),
        ExamplePrompt(
            "Teach me Python.",
            Icons.Default.Code,
            "Teach me Python basics with simple examples.",
            listOf(Color(0xFF10B981), Color(0xFF059669))
        ),
        ExamplePrompt(
            "Explain this image.",
            Icons.Default.Image,
            "Explain what is in this image and help me understand it.",
            listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Hero Liquid Badge with Glowing Rings
        Box(contentAlignment = Alignment.Center) {
            // Ambient outer soft glow
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isDark) LiquidCyan else LiquidIndigo).copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner Liquid Glass orb
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF06B6D4), Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color.White.copy(0.8f), Color.White.copy(0.2f))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "LearnMate Logo",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // App Title
        Text(
            text = "LearnMate AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle / Tagline in radiant cyan/indigo
        Text(
            text = "Learn. Understand. Solve.",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isDark) LiquidSky else Color(0xFF0284C7)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Core Student Philosophy Liquid Glass Pill
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0x2238BDF8) else Color(0x18BAE6FD))
                .border(1.dp, if (isDark) Color(0x3538BDF8) else Color(0x350284C7), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "A student's AI world — learn, understand, create, talk, play and discover.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                ),
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lead prompt
        Text(
            text = "Ask me anything.",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Example Prompts Header
        Text(
            text = "Try asking:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isDark) Color(0x9994A3B8) else Color(0x9964748B),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, start = 4.dp)
        )

        // Example Prompts List with Liquid Glass Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            examplePrompts.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(18.dp),
                            isDark = isDark,
                            elevation = 3.dp
                        )
                        .clickable { onSelectPrompt(item.prompt) }
                        .testTag("example_prompt_${item.title.take(10)}")
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(item.iconGradient))
                                    .border(1.dp, Color.White.copy(0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isDark) LiquidSky.copy(alpha = 0.7f) else Color(0xFF0284C7).copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
