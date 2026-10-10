package com.esheeq.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalEmeraldDim
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

@Composable
fun TerminalTopBar(
    title: String = "قصة عشق",
    subTitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .statusBarsPadding()
            .border(
                width = 1.dp,
                color = TerminalBorder,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = TerminalBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TerminalBlue,
                        fontSize = 20.sp
                    )
                    Text(
                        text = title,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = TerminalTextPrimary,
                        fontSize = 19.sp
                    )
                    Text(
                        text = "]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TerminalBlue,
                        fontSize = 20.sp
                    )
                }
            }

            // Pulsing live status indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalEmeraldDim)
                    .border(1.dp, TerminalEmerald.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(pulseAlpha)
                        .clip(CircleShape)
                        .background(TerminalEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "متصل // بث حي",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalEmerald,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (subTitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subTitle,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalTextMuted
            )
        }
    }
}
