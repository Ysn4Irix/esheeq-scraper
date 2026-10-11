package com.esheeq.app.ui.series

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.esheeq.app.data.model.DailyLink
import com.esheeq.app.data.model.SeriesDetails
import com.esheeq.app.ui.components.BadgeStyle
import com.esheeq.app.ui.components.StatusBadge
import com.esheeq.app.ui.components.TerminalTopBar
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBlueDim
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalEmeraldDim
import com.esheeq.app.ui.theme.TerminalRed
import com.esheeq.app.ui.theme.TerminalRedDim
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalSurfaceElevated
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeriesDetailScreen(
    seriesId: String,
    onBackClick: () -> Unit,
    onNavigateToPlayer: (url: String, title: String) -> Unit,
    viewModel: SeriesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(seriesId) {
        viewModel.loadSeries(seriesId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        TerminalTopBar(
            title = "قصة عشق",
            subTitle = "فهرس المسلسلات // بيانات العمل",
            showBackButton = true,
            onBackClick = onBackClick
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = TerminalBlue,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "[*] جاري استخراج بيانات العمل والحلقات...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TerminalTextMuted
                        )
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalRedDim)
                            .border(1.dp, TerminalRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "[!] خطأ: ${uiState.error}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TerminalRed
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TerminalSurface)
                                .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                                .clickable { viewModel.loadSeries(seriesId) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "إعادة المحاولة",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TerminalTextPrimary
                            )
                        }
                    }
                }
            }

            uiState.series != null -> {
                val series = uiState.series!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item {
                        SeriesInfoCard(series = series)
                    }

                    // Bulk Copy Bar
                    if (series.dailyLinks.isNotEmpty()) {
                        item {
                            BulkCopyBanner(
                                series = series,
                                onCopyAll = {
                                    copyAllLinks(context, series)
                                }
                            )
                        }
                    }

                    // Episodes Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "فهرس الحلقات // المشاهدة المباشرة",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerminalTextPrimary
                            )

                            StatusBadge(
                                text = "${series.dailyLinks.size} حلقة",
                                style = BadgeStyle.PRIMARY
                            )
                        }
                    }

                    // Episodes List
                    items(series.dailyLinks, key = { it.episode }) { item ->
                        EpisodeRowItem(
                            seriesTitle = series.title,
                            dailyLink = item,
                            onWatchClick = { url ->
                                onNavigateToPlayer(url, "${series.title} - الحلقة ${item.episode}")
                            },
                            onCopyClick = { url ->
                                copySingleLink(context, url)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeriesInfoCard(series: SeriesDetails) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Poster
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 128.dp)
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

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(TerminalBlueDim)
                            .border(1.dp, TerminalBlue.copy(alpha = 0.5f), RoundedCornerShape(topStart = 4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "1080p",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Metadata Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = series.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StatusBadge(
                            text = "● مكتمل",
                            style = BadgeStyle.SUCCESS
                        )
                        StatusBadge(
                            text = "${series.dailyLinks.size} حلقة",
                            style = BadgeStyle.PRIMARY
                        )
                        StatusBadge(
                            text = "مسلسل",
                            style = BadgeStyle.MUTED
                        )
                    }
                }
            }

            // Synopsis
            if (series.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(TerminalBg)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = series.description,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TerminalTextMuted
                    )
                }
            }

            // Cast chips
            if (series.actors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "طاقم العمل والتمثيل:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    series.actors.filter { it.isNotBlank() }.take(8).forEach { actor ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TerminalSurfaceElevated)
                                .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = actor.trim(),
                                fontSize = 11.sp,
                                color = TerminalTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkCopyBanner(
    series: SeriesDetails,
    onCopyAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalEmeraldDim)
            .border(1.dp, TerminalEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onCopyAll() }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "نسخ جميع الروابط دفعة واحدة (${series.dailyLinks.size} حلقة)",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalEmerald
            )
        }
    }
}

@Composable
private fun EpisodeRowItem(
    seriesTitle: String,
    dailyLink: DailyLink,
    onWatchClick: (String) -> Unit,
    onCopyClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                StatusBadge(
                    text = "حـ${dailyLink.episode}",
                    style = BadgeStyle.SUCCESS
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "الحلقة ${dailyLink.episode}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TerminalTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Watch CTA
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalEmerald)
                        .clickable {
                            val link = dailyLink.link ?: ""
                            if (link.isNotBlank()) onWatchClick(link)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
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
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "مشاهدة",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Copy CTA
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalBlueDim)
                        .border(1.dp, TerminalBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .clickable {
                            val link = dailyLink.link ?: ""
                            if (link.isNotBlank()) onCopyClick(link)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
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
                            text = "نسخ",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TerminalBlue
                        )
                    }
                }
            }
        }
    }
}

private fun copySingleLink(context: Context, link: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Esheeq Episode Link", link)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ رابط الحلقة بنجاح", Toast.LENGTH_SHORT).show()
}

private fun copyAllLinks(context: Context, series: SeriesDetails) {
    val builder = StringBuilder()
    builder.append("مسلسل: ${series.title}\n")
    builder.append("=========================\n")
    series.dailyLinks.forEach { item ->
        builder.append("الحلقة ${item.episode}: ${item.link ?: "غير متوفر"}\n")
    }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("All Esheeq Episodes", builder.toString())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ جميع روابط المسلسل (${series.dailyLinks.size} حلقة) بنجاح!", Toast.LENGTH_LONG).show()
}
