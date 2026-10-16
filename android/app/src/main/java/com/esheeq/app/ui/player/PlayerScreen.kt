package com.esheeq.app.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.graphics.Color as AndroidColor
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.esheeq.app.MainActivity
import com.esheeq.app.data.model.DailyLink
import com.esheeq.app.data.model.SeriesDetails
import com.esheeq.app.data.repository.AdBlockManager
import com.esheeq.app.data.repository.DailymotionStreamResolver
import com.esheeq.app.data.repository.PlaybackHistoryManager
import com.esheeq.app.data.repository.SeriesRepository
import com.esheeq.app.ui.theme.TerminalBg
import com.esheeq.app.ui.theme.TerminalBlue
import com.esheeq.app.ui.theme.TerminalBorder
import com.esheeq.app.ui.theme.TerminalEmerald
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
 * Builds responsive HTML embedding Dailymotion iframe with postMessage listener.
 */
fun buildDailymotionHtml(videoId: String, startSeconds: Long = 0L): String {
    val startParam = if (startSeconds > 0) "&start=$startSeconds" else ""
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
            id="dm_player"
            src="https://www.dailymotion.com/embed/video/$videoId?autoplay=1&ui-logo=0&ui-start-screen-info=0$startParam"
            allow="autoplay; fullscreen; picture-in-picture; web-share"
            allowfullscreen>
          </iframe>
          <script>
            window.addEventListener('message', function(e) {
                try {
                    var data = typeof e.data === 'string' ? JSON.parse(e.data) : e.data;
                    if (data && data.event === 'timeupdate') {
                        var cur = Math.floor((data.time || 0) * 1000);
                        var dur = Math.floor((data.duration || 0) * 1000);
                        if (window.AndroidBridge && cur > 0) {
                            window.AndroidBridge.onTimeUpdate(cur, dur);
                        }
                    }
                } catch(err) {}
            });
            setInterval(function() {
                var v = document.querySelector('video');
                if (v && !v.paused && v.duration > 0 && window.AndroidBridge) {
                    window.AndroidBridge.onTimeUpdate(Math.floor(v.currentTime * 1000), Math.floor(v.duration * 1000));
                }
            }, 1500);
          </script>
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
    seriesId: String? = null,
    currentEpisode: String? = null,
    onBackClick: () -> Unit,
    onNavigateToEpisode: ((nextUrl: String, nextTitle: String, nextEpisode: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    // Immersive mode: hide status bar, navigation bar, and bottom area for true edge-to-edge playback
    DisposableEffect(activity) {
        val window = activity?.window
        val insetsController = if (window != null) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else null

        val originalCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && window != null) {
            val prev = window.attributes.layoutInDisplayCutoutMode
            val params = window.attributes
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
            prev
        } else 0

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        @Suppress("DEPRECATION")
        window?.statusBarColor = AndroidColor.TRANSPARENT
        @Suppress("DEPRECATION")
        window?.navigationBarColor = AndroidColor.TRANSPARENT

        insetsController?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && window != null) {
                val params = window.attributes
                params.layoutInDisplayCutoutMode = originalCutoutMode
                window.attributes = params
            }

            @Suppress("DEPRECATION")
            window?.statusBarColor = TerminalBg.toArgb()
            @Suppress("DEPRECATION")
            window?.navigationBarColor = TerminalBg.toArgb()

            insetsController?.apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Direct stream resolution for Dailymotion and supported mirrors
    var resolvedDirectStreamUrl by remember(videoUrl) { mutableStateOf<String?>(null) }
    LaunchedEffect(videoUrl) {
        val dmId = extractDailymotionId(videoUrl)
        if (dmId != null) {
            val directStream = DailymotionStreamResolver.resolveStreamUrl(dmId)
            if (!directStream.isNullOrBlank()) {
                resolvedDirectStreamUrl = directStream
            }
        }
    }

    val activeVideoUrl = resolvedDirectStreamUrl ?: videoUrl
    val isDirectMedia = activeVideoUrl.contains(".m3u8", ignoreCase = true) ||
            activeVideoUrl.contains(".mp4", ignoreCase = true)

    val currentEngine = remember(activeVideoUrl, isDirectMedia) {
        if (isDirectMedia) PlayerEngine.EXO_PLAYER else PlayerEngine.WEB_EMBED
    }

    val effectiveEmbedUrl = remember(videoUrl) {
        formatStreamEmbedUrl(videoUrl)
    }

    // AdBlock Shield State
    var isShieldEnabled by remember { mutableStateOf(AdBlockManager.isAdBlockEnabled(context)) }
    var shieldToastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(shieldToastMessage) {
        if (shieldToastMessage != null) {
            delay(2500)
            shieldToastMessage = null
        }
    }

    // Resume Watching State
    val initialProgress = remember(videoUrl) {
        PlaybackHistoryManager.getProgress(videoUrl)
    }
    var resumeToastTime by remember { mutableStateOf<String?>(null) }
    var showResumeToast by remember { mutableStateOf(false) }

    LaunchedEffect(videoUrl) {
        if (initialProgress != null && initialProgress.isResumeEligible) {
            resumeToastTime = PlaybackHistoryManager.formatTime(initialProgress.positionMs)
            showResumeToast = true
        }
    }

    LaunchedEffect(showResumeToast) {
        if (showResumeToast) {
            delay(4500)
            showResumeToast = false
        }
    }

    // Lock Mode State
    var isLocked by remember { mutableStateOf(false) }
    var showUnlockPill by remember { mutableStateOf(false) }

    LaunchedEffect(showUnlockPill) {
        if (showUnlockPill) {
            delay(3500)
            showUnlockPill = false
        }
    }

    // Floating Controls Visibility (Auto-hides after 5s)
    var showFloatingControls by remember { mutableStateOf(true) }
    LaunchedEffect(showFloatingControls) {
        if (showFloatingControls) {
            delay(5000)
            showFloatingControls = false
        }
    }

    // Gesture Controls State
    var gestureIndicatorType by remember { mutableStateOf<String?>(null) } // "brightness", "volume"
    var gestureIndicatorValue by remember { mutableIntStateOf(0) }
    var doubleTapSeekText by remember { mutableStateOf<String?>(null) }
    var doubleTapSeekSide by remember { mutableStateOf<String?>(null) } // "left", "right"

    LaunchedEffect(doubleTapSeekText) {
        if (doubleTapSeekText != null) {
            delay(700)
            doubleTapSeekText = null
            doubleTapSeekSide = null
        }
    }

    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var isWebLoading by remember { mutableStateOf(true) }

    // Next / Previous Episodes resolution
    var seriesDetails by remember { mutableStateOf<SeriesDetails?>(null) }
    var previousEpisode by remember { mutableStateOf<DailyLink?>(null) }
    var nextEpisode by remember { mutableStateOf<DailyLink?>(null) }

    LaunchedEffect(seriesId) {
        if (!seriesId.isNullOrBlank()) {
            try {
                val result = SeriesRepository().getSeriesDetails(seriesId)
                if (result.isSuccess) {
                    val details = result.getOrNull()
                    seriesDetails = details
                    if (details != null && details.dailyLinks.isNotEmpty()) {
                        val currentIndex = details.dailyLinks.indexOfFirst {
                            it.link == videoUrl || it.episode == currentEpisode
                        }
                        if (currentIndex != -1) {
                            if (currentIndex > 0) {
                                previousEpisode = details.dailyLinks[currentIndex - 1]
                            }
                            if (currentIndex < details.dailyLinks.size - 1) {
                                nextEpisode = details.dailyLinks[currentIndex + 1]
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // ExoPlayer Instance
    val exoPlayer = remember(context, activeVideoUrl, currentEngine) {
        if (currentEngine == PlayerEngine.EXO_PLAYER) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(activeVideoUrl)
                setMediaItem(mediaItem)
                prepare()
                if (initialProgress != null && initialProgress.isResumeEligible) {
                    seekTo(initialProgress.positionMs)
                }
                playWhenReady = true
            }
        } else {
            null
        }
    }

    // Periodic progress saving for ExoPlayer
    LaunchedEffect(exoPlayer, videoUrl) {
        while (isActive) {
            delay(1500)
            if (exoPlayer != null && exoPlayer.isPlaying) {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                if (dur > 0 && pos > 0) {
                    PlaybackHistoryManager.saveProgress(
                        url = videoUrl,
                        positionMs = pos,
                        durationMs = dur,
                        seriesId = seriesId,
                        episodeNumber = currentEpisode,
                        title = title
                    )
                }
            }
        }
    }

    // WebView Instance
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
                    setSupportMultipleWindows(false)
                    javaScriptCanOpenWindowsAutomatically = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString =
                        "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                class WebAppBridge {
                    @JavascriptInterface
                    fun onTimeUpdate(positionMs: Long, durationMs: Long) {
                        if (positionMs > 0) {
                            PlaybackHistoryManager.saveProgress(
                                url = videoUrl,
                                positionMs = positionMs,
                                durationMs = durationMs,
                                seriesId = seriesId,
                                episodeNumber = currentEpisode,
                                title = title
                            )
                        }
                    }
                }
                addJavascriptInterface(WebAppBridge(), "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        if (isShieldEnabled && request != null && AdBlockManager.isAd(request.url)) {
                            return AdBlockManager.getEmptyResponse()
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isWebLoading = false
                        if (isShieldEnabled) {
                            view?.evaluateJavascript(AdBlockManager.SCRIPT_DEFUSER, null)
                            view?.evaluateJavascript(AdBlockManager.COSMETIC_HIDE_CSS, null)
                        }
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
                        if (isShieldEnabled && AdBlockManager.isAd(reqUrl)) {
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
                    override fun onCreateWindow(
                        view: WebView?,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: Message?
                    ): Boolean {
                        if (isShieldEnabled) {
                            return false
                        }
                        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    }

                    override fun onJsAlert(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?
                    ): Boolean {
                        if (isShieldEnabled) {
                            result?.cancel()
                            return true
                        }
                        return super.onJsAlert(view, url, message, result)
                    }

                    override fun onJsConfirm(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?
                    ): Boolean {
                        if (isShieldEnabled) {
                            result?.cancel()
                            return true
                        }
                        return super.onJsConfirm(view, url, message, result)
                    }

                    override fun onShowCustomView(
                        view: View?,
                        callback: CustomViewCallback?
                    ) {
                        customView = view
                        customViewCallback = callback
                        activity?.window?.let { win ->
                            val insets = WindowCompat.getInsetsController(win, win.decorView)
                            insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            insets.hide(WindowInsetsCompat.Type.systemBars())
                        }
                    }

                    override fun onHideCustomView() {
                        try {
                            (customView?.parent as? ViewGroup)?.removeView(customView)
                            customViewCallback?.onCustomViewHidden()
                        } catch (_: Exception) {}
                        customView = null
                        customViewCallback = null
                        activity?.window?.let { win ->
                            val insets = WindowCompat.getInsetsController(win, win.decorView)
                            insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            insets.hide(WindowInsetsCompat.Type.systemBars())
                        }
                    }
                }

                val dmId = extractDailymotionId(videoUrl)
                val resumeSec = if (initialProgress != null && initialProgress.isResumeEligible) {
                    initialProgress.positionMs / 1000
                } else 0L

                if (dmId != null) {
                    val html = buildDailymotionHtml(dmId, resumeSec)
                    loadDataWithBaseURL("https://www.dailymotion.com/", html, "text/html", "UTF-8", null)
                } else {
                    loadUrl(effectiveEmbedUrl)
                }
            }
        } else {
            null
        }
    }

    // Seek helper for double-tap seek
    fun seekRelative(deltaMs: Long) {
        if (currentEngine == PlayerEngine.EXO_PLAYER && exoPlayer != null) {
            val cur = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            val target = (cur + deltaMs).coerceIn(0L, if (dur > 0) dur else Long.MAX_VALUE)
            exoPlayer.seekTo(target)
        } else if (currentEngine == PlayerEngine.WEB_EMBED && webView != null) {
            val deltaSec = deltaMs / 1000
            val js = """
                (function() {
                    var v = document.querySelector('video');
                    if (v) {
                        v.currentTime = Math.max(0, v.currentTime + ($deltaSec));
                    }
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        }
    }

    var isCleanedUp by remember { mutableStateOf(false) }

    val cleanupPlayback: () -> Unit = remember(exoPlayer, webView) {
        {
            if (!isCleanedUp) {
                isCleanedUp = true

                try {
                    if (exoPlayer != null) {
                        val pos = exoPlayer.currentPosition
                        val dur = exoPlayer.duration
                        if (dur > 0 && pos > 0) {
                            PlaybackHistoryManager.saveProgress(
                                url = videoUrl,
                                positionMs = pos,
                                durationMs = dur,
                                seriesId = seriesId,
                                episodeNumber = currentEpisode,
                                title = title
                            )
                        }
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                        exoPlayer.release()
                    }
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
            activity?.window?.let { win ->
                val insets = WindowCompat.getInsetsController(win, win.decorView)
                insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                insets.show(WindowInsetsCompat.Type.systemBars())
            }
            onBackClick()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cleanupPlayback()
        }
    }

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
                        activity?.window?.let { win ->
                            val insets = WindowCompat.getInsetsController(win, win.decorView)
                            insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            insets.hide(WindowInsetsCompat.Type.systemBars())
                        }
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
        if (isLocked) {
            showUnlockPill = true
        } else if (customView != null) {
            try {
                (customView?.parent as? ViewGroup)?.removeView(customView)
                customViewCallback?.onCustomViewHidden()
            } catch (_: Exception) {}
            customView = null
            customViewCallback = null
            activity?.window?.let { win ->
                val insets = WindowCompat.getInsetsController(win, win.decorView)
                insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insets.hide(WindowInsetsCompat.Type.systemBars())
            }
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
                                val exoGestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                                        showFloatingControls = !showFloatingControls
                                        return false
                                    }
                                    override fun onDoubleTap(e: MotionEvent): Boolean {
                                        val w = width.toFloat()
                                        if (e.x < w * 0.35f) {
                                            seekRelative(-10_000L)
                                            doubleTapSeekSide = "left"
                                            doubleTapSeekText = "-10 ث"
                                            return true
                                        } else if (e.x > w * 0.65f) {
                                            seekRelative(10_000L)
                                            doubleTapSeekSide = "right"
                                            doubleTapSeekText = "+10 ث"
                                            return true
                                        }
                                        return false
                                    }
                                })
                                setOnTouchListener { _, event ->
                                    exoGestureDetector.onTouchEvent(event)
                                    false
                                }
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
                                val webGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                                        showFloatingControls = !showFloatingControls
                                        return false
                                    }
                                    override fun onDoubleTap(e: MotionEvent): Boolean {
                                        val w = webView.width.toFloat()
                                        if (e.x < w * 0.35f) {
                                            seekRelative(-10_000L)
                                            doubleTapSeekSide = "left"
                                            doubleTapSeekText = "-10 ث"
                                            return true
                                        } else if (e.x > w * 0.65f) {
                                            seekRelative(10_000L)
                                            doubleTapSeekSide = "right"
                                            doubleTapSeekText = "+10 ث"
                                            return true
                                        }
                                        return false
                                    }
                                })
                                webView.setOnTouchListener { _, event ->
                                    webGestureDetector.onTouchEvent(event)
                                    false
                                }
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

        // Left Edge Gesture Zone: Vertical drag adjusts Brightness (outer 18% margin only)
        if (!isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.18f)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { gestureIndicatorType = "brightness" },
                            onDragEnd = { gestureIndicatorType = null },
                            onDragCancel = { gestureIndicatorType = null },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val h = size.height.toFloat()
                                val delta = -dragAmount / h
                                if (activity != null) {
                                    val current = activity.window.attributes.screenBrightness.let {
                                        if (it < 0f) 0.5f else it
                                    }
                                    val newBrightness = (current + delta).coerceIn(0.01f, 1f)
                                    val params = activity.window.attributes
                                    params.screenBrightness = newBrightness
                                    activity.window.attributes = params
                                    gestureIndicatorValue = (newBrightness * 100).toInt()
                                }
                            }
                        )
                    }
            )
        }

        // Right Edge Gesture Zone: Vertical drag adjusts Volume (outer 18% margin only)
        if (!isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.18f)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { gestureIndicatorType = "volume" },
                            onDragEnd = { gestureIndicatorType = null },
                            onDragCancel = { gestureIndicatorType = null },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val h = size.height.toFloat()
                                val delta = -dragAmount / h
                                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                                if (audioManager != null) {
                                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    val step = (delta * max * 1.5f).toInt()
                                    if (step != 0) {
                                        val newVol = (cur + step).coerceIn(0, max)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                        gestureIndicatorValue = (newVol * 100 / max.coerceAtLeast(1))
                                    }
                                }
                            }
                        )
                    }
            )
        }

        // Fullscreen touch barrier when locked
        if (isLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            showUnlockPill = true
                        }
                    }
            )
        }

        // Double-Tap Seek Visual Badges
        if (doubleTapSeekText != null && doubleTapSeekSide == "left") {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = doubleTapSeekText ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (doubleTapSeekText != null && doubleTapSeekSide == "right") {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 40.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = doubleTapSeekText ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Gesture HUD (Brightness or Volume indicator in center)
        if (gestureIndicatorType != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xD90D1117))
                    .border(1.dp, TerminalBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (gestureIndicatorType == "brightness") Icons.Default.BrightnessMedium else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = TerminalEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$gestureIndicatorValue%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Unlock Pill (Displayed when locked upon any tap)
        if (isLocked && showUnlockPill) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xEB0D1117))
                    .border(1.dp, TerminalEmerald.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .clickable {
                        isLocked = false
                        showUnlockPill = false
                        showFloatingControls = true
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "إلغاء القفل",
                        tint = TerminalEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "القفل مفعّل (انقر لفتح الشاشة)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Shield Toast Pill
        if (shieldToastMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE60D1117))
                    .border(
                        1.dp,
                        if (isShieldEnabled) TerminalEmerald.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isShieldEnabled) TerminalEmerald else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = shieldToastMessage ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Resume Toast Pill
        if (showResumeToast && !resumeToastTime.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE60D1117))
                    .border(1.dp, TerminalEmerald.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تم الاستئناف من $resumeToastTime",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "البدء من البداية",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalEmerald,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                PlaybackHistoryManager.resetToBeginning(videoUrl)
                                if (currentEngine == PlayerEngine.EXO_PLAYER) {
                                    exoPlayer?.seekTo(0L)
                                } else if (currentEngine == PlayerEngine.WEB_EMBED) {
                                    webView?.evaluateJavascript(
                                        "(function(){ var v=document.querySelector('video'); if(v) v.currentTime=0; })();",
                                        null
                                    )
                                }
                                showResumeToast = false
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Floating controls (Auto-hides after 3.5s; hidden when locked)
        if (!isLocked && showFloatingControls) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(
                        start = if (isRtl) 68.dp else 14.dp,
                        end = if (isRtl) 14.dp else 68.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
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

                // Title in Center
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                // Controls action row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Episode Shortcut
                    if (previousEpisode != null && onNavigateToEpisode != null) {
                        IconButton(
                            onClick = {
                                previousEpisode?.let { prev ->
                                    val url = prev.link
                                    if (!url.isNullOrBlank()) {
                                        cleanupPlayback()
                                        onNavigateToEpisode(
                                            url,
                                            "${seriesDetails?.title ?: title} - الحلقة ${prev.episode}",
                                            prev.episode
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x80000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "الحلقة السابقة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Next Episode Shortcut
                    if (nextEpisode != null && onNavigateToEpisode != null) {
                        IconButton(
                            onClick = {
                                nextEpisode?.let { nxt ->
                                    val url = nxt.link
                                    if (!url.isNullOrBlank()) {
                                        cleanupPlayback()
                                        onNavigateToEpisode(
                                            url,
                                            "${seriesDetails?.title ?: title} - الحلقة ${nxt.episode}",
                                            nxt.episode
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x80000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "الحلقة التالية",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // AdBlock Shield Toggle Button
                    IconButton(
                        onClick = {
                            isShieldEnabled = !isShieldEnabled
                            AdBlockManager.setAdBlockEnabled(context, isShieldEnabled)
                            shieldToastMessage = if (isShieldEnabled) "درع الإعلانات: مفعل" else "درع الإعلانات: معطل"
                            if (currentEngine == PlayerEngine.WEB_EMBED) {
                                webView?.reload()
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isShieldEnabled) TerminalEmerald.copy(alpha = 0.25f) else Color(0x80000000))
                            .border(
                                1.dp,
                                if (isShieldEnabled) TerminalEmerald.copy(alpha = 0.6f) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = if (isShieldEnabled) "درع الإعلانات: مفعل" else "درع الإعلانات: معطل",
                            tint = if (isShieldEnabled) TerminalEmerald else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Screen Lock Button
                    IconButton(
                        onClick = {
                            isLocked = true
                            showFloatingControls = false
                            showUnlockPill = true
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "قفل الشاشة",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Picture-in-Picture Button
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
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        // Subtle persistent Back button when controls are hidden so the user is never trapped
        if (!isLocked && !showFloatingControls) {
            IconButton(
                onClick = safelyNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(
                        start = if (isRtl) 68.dp else 14.dp,
                        top = 10.dp
                    )
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
