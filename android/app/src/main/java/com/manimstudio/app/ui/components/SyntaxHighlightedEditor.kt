package com.manimstudio.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.isSystemInDarkTheme
import com.manimstudio.app.ui.components.editor.CodeCompletionEngine
import com.manimstudio.app.ui.components.editor.Completion
import com.manimstudio.app.ui.components.editor.CompletionType
import com.manimstudio.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SyntaxHighlightedEditor(
    code: String,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(code, TextRange(code.length)))
    }
    var completions by remember { mutableStateOf(emptyList<Completion>()) }
    var showCompletions by remember { mutableStateOf(false) }
    var currentWord by remember { mutableStateOf("") }

    // Debounce completion lookup
    LaunchedEffect(textFieldValue.text, textFieldValue.selection.start) {
        delay(150) // debounce
        val cursor = textFieldValue.selection.start
        currentWord = CodeCompletionEngine.getCurrentWord(textFieldValue.text, cursor)
        completions = if (currentWord.length >= 2) {
            CodeCompletionEngine.getSuggestions(currentWord, textFieldValue.text, 0)
        } else emptyList()
        showCompletions = completions.isNotEmpty()
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                val lineCount = textFieldValue.text.lines().size
                repeat(lineCount) { i ->
                    Text(
                        text = "${i + 1}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            textAlign = TextAlign.End,
                        ),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            // Code input
            BasicTextField(
                value = textFieldValue,
                onValueChange = { new ->
                    val lastChar = new.text.take(new.selection.start).lastOrNull()
                    val prevLength = textFieldValue.text.length
                    
                    if (new.text.length > prevLength && lastChar == '(') {
                        val beforeCursor = new.text.substring(0, new.selection.start)
                        val afterCursor = new.text.substring(new.selection.start)
                        val insertedText = beforeCursor + ")" + afterCursor
                        textFieldValue = TextFieldValue(
                            text = insertedText,
                            selection = TextRange(new.selection.start)
                        )
                        onCodeChanged(insertedText)
                    } else {
                        textFieldValue = new
                        onCodeChanged(new.text)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, end = 16.dp, bottom = 200.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = SyntaxHighlightTransformation(
                    isDark = isSystemInDarkTheme(),
                ),
            )
        }

        // Completion popup — anchored to bottom-left of editor
        AnimatedVisibility(
            visible = showCompletions,
            enter = fadeIn(tween(120)) + expandVertically(
                expandFrom = Alignment.Bottom,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            ),
            exit = fadeOut(tween(80)) + shrinkVertically(shrinkTowards = Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 52.dp, bottom = 210.dp)
                .widthIn(max = 300.dp)
                .zIndex(100f),
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column {
                    completions.take(5).forEach { completion ->
                        CompletionItem(
                            completion = completion,
                            onClick = {
                                val cursor = textFieldValue.selection.start
                                val word = CodeCompletionEngine.getCurrentWord(
                                    textFieldValue.text, cursor)
                                val insertText = completion.expandTo ?: completion.label
                                val before = textFieldValue.text.substring(0, cursor - word.length)
                                val after = textFieldValue.text.substring(cursor)
                                val newText = before + insertText + after
                                val newCursor = before.length + insertText.length
                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newCursor),
                                )
                                onCodeChanged(newText)
                                showCompletions = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionItem(
    completion: Completion,
    onClick: () -> Unit,
) {
    val typeColor = when (completion.type) {
        CompletionType.CLASS -> Color(0xFF64B5F6)
        CompletionType.FUNCTION -> Color(0xFFFFB74D)
        CompletionType.CONSTANT -> Color(0xFFBA68C8)
        CompletionType.SNIPPET -> Color(0xFF81C784)
        CompletionType.KEYWORD -> CodeKeyword
    }
    val typeLabel = when (completion.type) {
        CompletionType.CLASS -> "cls"
        CompletionType.FUNCTION -> "fn"
        CompletionType.CONSTANT -> "const"
        CompletionType.SNIPPET -> "snip"
        CompletionType.KEYWORD -> "kw"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = typeLabel,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = typeColor,
            ),
            modifier = Modifier
                .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = completion.label,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (completion.description.isNotEmpty()) {
                Text(
                    text = completion.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

class SyntaxHighlightTransformation(val isDark: Boolean) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            highlightPython(text.text),
            OffsetMapping.Identity
        )
    }
}

fun highlightPython(code: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = code.split("\n")
        lines.forEachIndexed { lineIndex, line ->
            val tokens = tokenizePythonLine(line)
            tokens.forEach { (token, type) ->
                when (type) {
                    TokenType.KEYWORD -> withStyle(SpanStyle(color = CodeKeyword,
                        fontWeight = FontWeight.Bold)) { append(token) }
                    TokenType.MANIM_TYPE -> withStyle(SpanStyle(color = CodeType)) {
                        append(token) }
                    TokenType.STRING -> withStyle(SpanStyle(color = CodeString)) {
                        append(token) }
                    TokenType.COMMENT -> withStyle(SpanStyle(color = CodeComment,
                        fontStyle = FontStyle.Italic)) { append(token) }
                    TokenType.NUMBER -> withStyle(SpanStyle(color = CodeNumber)) {
                        append(token) }
                    TokenType.FUNCTION -> withStyle(SpanStyle(color = CodeFunction)) {
                        append(token) }
                    else -> withStyle(SpanStyle(color = CodeDefault)) { append(token) }
                }
            }
            if (lineIndex < lines.size - 1) append("\n")
        }
    }
}

// Simple tokenizer for Python
enum class TokenType { DEFAULT, KEYWORD, MANIM_TYPE, STRING, COMMENT, NUMBER, FUNCTION }

fun tokenizePythonLine(line: String): List<Pair<String, TokenType>> {
    val result = mutableListOf<Pair<String, TokenType>>()
    if (line.trim().startsWith("#")) {
        result.add(line to TokenType.COMMENT)
        return result
    }

    val keywords = setOf("from", "import", "class", "def", "self", "return", "if", "else", "elif", "for", "while", "in", "True", "False", "None", "and", "or", "not", "lambda")
    val manimTypes = setOf("Scene", "Text", "Circle", "Square", "Triangle", "Arrow", "Line", "Axes", "MathTex", "Tex", "VGroup", "Dot", "Rectangle", "Polygon", "FadeIn", "FadeOut", "Create", "Write", "Transform", "GrowArrow", "NumberPlane", "MovingCameraScene", "ThreeDScene")

    var current = ""
    var i = 0
    while (i < line.length) {
        val char = line[i]
        
        when {
            char.isLetter() || char == '_' -> {
                while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) {
                    current += line[i]
                    i++
                }
                val type = when {
                    current in keywords -> TokenType.KEYWORD
                    current in manimTypes -> TokenType.MANIM_TYPE
                    i < line.length && line[i] == '(' -> TokenType.FUNCTION
                    else -> TokenType.DEFAULT
                }
                result.add(current to type)
                current = ""
                i-- // adjust for outer loop
            }
            char == '"' || char == '\'' -> {
                current += char
                i++
                while (i < line.length && line[i] != char) {
                    current += line[i]
                    i++
                }
                if (i < line.length) current += line[i]
                result.add(current to TokenType.STRING)
                current = ""
            }
            char == '#' -> {
                result.add(line.substring(i) to TokenType.COMMENT)
                i = line.length
            }
            char.isDigit() -> {
                while (i < line.length && (line[i].isDigit() || line[i] == '.')) {
                    current += line[i]
                    i++
                }
                result.add(current to TokenType.NUMBER)
                current = ""
                i--
            }
            else -> {
                result.add(char.toString() to TokenType.DEFAULT)
            }
        }
        i++
    }
    return result
}
