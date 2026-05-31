package com.manimstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.app.ui.theme.*

@Composable
fun SyntaxHighlightedEditor(
    code: String,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlightedCode = remember(code) { highlightPython(code) }

    BasicTextField(
        value = TextFieldValue(
            annotatedString = highlightedCode,
            selection = TextRange(code.length),
        ),
        onValueChange = { onCodeChanged(it.text) },
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        textStyle = TextStyle(
            fontFamily = CodeFontFamily,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            color = CodeDefault,
        ),
        cursorBrush = SolidColor(Primary),
        decorationBox = { innerTextField ->
            // Line numbers on left
            Row {
                LineNumbers(lineCount = code.lines().size)
                Spacer(modifier = Modifier.width(16.dp))
                innerTextField()
            }
        }
    )
}

fun highlightPython(code: String): AnnotatedString {
    return buildAnnotatedString {
        val keywords = setOf("from", "import", "class", "def", "self",
            "return", "if", "else", "elif", "for", "while", "in",
            "True", "False", "None", "and", "or", "not", "lambda")
        val manimTypes = setOf("Scene", "Text", "Circle", "Square",
            "Triangle", "Arrow", "Line", "Axes", "MathTex", "Tex",
            "VGroup", "Dot", "Rectangle", "Polygon", "FadeIn",
            "FadeOut", "Create", "Write", "Transform", "GrowArrow",
            "NumberPlane", "MovingCameraScene", "ThreeDScene")

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

@Composable
fun LineNumbers(lineCount: Int) {
    Column(
        modifier = Modifier.width(32.dp),
        horizontalAlignment = Alignment.End,
    ) {
        repeat(lineCount) { index ->
            Text(
                text = "${index + 1}",
                style = TextStyle(
                    fontFamily = CodeFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    color = OnSurfaceDim,
                    textAlign = TextAlign.End,
                ),
            )
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
                val quote = char
                current += char
                i++
                while (i < line.length && line[i] != quote) {
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
