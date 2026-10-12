package com.esheeq.app.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.esheeq.app.MainActivity
import com.esheeq.app.ui.theme.TerminalEmerald
import kotlinx.coroutines.delay

enum class PlayerEngine {
    EXO_PLAYER,
    WEB_EMBED
}

/**
 * Extracts video ID from any Dailymotion URL format.
 */
fun extractDailymotionId(rawUrl: String): String? {
    val trimmed = rawUrl.trim()
    val patterns = listOf(
        Regex("""dailymotion\.com/(?:video|embed/video)/([a-zA-Z0-9]+)"""),
        Regex("""dai\.ly/([a-zA-Z0-9]+)"""),
        Regex("""dailymotion\.com/hub/[^#]+#video=([a-zA-Z0-9]+)""")
    )
    for (pattern in patterns) {
        val match = pattern.find(trimmed)
        if (match != null) return match.groupValues[1]
    }
    return null
}

/**
 * Builds responsive HTML embedding Dailymotion iframe with proper referrer origin.
 */
fun buildDailymotionHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html, body { width: 100%; height: 100%; background-color: #000; overflow: hidden; }
            iframe { width: 100%; height: 100%; border: 0; display: block; }
          </style>
        </head>
        <body>
          <iframe
            src="https://www.dailymotion.com/embed/video/$videoId?autoplay=1&ui-logo=0&ui-start-screen-info=0"
            allow="autoplay; fullscreen; picture-in-picture; web-share"
            allowfullscreen>
          </iframe>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Transforms standard streaming page links into clean, ad-resilient iframe embeds.
 */
fun formatStreamEmbedUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()

    val dmId = extractDailymotionId(trimmed)
    if (dmId != null) {
        return "https://www.dailymotion.com/embed/video/$dmId?autoplay=1&ui-logo=0&ui-start-screen-info=0"
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

    val currentEngine = remember(videoUrl) {
        if (isDirectMedia) PlayerEngine.EXO_PLAYER else PlayerEngine.WEB_EMBED
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

    // WebView Instance: Only initialize when currentEngine is WEB_EMBED
    val webView = remember(context, videoUrl, currentEngine) {
        if (currentEngine == PlayerEngine.WEB_EMBED) {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.BLACK)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                val currentWebView = this
                CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(currentWebView, true)
                }

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
                        val reqUrl = request?.url ?: return false
                        val scheme = reqUrl.scheme?.lowercase() ?: ""
                        if (scheme != "http" && scheme != "https") {
                            return true
                        }
                        if (request.isForMainFrame) {
                            val host = reqUrl.host?.lowercase() ?: ""
                            if (host.contains("dailymotion.com") ||
                                host.contains("dmcdn.net") ||
                                host.contains("dmxleo.com") ||
                                host.contains("mail.ru") ||
                                host.contains("ok.ru") ||
                                host.contains("vidshar") ||
                                host.contains("qeseh") ||
                                host.contains("qesen")
                            ) {
                                return false
                            }
                            return true
                        }
                        return false
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
                        try {
                            (customView?.parent as? ViewGroup)?.removeView(customView)
                            customViewCallback?.onCustomViewHidden()
                        } catch (_: Exception) {}
                        customView = null
                        customViewCallback = null
                    }
                }

                val dmId = extractDailymotionId(videoUrl)
                if (dmId != null) {
                    val html = buildDailymotionHtml(dmId)
                    loadDataWithBaseURL("https://www.dailymotion.com/", html, "text/html", "UTF-8", null)
                } else {
                    loadUrl(effectiveEmbedUrl)
                }
            }
        } else {
            null
        }
    }

    var isCleanedUp by remember { mutableStateOf(false) }

    // Comprehensive cleanup for both ExoPlayer and WebView engines
    val cleanupPlayback: () -> Unit = remember(exoPlayer, webView) {
        {
            if (!isCleanedUp) {
                isCleanedUp = true

                try {
                    exoPlayer?.stop()
                    exoPlayer?.clearMediaItems()
                    exoPlayer?.release()
                } catch (_: Exception) {}

                try {
                    if (customView != null) {
                        (customView?.parent as? ViewGroup)?.removeView(customView)
                        customViewCallback?.onCustomViewHidden()
                        customView = null
                        customViewCallback = null
                    }
                } catch (_: Exception) {}

                try {
                    webView?.apply {
                        evaluateJavascript(
                            """
                            (function() {
                                var videos = document.getElementsByTagName('video');
                                for (var i = 0; i < videos.length; i++) {
                                    videos[i].pause();
                                    videos[i].src = '';
                                    videos[i].load();
                                }
                                var audios = document.getElementsByTagName('audio');
                                for (var i = 0; i < audios.length; i++) {
                                    audios[i].pause();
                                    audios[i].src = '';
                                    audios[i].load();
                                }
                                var iframes = document.getElementsByTagName('iframe');
                                for (var i = 0; i < iframes.length; i++) {
                                    iframes[i].src = 'about:blank';
                                }
                            })();
                            """.trimIndent(),
                            null
                        )
                        stopLoading()
                        pauseTimers()
                        onPause()
                        loadUrl("about:blank")
                        clearHistory()
                        (parent as? ViewGroup)?.removeView(this)
                        removeAllViews()
                        destroy()
                    }
                } catch (_: Exception) {}

                MainActivity.flushAudioOutput(context)
            }
        }
    }

    val safelyNavigateBack: () -> Unit = remember(cleanupPlayback) {
        {
            cleanupPlayback()
            onBackClick()
        }
    }

    // Terminate playback when composable leaves composition (navigation away)
    DisposableEffect(Unit) {
        onDispose {
            cleanupPlayback()
        }
    }

    // Pause audio/video on app minimize/lock unless in Picture-in-Picture
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, cleanupPlayback) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    val inPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        activity?.isInPictureInPictureMode == true
                    } else false
                    if (!inPip) {
                        exoPlayer?.pause()
                        webView?.evaluateJavascript(
                            "document.querySelectorAll('video, audio').forEach(function(el){ el.pause(); });",
                            null
                        )
                        webView?.onPause()
                        webView?.pauseTimers()
                        MainActivity.flushAudioOutput(context)
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    val inPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        activity?.isInPictureInPictureMode == true
                    } else false
                    if (!inPip) {
                        webView?.onResume()
                        webView?.resumeTimers()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    cleanupPlayback()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cleanupPlayback()
        }
    }

    BackHandler {
        if (customView != null) {
            try {
                (customView?.parent as? ViewGroup)?.removeView(customView)
                customViewCallback?.onCustomViewHidden()
            } catch (_: Exception) {}
            customView = null
            customViewCallback = null
        } else {
            safelyNavigateBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
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
                        onRelease = { cleanupPlayback() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            PlayerEngine.WEB_EMBED -> {
                if (webView != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = {
                                (webView.parent as? ViewGroup)?.removeView(webView)
                                webView
                            },
                            onRelease = { cleanupPlayback() },
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

        if (customView != null) {
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(AndroidColor.BLACK)
                        (customView?.parent as? ViewGroup)?.removeView(customView)
                        addView(customView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Minimal floating controls: auto-hides after 3.5s so top bar is completely disabled during playback
        var showFloatingControls by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            delay(3500)
            showFloatingControls = false
        }

        if (showFloatingControls) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = safelyNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x80000000))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

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
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "صورة داخل صورة",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

