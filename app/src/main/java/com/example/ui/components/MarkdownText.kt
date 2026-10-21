package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class CodeBlock(val code: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    object BlankLine : MarkdownBlock()
}

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 24.sp
) {
    val blocks = parseMarkdownBlocks(markdown)

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    if (index > 0) Spacer(modifier = Modifier.height(6.dp))
                    val headingFontSize = when (block.level) {
                        1 -> 22.sp
                        2 -> 19.sp
                        else -> 17.sp
                    }
                    Text(
                        text = parseInlineMarkdown(block.text, color),
                        fontSize = headingFontSize,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        lineHeight = (headingFontSize.value * 1.35).sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(end = 8.dp, start = 2.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, color),
                            fontSize = fontSize,
                            color = color,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = block.number,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, color),
                            fontSize = fontSize,
                            color = color,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = block.code,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = color,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                is MarkdownBlock.Paragraph -> {
                    if (block.text.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(block.text, color),
                            fontSize = fontSize,
                            color = color,
                            lineHeight = lineHeight,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                is MarkdownBlock.BlankLine -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.split("\n")
    var inCodeBlock = false
    val codeBuffer = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.CodeBlock(codeBuffer.toString().trimEnd()))
                codeBuffer.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
            }
            continue
        }

        if (inCodeBlock) {
            codeBuffer.append(line).append("\n")
            continue
        }

        if (trimmed.isEmpty()) {
            if (blocks.isNotEmpty() && blocks.last() !is MarkdownBlock.BlankLine) {
                blocks.add(MarkdownBlock.BlankLine)
            }
            continue
        }

        // Heading detection: #, ##, ###, ####
        val headingMatch = Regex("""^(#{1,6})\s+(.*)$""").find(trimmed)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val text = headingMatch.groupValues[2]
            blocks.add(MarkdownBlock.Heading(level, text))
            continue
        }

        // Bullet list detection: * item, - item, + item
        val bulletMatch = Regex("""^[*+\-]\s+(.*)$""").find(trimmed)
        if (bulletMatch != null) {
            val text = bulletMatch.groupValues[1]
            blocks.add(MarkdownBlock.BulletItem(text))
            continue
        }

        // Numbered list detection: 1. item, 2. item, etc.
        val numberMatch = Regex("""^(\d+[\.\)])\s+(.*)$""").find(trimmed)
        if (numberMatch != null) {
            val number = numberMatch.groupValues[1]
            val text = numberMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(number, text))
            continue
        }

        // Regular Paragraph
        blocks.add(MarkdownBlock.Paragraph(line))
    }

    return blocks
}

fun parseInlineMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        // Match bold (**text** or __text__), inline code (`text`), or italic (*text* or _text_)
        val pattern = Regex("""(\*\*|__|`|\*|_)([\s\S]+?)\1""")
        val matches = pattern.findAll(text)

        for (match in matches) {
            if (match.range.first > currentIndex) {
                append(text.substring(currentIndex, match.range.first))
            }

            val token = match.groupValues[1]
            val content = match.groupValues[2]

            when (token) {
                "**", "__" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(content)
                    }
                }
                "*", "_" -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(content)
                    }
                }
                "`" -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = defaultColor.copy(alpha = 0.10f),
                            color = defaultColor
                        )
                    ) {
                        append(content)
                    }
                }
                else -> append(match.value)
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
