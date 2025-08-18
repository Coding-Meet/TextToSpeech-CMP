package com.example.texttospeech

expect class TextToSpeechManager() {
    fun initialize(onInitialized: () -> Unit)
    fun speak(
        text: String,
        onWordBoundary: (wordStart: Int, wordEnd: Int) -> Unit,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {}
    )

    fun stop()
    fun pause()
    fun resume()
    fun isPlaying(): Boolean
    fun isPaused(): Boolean
    fun release()
}

interface TTSProvider {
    fun initialize()
    fun speak(
        text: String,
        onWordBoundary: (Int, Int) -> Unit,
        onStart: () -> Unit,
        onComplete: () -> Unit
    )

    fun stop()
    fun pause()
    fun resume()
    fun isPlaying(): Boolean
    fun isPaused(): Boolean
    fun release()
}