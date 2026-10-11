package com.esheeq.app.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBlueDim
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import com.esheeq.app.ui.theme.TerminalEmeraldDim
import com.esheeq.app.ui.theme.TerminalSurface
import com.esheeq.app.ui.theme.TerminalTextMuted
import com.esheeq.app.ui.theme.TerminalTextPrimary

enum class PlayerEngine {
    EXO_PLAYER,
    WEB_EMBED
}

/**
 * Transforms standard streaming page links into clean, ad-resilient iframe embeds.
 */
fun formatStreamEmbedUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    
    // Dailymotion: convert https://www.dailymotion.com/video/ID into official embed
    val dmMatch = Regex("""dailymotion\.com/(?:video|embed/video)/([a-zA-Z0-9]+)""").find(trimmed)
    if (dmMatch != null) {
        val id = dmMatch.groupValues[1]
        return "https://www.dailymotion.com/embed/video/$id?autoplay=1&ui-logo=0&ui-start-screen-info=0"
    }

    // OK.ru: convert video URL to embed
    val okMatch = Regex("""ok\.ru/video(?:embed)?/([0-9]+)""").find(trimmed)
    if (okMatch != null) {
        val id = okMatch.groupValues[1]
        return "https://ok.ru/videoembed/$id"
    }

    return trimmed
}

@OptIn(UnstableApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    videoUrl: String,
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isDirectMedia = videoUrl.contains(".m3u8", ignoreCase = true) ||
            videoUrl.contains(".mp4", ignoreCase = true)

    var currentEngine by remember {
        mutableStateOf(if (isDirectMedia) PlayerEngine.EXO_PLAYER else PlayerEngine.WEB_EMBED)
    }

    val effectiveEmbedUrl = remember(videoUrl) {
        formatStreamEmbedUrl(videoUrl)
    }

    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var isWebLoading by remember { mutableStateOf(true) }

    // ExoPlayer Instance: Only initialize when currentEngine is EXO_PLAYER
    val exoPlayer = remember(context, videoUrl, currentEngine) {
        if (currentEngine == PlayerEngine.EXO_PLAYER) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        } else {
            null
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    BackHandler {
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
        } else {
            onBackClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (customView != null) {
            AndroidView(
                factory = {
                    FrameLayout(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(customView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (currentEngine) {
                PlayerEngine.EXO_PLAYER -> {
                    if (exoPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    useController = true
                                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                    setBackgroundColor(AndroidColor.BLACK)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                PlayerEngine.WEB_EMBED -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    setBackgroundColor(AndroidColor.BLACK)

                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        mediaPlaybackRequiresUserGesture = false
                                        allowFileAccess = false
                                        allowContentAccess = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        cacheMode = WebSettings.LOAD_DEFAULT
                                        userAgentString =
                                            "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                    }

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isWebLoading = false
                                        }

                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val reqUrl = request?.url?.toString() ?: return false
                                            val scheme = request.url?.scheme?.lowercase() ?: ""
                                            if (scheme != "http" && scheme != "https") {
                                                // Block intent://, market://, app:// redirects from interrupting playback
                                                return true
                                            }
                                            val host = request.url?.host?.lowercase() ?: ""
                                            // Allow video stream and embed CDN hosts
                                            if (host.contains("dailymotion.com") ||
                                                host.contains("dmcdn.net") ||
                                                host.contains("mail.ru") ||
                                                host.contains("ok.ru") ||
                                                host.contains("vidshar") ||
                                                host.contains("qeseh") ||
                                                host.contains("qesen")
                                            ) {
                                                return false
                                            }
                                            // Intercept ad popups and prevent navigating away
                                            return true
                                        }
                                    }

                                    webChromeClient = object : WebChromeClient() {
                                        override fun onShowCustomView(
                                            view: View?,
                                            callback: CustomViewCallback?
                                        ) {
                                            customView = view
                                            customViewCallback = callback
                                        }

                                        override fun onHideCustomView() {
                                            customView = null
                                            customViewCallback = null
                                        }
                                    }

                                    loadUrl(effectiveEmbedUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isWebLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = TerminalEmerald,
                                    modifier = Modifier.size(40.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Overlay Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(TerminalBg.copy(alpha = 0.90f))
                .statusBarsPadding()
                .border(1.dp, TerminalBorder.copy(alpha = 0.6f), RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
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

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TerminalTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Engine Switcher
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalBlueDim)
                            .border(1.dp, TerminalBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable {
                                currentEngine = if (currentEngine == PlayerEngine.EXO_PLAYER) {
                                    PlayerEngine.WEB_EMBED
                                } else {
                                    PlayerEngine.EXO_PLAYER
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (currentEngine == PlayerEngine.EXO_PLAYER) "محرك: أصلي" else "محرك: مضمن",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalBlue
                        )
                    }

                    // Picture in Picture (PiP) Button
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                        IconButton(
                            onClick = {
                                try {
                                    val pipParams = PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .build()
                                    activity.enterPictureInPictureMode(pipParams)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "صورة داخل صورة",
                                tint = TerminalEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}
