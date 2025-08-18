package com.example.texttospeech


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class TTSState {
    IDLE, PLAYING, PAUSED
}

class TTSViewModel : ViewModel() {
    private val _currentWordRange = MutableStateFlow(-1..-1)
    val currentWordRange: StateFlow<IntRange> = _currentWordRange

    private val _ttsState = MutableStateFlow(TTSState.IDLE)
    val ttsState: StateFlow<TTSState> = _ttsState

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val ttsManager = TextToSpeechManager()
    private var currentText: String = ""

    init {
        ttsManager.initialize {
            _isInitialized.value = true
        }
    }

    fun speak(text: String) {
        currentText = text
        // Reset highlight immediately when starting
        _currentWordRange.value = -1..-1

        ttsManager.speak(
            text = text,
            onWordBoundary = { wordStart, wordEnd ->
                _currentWordRange.value = wordStart..wordEnd
            },
            onStart = {
                _ttsState.value = TTSState.PLAYING
            },
            onComplete = {
                _ttsState.value = TTSState.IDLE
                _currentWordRange.value = -1..-1
            }
        )
    }

    fun stop() {
        ttsManager.stop()
        _ttsState.value = TTSState.IDLE
        _currentWordRange.value = -1..-1
    }

    fun pause() {
        if (_ttsState.value == TTSState.PLAYING) {
            ttsManager.pause()
            _ttsState.value = TTSState.PAUSED
        }
    }

    fun resume() {
        if (_ttsState.value == TTSState.PAUSED) {
            ttsManager.resume()
            _ttsState.value = TTSState.PLAYING
        }
    }

    fun isPlaying(): Boolean = _ttsState.value == TTSState.PLAYING
    fun isPaused(): Boolean = _ttsState.value == TTSState.PAUSED
    fun isIdle(): Boolean = _ttsState.value == TTSState.IDLE

    fun release() {
        ttsManager.release()
    }
}

