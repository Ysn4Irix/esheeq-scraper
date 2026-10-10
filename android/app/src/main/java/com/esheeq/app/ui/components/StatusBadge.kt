package com.esheeq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBlueDim
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalEmeraldDim
import com.esheeq.app.ui.theme.TerminalSurfaceElevated
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

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
    val (bgColor, borderColor, textColor, dotColor) = when (style) {
        BadgeStyle.PRIMARY -> Quad(
            TerminalBlueDim,
            TerminalBlue.copy(alpha = 0.4f),
            TerminalBlue,
            TerminalBlue
        )
        BadgeStyle.SUCCESS -> Quad(
            TerminalEmeraldDim,
            TerminalEmerald.copy(alpha = 0.4f),
            TerminalEmerald,
            TerminalEmerald
        )
        BadgeStyle.MUTED -> Quad(
            TerminalSurfaceElevated,
            Color(0xFF30363D),
            TerminalTextMuted,
            TerminalTextMuted
        )
        BadgeStyle.WARNING -> Quad(
            Color(0x26D29922),
            Color(0x66D29922),
            Color(0xFFE3B341),
            Color(0xFFE3B341)
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
            androidx.compose.foundation.layout.Box(
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
