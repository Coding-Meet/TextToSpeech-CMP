package com.example.texttospeech

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun HighlightedText(
    text: String,
    highlightRange: IntRange,
    modifier: Modifier = Modifier,
    normalTextColor: Color = MaterialTheme.colorScheme.onSurface,
    highlightColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    highlightTextColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val annotatedString = buildAnnotatedString {
        // Check if we have a valid highlight range
        if (highlightRange.first >= 0 &&
            highlightRange.last >= highlightRange.first &&
            highlightRange.first < text.length
        ) {

            val safeStart = maxOf(0, highlightRange.first)
            val safeEnd = minOf(text.length - 1, highlightRange.last)

            // Text before highlight
            if (safeStart > 0) {
                withStyle(SpanStyle(color = normalTextColor)) {
                    append(text.substring(0, safeStart))
                }
            }

            // Highlighted text with enhanced styling
            if (safeStart <= safeEnd) {
                withStyle(
                    SpanStyle(
                        background = highlightColor,
                        color = highlightTextColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                ) {
                    append(text.substring(safeStart, safeEnd + 1))
                }
            }

            // Text after highlight
            if (safeEnd + 1 < text.length) {
                withStyle(SpanStyle(color = normalTextColor)) {
                    append(text.substring(safeEnd + 1))
                }
            }
        } else {
            // No highlight, show normal text
            withStyle(SpanStyle(color = normalTextColor)) {
                append(text)
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = 16.sp,
        lineHeight = 28.sp,
        style = MaterialTheme.typography.bodyLarge
    )
}