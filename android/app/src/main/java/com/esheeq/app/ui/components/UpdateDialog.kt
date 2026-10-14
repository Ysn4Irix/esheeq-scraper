package com.esheeq.app.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.esheeq.app.data.model.DownloadState
import com.esheeq.app.data.model.UpdateInfo
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalRed
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalSurfaceElevated
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    downloadState: DownloadState,
    onStartDownload: () -> Unit,
    onInstallApk: (java.io.File) -> Unit,
    onOpenBrowser: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            if (downloadState !is DownloadState.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = downloadState !is DownloadState.Downloading,
            dismissOnClickOutside = downloadState !is DownloadState.Downloading
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(TerminalSurface)
                .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Terminal badge and dismiss icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalEmerald.copy(alpha = 0.15f))
                                .border(1.dp, TerminalEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = TerminalEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "تحديث جديد متاح",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerminalTextPrimary
                            )
                            Text(
                                text = "NEW_VERSION_AVAILABLE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TerminalTextMuted
                            )
                        }
                    }

                    if (downloadState !is DownloadState.Downloading) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = TerminalTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Version Delta Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalBg)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current version
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الإصدار الحالي",
                            fontSize = 11.sp,
                            color = TerminalTextMuted
                        )
                        Text(
                            text = "v${updateInfo.currentVersionName}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TerminalTextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TerminalBlue,
                        modifier = Modifier.size(18.dp)
                    )

                    // Latest version
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الإصدار الجديد",
                            fontSize = 11.sp,
                            color = TerminalEmerald
                        )
                        Text(
                            text = "v${updateInfo.latestVersionName}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalEmerald
                        )
                    }

                    // Size badge
                    if (updateInfo.formattedSize.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TerminalBlue.copy(alpha = 0.15f))
                                .border(1.dp, TerminalBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = updateInfo.formattedSize,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TerminalBlue
                            )
                        }
                    }
                }

                // Changelog / Release Notes
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ما الجديد في هذا التحديث:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TerminalTextPrimary
                    )

                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBg)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = updateInfo.releaseNotes.ifBlank { "تحسينات عامة وإصلاحات في الأداء." },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TerminalTextPrimary
                        )
                    }
                }

                // Download Progress or Status State
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalBg)
                                .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "جاري تنزيل التحديث...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TerminalTextPrimary
                                )
                                Text(
                                    text = "${(downloadState.progress * 100).toInt()}%",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerminalEmerald
                                )
                            }

                            LinearProgressIndicator(
                                progress = { downloadState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = TerminalEmerald,
                                trackColor = TerminalSurfaceElevated
                            )
                        }
                    }

                    is DownloadState.ReadyToInstall -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalEmerald.copy(alpha = 0.12f))
                                .border(1.dp, TerminalEmerald.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TerminalEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "تم تنزيل حزمة التحديث بنجاح! جاهز للتثبيت.",
                                fontSize = 12.sp,
                                color = TerminalEmerald
                            )
                        }
                    }

                    is DownloadState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalRed.copy(alpha = 0.12f))
                                .border(1.dp, TerminalRed.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = TerminalRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = downloadState.message,
                                fontSize = 11.sp,
                                color = TerminalRed
                            )
                        }
                    }

                    DownloadState.Idle -> {}
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (downloadState) {
                        is DownloadState.Idle -> {
                            // Later button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalSurfaceElevated)
                                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لاحقاً",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TerminalTextMuted
                                )
                            }

                            // Update Now button
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalEmerald)
                                    .clickable { onStartDownload() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "تحديث الآن",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        is DownloadState.Downloading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalSurfaceElevated)
                                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = TerminalEmerald,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "جاري التنزيل...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TerminalTextPrimary
                                    )
                                }
                            }
                        }

                        is DownloadState.ReadyToInstall -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalEmerald)
                                    .clickable { onInstallApk(downloadState.apkFile) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "تثبيت التحديث الآن",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        is DownloadState.Error -> {
                            // Retry button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalSurfaceElevated)
                                    .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                                    .clickable { onStartDownload() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "إعادة المحاولة",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TerminalTextPrimary
                                )
                            }

                            // Open Browser fallback button
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalBlue)
                                    .clickable { onOpenBrowser(updateInfo.htmlUrl) },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "تنزيل من المتصفح",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // External link helper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenBrowser(updateInfo.htmlUrl) }
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        tint = TerminalTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "عرض ملاحظات الإصدار على GitHub",
                        fontSize = 11.sp,
                        color = TerminalTextMuted
                    )
                }
            }
        }
    }
}
