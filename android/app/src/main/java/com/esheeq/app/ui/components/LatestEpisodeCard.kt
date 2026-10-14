package com.esheeq.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.esheeq.app.data.repository.PlaybackHistoryManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.esheeq.app.data.model.LatestEpisode
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalSurfaceElevated
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

@Composable
fun LatestEpisodeCard(
    episode: LatestEpisode,
    onWatchClick: (LatestEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyMap by PlaybackHistoryManager.historyState.collectAsState()
    val targetUrl = episode.streamLink ?: episode.url
    val watchProgress = historyMap[targetUrl]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode Poster (True 3:4 portrait ratio for TV series)
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 88.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBg)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                    .clickable { onWatchClick(episode) }
            ) {
                if (episode.thumb.isNotEmpty()) {
                    AsyncImage(
                        model = episode.thumb,
                        contentDescription = episode.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Scrim gradient for contrast
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xB3000000)),
                                startY = 40f
                            )
                        )
                )

                if (episode.episode.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = if (watchProgress != null && watchProgress.progressFraction > 0.03f) 5.dp else 3.dp, end = 3.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xD90D1117))
                            .border(0.5.dp, TerminalEmerald.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "حـ${episode.episode}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalEmerald
                        )
                    }
                }

                // Progress bar along bottom of poster
                if (watchProgress != null && watchProgress.progressFraction > 0.03f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(watchProgress.progressFraction)
                                .fillMaxHeight()
                                .background(if (watchProgress.isCompleted) TerminalBlue else TerminalEmerald)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info Column in the center
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onWatchClick(episode) }
            ) {
                Text(
                    text = episode.seriesTitle.ifEmpty { episode.title },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TerminalTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadge(
                        text = if (episode.episode.isNotEmpty()) "الحلقة ${episode.episode}" else "حلقة جديدة",
                        style = BadgeStyle.PRIMARY
                    )

                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = TerminalTextMuted
                    )

                    Text(
                        text = "1080p FHD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "سيرفر سريع • جودة أصلية",
                    fontSize = 11.sp,
                    color = TerminalTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Direct Actions Column on the LEFT (Eliminating the empty space)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Watch CTA (Primary action)
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalEmerald)
                        .clickable { onWatchClick(episode) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "مشاهدة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Copy CTA (Secondary action)
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurfaceElevated)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            val linkToCopy = episode.streamLink ?: episode.url
                            copyToClipboard(context, linkToCopy)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = TerminalBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "نسخ الرابط",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TerminalBlue
                        )
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Esheeq Episode Link", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ الرابط بنجاح للحافظة", Toast.LENGTH_SHORT).show()
}
