package com.esheeq.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.esheeq.app.ui.navigation.EsheeqNavGraph
import com.esheeq.app.ui.theme.EsheeqTheme
import com.esheeq.app.ui.theme.TerminalBg

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EsheeqTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = TerminalBg
                    ) {
                        EsheeqNavGraph()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val inPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInPictureInPictureMode
        } else false
        if (!inPip) {
            flushAudioOutput(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flushAudioOutput(this)
    }

    companion object {
        fun flushAudioOutput(context: Context) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(audioAttributes)
                        .build()
                    audioManager.requestAudioFocus(focusRequest)
                    audioManager.abandonAudioFocusRequest(focusRequest)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    @Suppress("DEPRECATION")
                    audioManager.abandonAudioFocus(null)
                }
            } catch (_: Exception) {}
        }
    }
}

