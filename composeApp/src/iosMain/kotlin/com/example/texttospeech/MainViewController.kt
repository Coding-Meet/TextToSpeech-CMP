package com.example.texttospeech

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(ttsProvider: TTSProvider) = ComposeUIViewController(
    configure = {
        setTTSProvider {
            ttsProvider
        }
    }
) { App() }