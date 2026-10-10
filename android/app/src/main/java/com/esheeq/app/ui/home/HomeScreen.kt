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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalRed
import com.esheeq.app.ui.theme.TerminalRedDim
import com.esheeq.app.ui.theme.TerminalSurface
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        TerminalTopBar(
            title = "قصة عشق",
            subTitle = "النظام الطرفي // إصدار أندرويد الأصلي 1.0"
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "قائمة الانتظار // آخر 5 حلقات مضافة",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TerminalTextPrimary
                        )

                        StatusBadge(
                            text = "5 حلقات",
                            style = BadgeStyle.SUCCESS,
                            showDot = true
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
                            onRetry = viewModel::loadLatestEpisodes
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
    }
}

@Composable
private fun TerminalErrorCard(
    message: String,
    onRetry: () -> Unit
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
            Spacer(modifier = Modifier.height(10.dp))
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
        }
    }
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
