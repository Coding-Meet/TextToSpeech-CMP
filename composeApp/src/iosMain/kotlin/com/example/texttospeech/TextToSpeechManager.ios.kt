package com.example.texttospeech


actual class TextToSpeechManager {
    private var ttsProvider = ttsProvider()

    actual fun initialize(onInitialized: () -> Unit) {
        ttsProvider?.initialize()
        onInitialized()
    }

    actual fun speak(
        text: String,
        onWordBoundary: (wordStart: Int, wordEnd: Int) -> Unit,
        onStart: () -> Unit,
        onComplete: () -> Unit
    ) {
        ttsProvider?.speak(text, onWordBoundary, onStart, onComplete)
    }

    actual fun stop() {
        ttsProvider?.stop()
    }

    actual fun pause() {
        ttsProvider?.pause()
    }

    actual fun resume() {
        ttsProvider?.resume()
    }

    actual fun isPlaying(): Boolean {
        return ttsProvider?.isPlaying() ?: false
    }

    actual fun isPaused(): Boolean {
        return ttsProvider?.isPaused() ?: false
    }

    actual fun release() {
        ttsProvider?.release()
    }
}

private var ttsProvider: () -> TTSProvider? = {  // Add this
    null
}

fun setTTSProvider(provider: () -> TTSProvider) {  // Add this function
    ttsProvider = provider
}