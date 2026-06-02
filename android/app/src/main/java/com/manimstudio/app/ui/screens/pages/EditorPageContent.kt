package com.manimstudio.app.ui.screens.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.data.models.StudioPhase

@Composable
fun EditorPageContent(
    code: String,
    onCodeChanged: (String) -> Unit,
    phase: StudioPhase,
    renderProgress: String,
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Editor background — slightly different from page bg
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
        )

        val scrollState = rememberScrollState()

        // Main scrollable editor
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = 96.dp, // clears top bar overlay
                    bottom = 280.dp, // clears render chips + input
                ),
        ) {
            // Line numbers column
            val lines = if (code.isEmpty()) listOf("") else code.lines()
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.End,
            ) {
                lines.forEachIndexed { i, _ ->
                    Text(
                        text = "${i + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        modifier = Modifier.padding(end = 14.dp),
                    )
                }
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp * lines.size.coerceAtLeast(1)) // explicitly heighted to cover all lines
                    .padding(vertical = 2.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
            )

            // Code input
            BasicTextField(
                value = code,
                onValueChange = onCodeChanged,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 16.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = PythonSyntaxTransformation(),
                decorationBox = { inner ->
                    if (code.isEmpty()) {
                        Text(
                            "# Write Manim code here\n# or describe what you want below",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        )
                    }
                    inner()
                },
            )
        }

        // Language badge (top-right of editor area, below top bar)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            ) {
                Text(
                    "Python",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp,
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        // Shimmer progress bar
        AnimatedVisibility(
            visible = phase == StudioPhase.RENDERING || phase == StudioPhase.GENERATING,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 84.dp, start = 16.dp, end = 16.dp),
        ) {
            ShimmerProgressBar(
                text = if (phase == StudioPhase.GENERATING) "Generating" else "Rendering",
                elapsed = elapsedSeconds,
                detail = renderProgress,
            )
        }

        // Console — pinned above render chips
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 168.dp), // above CompactRenderChips (50dp) + input (90dp) + gap
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                // Console header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Terminal, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            "CONSOLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        // Pulsing status dot
                        val dotTransition = rememberInfiniteTransition(label = "dot")
                        val dotAlpha by dotTransition.animateFloat(
                            initialValue = 1f, targetValue = 0.2f,
                            animationSpec = infiniteRepeatable(
                                tween(700), RepeatMode.Reverse
                            ), label = "dotA",
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    when (phase) {
                                        StudioPhase.RENDERING ->
                                            MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha)
                                        StudioPhase.DONE -> Color(0xFF4CAF50)
                                        StudioPhase.ERROR ->
                                            MaterialTheme.colorScheme.error
                                        else ->
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                    },
                                    CircleShape,
                                )
                        )
                        Text(
                            text = when (phase) {
                                StudioPhase.RENDERING -> "Rendering"
                                StudioPhase.GENERATING -> "Generating"
                                StudioPhase.DONE -> "Ready"
                                StudioPhase.ERROR -> "Error"
                                else -> "Idle"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (phase) {
                                StudioPhase.DONE -> Color(0xFF4CAF50)
                                StudioPhase.ERROR -> MaterialTheme.colorScheme.error
                                StudioPhase.RENDERING, StudioPhase.GENERATING ->
                                    MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            },
                            fontSize = 10.sp,
                        )
                    }
                }

                // Console output lines
                if (renderProgress.isNotEmpty()) {
                    Text(
                        text = renderProgress.takeLast(80),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = when {
                                renderProgress.contains("error", ignoreCase = true) ->
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                renderProgress.contains("warning", ignoreCase = true) ->
                                    Color(0xFFFFB74D)
                                else ->
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            },
                            lineHeight = 16.sp,
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Top fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                            Color.Transparent,
                        )
                    )
                )
        )
    }
}

class PythonSyntaxTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        // Keywords
        val keywords = listOf("from", "import", "class", "def", "self",
            "return", "if", "else", "for", "in", "True", "False", "None",
            "and", "or", "not", "while", "lambda", "pass", "break")
        val raw = text.text
        keywords.forEach { kw ->
            var idx = 0
            while (true) {
                idx = raw.indexOf(kw, idx)
                if (idx == -1) break
                val before = if (idx > 0) raw[idx - 1] else ' '
                val after = if (idx + kw.length < raw.length) raw[idx + kw.length] else ' '
                if (!before.isLetterOrDigit() && before != '_' &&
                    !after.isLetterOrDigit() && after != '_') {
                    builder.addStyle(
                        SpanStyle(color = Color(0xFFE8621A), fontWeight = FontWeight.SemiBold),
                        idx, idx + kw.length,
                    )
                }
                idx += kw.length
            }
        }
        // Strings — simple single-line
        val stringRegex = Regex("""(["'])(?:(?!\1)[^\\]|\\.)*\1""")
        stringRegex.findAll(raw).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF81C784)),
                match.range.first, match.range.last + 1,
            )
        }
        // Comments
        val commentRegex = Regex("""#.*""")
        commentRegex.findAll(raw).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF616161), fontStyle = FontStyle.Italic),
                match.range.first, match.range.last + 1,
            )
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

@Composable
fun ShimmerProgressBar(text: String, elapsed: Int, detail: String) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -300f, targetValue = 1500f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "shimmerX",
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary,
                            Color.Transparent,
                        ),
                        startX = shimmerX,
                        endX = shimmerX + 300f,
                    )
                )
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text("${elapsed}s", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace)
            if (detail.isNotEmpty()) {
                Text(
                    "· ${detail.takeLast(35)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
