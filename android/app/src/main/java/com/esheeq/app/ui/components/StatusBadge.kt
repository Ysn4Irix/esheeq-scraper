package com.esheeq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esheeq.app.ui.theme.EsheeqTheme

enum class BadgeStyle {
    PRIMARY,
    SUCCESS,
    MUTED,
    WARNING
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    style: BadgeStyle = BadgeStyle.MUTED,
    showDot: Boolean = false
) {
    val colors = EsheeqTheme.colors

    val (bgColor, borderColor, textColor, dotColor) = when (style) {
        BadgeStyle.PRIMARY -> Quad(
            colors.blueDim,
            colors.blue.copy(alpha = 0.4f),
            colors.blue,
            colors.blue
        )
        BadgeStyle.SUCCESS -> Quad(
            colors.emeraldDim,
            colors.emerald.copy(alpha = 0.4f),
            colors.emerald,
            colors.emerald
        )
        BadgeStyle.MUTED -> Quad(
            colors.surfaceElevated,
            colors.border,
            colors.textMuted,
            colors.textMuted
        )
        BadgeStyle.WARNING -> Quad(
            colors.warningDim,
            colors.warning.copy(alpha = 0.4f),
            colors.warningText,
            colors.warningText
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
