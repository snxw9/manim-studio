package com.manimstudio.app.ui.screens.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
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
        // Subtle horizontal line grid — like a notebook/editor
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineHeight = 22.dp.toPx() // match text lineHeight
            val lineColor = Color.White.copy(alpha = 0.025f)
            var y = 96.dp.toPx() // start after the top bar fade area
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.5.dp.toPx(),
                )
                y += lineHeight
            }
        }

        // Code editor — scrollable, fills screen
        // Uses a Column inside a verticalScroll so content isn't collapsed
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 56.dp), // just enough to clear console strip
        ) {
            // Top spacer to clear the overlay top bar
            Spacer(modifier = Modifier.height(96.dp))

            // Line numbers + code side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
            ) {
                // Line numbers
                val lines = code.lines()
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .padding(top = 2.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    lines.forEachIndexed { i, _ ->
                        Text(
                            text = "${i + 1}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    }
                }

                // Code field
                BasicTextField(
                    value = code,
                    onValueChange = onCodeChanged,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    // Simple visual transform for syntax colors
                    visualTransformation = PythonSyntaxTransformation(),
                    decorationBox = { innerTextField ->
                        if (code.isEmpty()) {
                            Text(
                                "# Write Manim code here",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            )
                        }
                        innerTextField()
                    },
                )
            }
        }

        // Shimmer progress bar at top (under nav bar overlay)
        AnimatedVisibility(
            visible = phase == StudioPhase.RENDERING || phase == StudioPhase.GENERATING,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 88.dp, start = 16.dp, end = 16.dp),
        ) {
            ShimmerProgressBar(
                text = if (phase == StudioPhase.GENERATING) "Generating" else "Rendering",
                elapsed = elapsedSeconds,
                detail = renderProgress,
            )
        }

        // Console bar — pinned at the very bottom of the editor page content
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp), // just above page bottom to feel clean
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.Terminal, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        "CONSOLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 0.8.sp,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    val transition = rememberInfiniteTransition(label = "dot")
                    val dotAlpha by transition.animateFloat(
                        initialValue = 1f, targetValue = 0.2f,
                        animationSpec = infiniteRepeatable(
                            tween(700, easing = EaseInOutSine), RepeatMode.Reverse
                        ), label = "dotAlpha",
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = when (phase) {
                                    StudioPhase.RENDERING -> MaterialTheme.colorScheme.primary
                                        .copy(alpha = dotAlpha)
                                    StudioPhase.DONE -> Color(0xFF4CAF50)
                                    StudioPhase.ERROR -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                },
                                shape = CircleShape,
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }

        // Top fade overlay (so top bar icons stay readable over code)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
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
