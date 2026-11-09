package com.example.ui.avatar

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.VoiceState
import com.example.ui.theme.ArishiPurpleDark

class AvatarBridge(private val onReady: () -> Unit) {
    @JavascriptInterface
    fun onAvatarReady() {
        onReady()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArishiAvatarView(
    voiceState: VoiceState,
    audioAmplitude: Float,
    emotion: String = "happy",
    theme: String = "default",
    isLowQuality: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isAvatarReady by remember { mutableStateOf(false) }

    // Synchronize voice state with 3D avatar
    LaunchedEffect(voiceState, isAvatarReady) {
        if (isAvatarReady) {
            val stateCode = when (voiceState) {
                VoiceState.LISTENING -> "listening"
                VoiceState.THINKING -> "thinking"
                VoiceState.SPEAKING -> "speaking"
                else -> "idle"
            }
            webViewInstance?.evaluateJavascript("window.setAvatarState && window.setAvatarState('$stateCode');", null)
        }
    }

    // Synchronize audio amplitude for lip sync
    LaunchedEffect(audioAmplitude, isAvatarReady) {
        if (isAvatarReady && voiceState == VoiceState.SPEAKING) {
            webViewInstance?.evaluateJavascript("window.setAudioAmplitude && window.setAudioAmplitude($audioAmplitude);", null)
        }
    }

    // Synchronize emotion
    LaunchedEffect(emotion, isAvatarReady) {
        if (isAvatarReady) {
            webViewInstance?.evaluateJavascript("window.setEmotion && window.setEmotion('$emotion');", null)
        }
    }

    // Synchronize background theme
    LaunchedEffect(theme, isAvatarReady) {
        if (isAvatarReady) {
            webViewInstance?.evaluateJavascript("window.setBackgroundTheme && window.setBackgroundTheme('$theme');", null)
        }
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArishiPurpleDark)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(Color.TRANSPARENT)
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    addJavascriptInterface(AvatarBridge {
                        isAvatarReady = true
                    }, "AndroidAvatarBridge")

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isAvatarReady = true
                        }
                    }

                    loadUrl("file:///android_asset/avatar/index.html")
                    webViewInstance = this
                }
            },
            update = { view ->
                webViewInstance = view
            }
        )
    }
}
