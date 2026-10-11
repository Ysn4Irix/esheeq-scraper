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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.esheeq.app.ui.theme.TerminalBlueDim
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalEmeraldDim
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

@Composable
fun LatestEpisodeCard(
    episode: LatestEpisode,
    onWatchClick: (LatestEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode Thumbnail with episode number overlay
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalBg)
                    .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
            ) {
                if (episode.thumb.isNotEmpty()) {
                    AsyncImage(
                        model = episode.thumb,
                        contentDescription = episode.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (episode.episode.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(TerminalEmeraldDim)
                            .border(1.dp, TerminalEmerald.copy(alpha = 0.5f), RoundedCornerShape(topStart = 4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
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
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = episode.seriesTitle.ifEmpty { episode.title },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TerminalTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (episode.episode.isNotEmpty()) "الحلقة ${episode.episode}" else episode.title,
                    fontSize = 12.sp,
                    color = TerminalTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions: Watch & Copy
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Watch CTA
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalEmeraldDim)
                            .border(1.dp, TerminalEmerald.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable { onWatchClick(episode) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[مشاهدة]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalEmerald
                        )
                    }

                    // Copy CTA
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalBlueDim)
                            .border(1.dp, TerminalBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable {
                                val linkToCopy = episode.streamLink ?: episode.url
                                copyToClipboard(context, linkToCopy)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[نسخ]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
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
