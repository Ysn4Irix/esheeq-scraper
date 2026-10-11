package com.esheeq.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esheeq.app.data.api.ApiClient
import com.esheeq.app.data.model.LatestEpisode
import com.esheeq.app.data.model.SearchResult
import com.esheeq.app.ui.components.BadgeStyle
import com.esheeq.app.ui.components.LatestEpisodeCard
import com.esheeq.app.ui.components.SearchBarComponent
import com.esheeq.app.ui.components.SeriesCard
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

@Composable
fun HomeScreen(
    onNavigateToSeries: (String) -> Unit,
    onNavigateToPlayer: (url: String, title: String) -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showServerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        TerminalTopBar(
            title = "قصة عشق"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar item
            item {
                SearchBarComponent(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged
                )
            }

            // If user is searching
            if (uiState.searchQuery.isNotBlank()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نتائج البحث // الاستعلام: \"${uiState.searchQuery}\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TerminalTextMuted
                        )
                        if (!uiState.isSearching) {
                            StatusBadge(
                                text = "${uiState.searchResults.size} نتائج",
                                style = BadgeStyle.PRIMARY
                            )
                        }
                    }
                }

                if (uiState.isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = TerminalBlue,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else if (uiState.searchError != null) {
                    item {
                        TerminalErrorCard(
                            message = uiState.searchError ?: "خطأ أثناء البحث",
                            onRetry = { viewModel.onSearchQueryChanged(uiState.searchQuery) }
                        )
                    }
                } else if (uiState.searchResults.isEmpty()) {
                    item {
                        TerminalEmptyCard(message = "لم يتم العثور على أية نتائج مطابقة للبحث")
                    }
                } else {
                    items(uiState.searchResults, key = { it.id.ifEmpty { it.url } }) { series ->
                        SeriesCard(
                            series = series,
                            onClick = { onNavigateToSeries(it.id) }
                        )
                    }
                }
            } else {
                // Latest Episodes Feed (Default View)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آخر 5 حلقات مضافة",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TerminalTextPrimary
                        )
                    }
                }

                if (uiState.isLatestLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = TerminalEmerald,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else if (uiState.latestError != null) {
                    item {
                        TerminalErrorCard(
                            message = uiState.latestError ?: "تعذر جلب الحلقات الأخيرة",
                            onRetry = viewModel::loadLatestEpisodes,
                            onConfigureServer = { showServerDialog = true }
                        )
                    }
                } else if (uiState.latestEpisodes.isEmpty()) {
                    item {
                        TerminalEmptyCard(message = "لا توجد حلقات متاحة في قائمة الانتظار حالياً")
                    }
                } else {
                    items(uiState.latestEpisodes, key = { it.id.ifEmpty { it.url } }) { episode ->
                        LatestEpisodeCard(
                            episode = episode,
                            onWatchClick = { ep ->
                                val targetUrl = ep.streamLink ?: ep.url
                                onNavigateToPlayer(targetUrl, ep.title)
                            }
                        )
                    }
                }
            }
        }

        if (showServerDialog) {
            ServerSettingsDialog(
                onDismiss = { showServerDialog = false },
                onSaved = { viewModel.loadLatestEpisodes() }
            )
        }
    }
}

@Composable
private fun TerminalErrorCard(
    message: String,
    onRetry: () -> Unit,
    onConfigureServer: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalRedDim)
            .border(1.dp, TerminalRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "[!] خطأ في جلب البيانات: $message",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalRed
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TerminalSurface)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                        .clickable { onRetry() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "إعادة المحاولة",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TerminalTextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TerminalBlueDim)
                        .border(1.dp, TerminalBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .clickable { onConfigureServer() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "إعدادات الخادم",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TerminalBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerSettingsDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf(ApiClient.getBaseUrl()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalSurface,
        title = {
            Text(
                text = "[إعدادات خادم الـ API]",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TerminalBlue
            )
        },
        text = {
            Column {
                Text(
                    text = "حدد عنوان خادم Express للربط:",
                    fontSize = 12.sp,
                    color = TerminalTextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))

                BasicTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    textStyle = TextStyle(
                        color = TerminalTextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalBg)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "نماذج جاهزة سريعة:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Production Cloud preset
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalEmeraldDim)
                            .border(1.dp, TerminalEmerald.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable { urlInput = "https://esheeq.ysnirix.xyz/api/" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("الرسمي (Cloud)", fontSize = 11.sp, color = TerminalEmerald, fontWeight = FontWeight.Bold)
                    }

                    // Emulator preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalSurfaceElevated)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                            .clickable { urlInput = "http://10.0.2.2:4000/api/" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("محاكي", fontSize = 11.sp, color = TerminalBlue)
                    }

                    // Local LAN preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalSurfaceElevated)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(4.dp))
                            .clickable { urlInput = "http://10.0.0.30:4000/api/" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("محلي (LAN)", fontSize = 11.sp, color = TerminalTextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(TerminalEmeraldDim)
                    .border(1.dp, TerminalEmerald.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .clickable {
                        ApiClient.setCustomBaseUrl(context, urlInput, saveToPrefs = true)
                        onDismiss()
                        onSaved()
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "حفظ وإعادة الاتصال",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TerminalEmerald
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("إلغاء", fontSize = 12.sp, color = TerminalTextMuted)
            }
        }
    )
}

@Composable
private fun TerminalEmptyCard(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "[*] $message",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TerminalTextMuted
        )
    }
}
