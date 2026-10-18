package com.esheeq.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import android.widget.Toast
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esheeq.app.data.model.LatestEpisode
import com.esheeq.app.data.model.SearchResult
import com.esheeq.app.data.repository.AppUpdateManager
import com.esheeq.app.data.repository.FavoritesManager
import com.esheeq.app.ui.components.BadgeStyle
import com.esheeq.app.ui.components.FavoriteSeriesCard
import com.esheeq.app.ui.components.LatestEpisodeCard
import com.esheeq.app.ui.components.SearchBarComponent
import com.esheeq.app.ui.components.SeriesCard
import com.esheeq.app.ui.components.StatusBadge
import com.esheeq.app.ui.components.TerminalTopBar
import com.esheeq.app.ui.components.UpdateDialog
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalRed
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.esheeq.app.ui.theme.TerminalRedDim
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary
import kotlinx.coroutines.launch

enum class HomeFeedTab {
    LATEST,
    FAVORITES
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToSeries: (String) -> Unit,
    onNavigateToPlayer: (url: String, title: String, seriesId: String, episode: String) -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val favoritesList by FavoritesManager.favoritesState.collectAsState()
    var selectedFeedTab by remember { mutableStateOf(HomeFeedTab.LATEST) }
    val latestUpdateInfo by AppUpdateManager.latestUpdateInfo.collectAsState()
    val downloadState by AppUpdateManager.downloadState.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingUpdates by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = AppUpdateManager.checkForUpdates(forceCheck = true)
        result.getOrNull()?.let { info ->
            if (info.isUpdateAvailable) {
                showUpdateDialog = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        TerminalTopBar(
            title = "قصة عشق",
            actions = {
                Box {
                    IconButton(
                        onClick = {
                            if (latestUpdateInfo?.isUpdateAvailable == true) {
                                showUpdateDialog = true
                            } else {
                                coroutineScope.launch {
                                    isCheckingUpdates = true
                                    val res = AppUpdateManager.checkForUpdates(forceCheck = true)
                                    isCheckingUpdates = false
                                    val info = res.getOrNull()
                                    if (info != null && info.isUpdateAvailable) {
                                        showUpdateDialog = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "أنت تستخدم أحدث إصدار (v${AppUpdateManager.getCurrentVersionName()})",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (isCheckingUpdates) {
                            CircularProgressIndicator(
                                color = TerminalBlue,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "فحص التحديثات",
                                tint = if (latestUpdateInfo?.isUpdateAvailable == true) TerminalEmerald else TerminalTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (latestUpdateInfo?.isUpdateAvailable == true) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TerminalEmerald)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
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
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onSearch = {
                        viewModel.recordSearch(context)
                    }
                )
            }

            // Recent Search History Section (Visible when query is empty and history exists)
            if (uiState.searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalSurface)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = TerminalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "سجل البحث // RECENT",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerminalTextMuted
                                )
                            }

                            Text(
                                text = "مسح الكل",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TerminalTextMuted,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { viewModel.clearHistory(context) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            searchHistory.forEach { historyQuery ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF161B22))
                                        .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.onSearchQueryChanged(historyQuery)
                                            viewModel.recordSearch(context, historyQuery)
                                        }
                                        .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "> $historyQuery",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = TerminalTextPrimary
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeHistoryItem(context, historyQuery) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "حذف",
                                            tint = TerminalTextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
                            onClick = {
                                viewModel.recordSearch(context)
                                onNavigateToSeries(it.id)
                            }
                        )
                    }
                }
            } else {
                // Segmented Switcher for Home Feed Tabs
                item {
                    HomeFeedSwitcher(
                        selectedTab = selectedFeedTab,
                        onTabSelected = { selectedFeedTab = it },
                        favoritesCount = favoritesList.size
                    )
                }

                when (selectedFeedTab) {
                    HomeFeedTab.LATEST -> {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 4.dp, height = 18.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(TerminalEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "آخر الحلقات المضافة",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerminalTextPrimary
                                    )
                                }
                                StatusBadge(
                                    text = "${uiState.latestEpisodes.size.coerceAtLeast(5)} حلقات جديدة",
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
                                        onNavigateToPlayer(targetUrl, ep.title, ep.id, ep.episode)
                                    }
                                )
                            }
                        }
                    }

                    HomeFeedTab.FAVORITES -> {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 4.dp, height = 18.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(TerminalEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "المسلسلات المفضلة",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerminalTextPrimary
                                    )
                                }
                                if (favoritesList.isNotEmpty()) {
                                    Text(
                                        text = "${favoritesList.size} عمل محفوظ",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = TerminalTextMuted
                                    )
                                }
                            }
                        }

                        if (favoritesList.isEmpty()) {
                            item {
                                TerminalEmptyCard(message = "لا توجد مسلسلات في قائمتك المفضلة حتى الآن\nاضغط على أيقونة الإشارة المرجعية في صفحة أي مسلسل لحفظه هنا")
                            }
                        } else {
                            items(favoritesList, key = { it.id }) { fav ->
                                FavoriteSeriesCard(
                                    series = fav,
                                    onClick = {
                                        val targetSlug = if (fav.id.any { it.code > 127 } && fav.url.isNotBlank()) {
                                            fav.url.trimEnd('/').substringAfterLast('/').ifBlank { fav.id }
                                        } else {
                                            fav.id
                                        }
                                        onNavigateToSeries(targetSlug)
                                    },
                                    onRemove = {
                                        FavoritesManager.removeFavorite(context, fav.id)
                                        Toast.makeText(context, "تمت إزالة \"${fav.title}\" من المفضلة", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUpdateDialog && latestUpdateInfo != null) {
        UpdateDialog(
            updateInfo = latestUpdateInfo!!,
            downloadState = downloadState,
            onStartDownload = {
                coroutineScope.launch {
                    AppUpdateManager.downloadApk(latestUpdateInfo!!)
                }
            },
            onInstallApk = { apkFile ->
                AppUpdateManager.installApk(context, apkFile)
            },
            onOpenBrowser = { url ->
                AppUpdateManager.openBrowser(context, url)
            },
            onDismiss = {
                showUpdateDialog = false
                AppUpdateManager.resetDownloadState()
            }
        )
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
            Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun HomeFeedSwitcher(
    selectedTab: HomeFeedTab,
    onTabSelected: (HomeFeedTab) -> Unit,
    favoritesCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Latest Episodes Tab
        val isLatestSelected = selectedTab == HomeFeedTab.LATEST
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isLatestSelected) TerminalBg else Color.Transparent)
                .border(
                    1.dp,
                    if (isLatestSelected) TerminalEmerald.copy(alpha = 0.5f) else Color.Transparent,
                    RoundedCornerShape(6.dp)
                )
                .clickable { onTabSelected(HomeFeedTab.LATEST) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (isLatestSelected) TerminalEmerald else TerminalTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "آخر الحلقات",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = if (isLatestSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isLatestSelected) TerminalTextPrimary else TerminalTextMuted
                )
            }
        }

        // Favorites Tab
        val isFavoritesSelected = selectedTab == HomeFeedTab.FAVORITES
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isFavoritesSelected) TerminalBg else Color.Transparent)
                .border(
                    1.dp,
                    if (isFavoritesSelected) TerminalEmerald.copy(alpha = 0.5f) else Color.Transparent,
                    RoundedCornerShape(6.dp)
                )
                .clickable { onTabSelected(HomeFeedTab.FAVORITES) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = if (isFavoritesSelected) TerminalEmerald else TerminalTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (favoritesCount > 0) "المفضلة ($favoritesCount)" else "المفضلة",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = if (isFavoritesSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isFavoritesSelected) TerminalTextPrimary else TerminalTextMuted
                )
            }
        }
    }
}

