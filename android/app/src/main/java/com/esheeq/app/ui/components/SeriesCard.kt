package com.esheeq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.esheeq.app.data.model.SearchResult
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

@Composable
fun SeriesCard(
    series: SearchResult,
    onClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .clickable { onClick(series) }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 75.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalBg)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
            ) {
                if (series.cover.isNotEmpty()) {
                    AsyncImage(
                        model = series.cover,
                        contentDescription = series.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = series.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TerminalTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = "[مسلسل]",
                        style = BadgeStyle.PRIMARY
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1080p FHD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TerminalTextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "عرض التفاصيل",
                tint = TerminalBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
