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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.local.MessageEntity
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidIndigo
import com.example.ui.theme.LiquidSky
import com.example.ui.theme.LiquidViolet

data class ExplainStyleOption(
    val label: String,
    val description: String,
    val promptModifier: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplainDifferentlyBottomSheet(
    targetMessage: MessageEntity,
    onDismiss: () -> Unit,
    onSelectStyle: (styleName: String, promptModifier: String) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val styles = listOf(
        ExplainStyleOption(
            label = "Explain Like I'm 5 (ELI5)",
            description = "Super simple terms, fun everyday analogies, zero jargon",
            promptModifier = "Explain this concept in an extremely simple, engaging way as if explaining to a 5-year-old child using everyday household analogies.",
            icon = Icons.Default.ChildCare,
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
        ),
        ExplainStyleOption(
            label = "Use a Real-World Analogy",
            description = "Relate the concept to cars, cooking, sports, or nature",
            promptModifier = "Explain this concept using a vivid, relatable real-world metaphor or story (like cooking, driving a car, or nature) that makes it click immediately.",
            icon = Icons.Default.Lightbulb,
            gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
        ),
        ExplainStyleOption(
            label = "Step-by-Step Breakdown",
            description = "Numbered walk-through showing exactly how it works",
            promptModifier = "Break down this explanation into clear, sequential, numbered steps from start to finish.",
            icon = Icons.Default.FormatListNumbered,
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669))
        ),
        ExplainStyleOption(
            label = "Socratic / Guided Questions",
            description = "Guide your thinking with thought-provoking questions",
            promptModifier = "Use the Socratic method to guide my understanding of this topic: ask 2-3 intuitive leading questions with explanations that help me discover the answer myself.",
            icon = Icons.Default.QuestionMark,
            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
        ),
        ExplainStyleOption(
            label = "Visual / ASCII Diagram Style",
            description = "Structured layout, comparisons, and visual tables",
            promptModifier = "Explain this using clean structured ASCII diagrams, visual tables, and flow arrows where helpful.",
            icon = Icons.Default.Schema,
            gradientColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
        ),
        ExplainStyleOption(
            label = "Concise Key Takeaways",
            description = "Short summary focusing on the essential points",
            promptModifier = "Provide a direct, concise summary highlighting only the core facts and takeaways.",
            icon = Icons.AutoMirrored.Filled.ShortText,
            gradientColors = listOf(Color(0xFF0EA5E9), Color(0xFF6366F1))
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = if (isDark) Color(0xF40D1526) else Color(0xF8FFFFFF),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(LiquidCyan, LiquidIndigo))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Explain Differently",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Choose an alternative perspective to help you understand this concept:",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                styles.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = RoundedCornerShape(16.dp),
                                isDark = isDark,
                                elevation = 2.dp
                            )
                            .clickable {
                                onSelectStyle(option.label, option.promptModifier)
                            }
                            .testTag("style_option_${option.label.take(8)}")
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(option.gradientColors))
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color(0xAA94A3B8) else Color(0xAA64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
