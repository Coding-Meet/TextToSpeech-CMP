package com.example.texttospeech

import android.app.Activity
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class AndroidTTSProvider : TTSProvider {
    private var tts: TextToSpeech? = null
    private var context = activityProvider.invoke()

    private var isPausedState = false
    private var originalText: String = ""
    private var pausedPosition = 0
    private var resumeOffset = 0

    // Callback blocks
    private var onWordBoundaryCallback: ((Int, Int) -> Unit)? = null
    private var onCompleteCallback: (() -> Unit)? = null

    override fun initialize(onInitialized: () -> Unit) {
        println("🚀 Android TTS Initialized")
        context?.let { ctx ->
            tts = TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    println("✅ Android TTS engine ready")
                    onInitialized()
                } else {
                    println("❌ Android TTS initialization failed with status: $status")
                }
            }
        }
    }

    override fun speak(
        text: String,
        onWordBoundary: (wordStart: Int, wordEnd: Int) -> Unit,
        onStart: () -> Unit,
        onComplete: () -> Unit
    ) {
        println("🗣️ Android Speak called with text: '${text.take(50)}...'")
        println("📊 Current state - isPaused: $isPausedState, resumeOffset: $resumeOffset")

        // Store callbacks for resume functionality
        onWordBoundaryCallback = onWordBoundary
        onCompleteCallback = onComplete

        // Check if originalText is empty to determine if this is first time or resume
        val isFirstTimeSpeak = originalText.isEmpty()

        if (isFirstTimeSpeak) {
            println("🆕 First time speaking - resetting state")
            originalText = text
            pausedPosition = 0
            resumeOffset = 0
        } else {
            println("🔄 Resume speaking - keeping resumeOffset: $resumeOffset")
        }

        // Set paused state to false after checking
        isPausedState = false

        tts?.let { textToSpeech ->
            val utteranceId = "tts_utterance_${System.currentTimeMillis()}"
            println("🎬 Starting utterance with ID: $utteranceId")

            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    println("🎤 Android TTS Started")
                    onStart()
                }

                override fun onDone(utteranceId: String?) {
                    println("✅ Android TTS Finished - isPaused: $isPausedState")
                    if (!isPausedState) {
                        println("🏁 Speech finished normally")
                        onWordBoundary(-1, -1) // Reset highlight
                        onComplete()
                        // Reset everything after completion
                        originalText = ""
                        pausedPosition = 0
                        resumeOffset = 0
                        println("🔄 State reset after completion")
                    } else {
                        println("⏸️ Speech finished due to pause - keeping state")
                    }
                }

                override fun onError(utteranceId: String?) {
                    println("❌ Android TTS Error occurred")
                    onComplete()
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    if (!isPausedState) {
                        // Calculate position in original text for resume functionality
                        val actualStart = resumeOffset + start
                        val actualEnd = resumeOffset + end - 1

                        println("🎯 Android word boundary: local($start-$end) -> actual($actualStart-$actualEnd)")
                        println("📏 Original text length: ${originalText.length}, resumeOffset: $resumeOffset")

                        // Bounds check
                        if (actualStart >= 0 && actualStart < originalText.length) {
                            // Find word boundaries in original text
                            val wordStart = findWordStart(originalText, actualStart)
                            val wordEnd =
                                findWordEnd(originalText, minOf(actualEnd, originalText.length - 1))

                            // Update paused position for future resume
                            pausedPosition = wordStart

                            println("✨ Android highlighting: $wordStart-$wordEnd, updated pausedPosition: $pausedPosition")

                            // Show highlighted text
                            if (wordStart <= wordEnd && wordEnd < originalText.length) {
                                val highlightedText = originalText.substring(wordStart, wordEnd + 1)
                                println("📝 Highlighted text: '$highlightedText'")
                            }

                            onWordBoundary(wordStart, wordEnd)
                        } else {
                            println("⚠️ Android word boundary actualStart($actualStart) out of bounds!")
                        }
                    }
                }
            })

            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    override fun stop() {
        println("🛑 Android Stop called")
        tts?.stop()
        isPausedState = false
        pausedPosition = 0
        resumeOffset = 0
        originalText = ""
        onWordBoundaryCallback?.invoke(-1, -1)
        println("🔄 All state reset after stop")
    }

    override fun pause() {
        println("⏸️ Android Pause called")
        if (tts?.isSpeaking == true) {
            println("📍 Pausing at position: $pausedPosition")
            isPausedState = true
            tts?.stop()
        } else {
            println("⚠️ Cannot pause - TTS not speaking")
        }
    }

    override fun resume() {
        println("▶️ Android Resume called")
        println("📊 Resume state - isPaused: $isPausedState, pausedPos: $pausedPosition, originalText.length: ${originalText.length}")

        if (isPausedState && originalText.isNotEmpty()) {
            // Find the remaining text from paused position
            val remainingText = if (pausedPosition < originalText.length) {
                // Find the start of the word at paused position to avoid cutting words
                val wordStartPos = findWordStart(originalText, pausedPosition)
                resumeOffset = wordStartPos  // Set offset for correct highlighting
                println("📍 Resume offset set to: $resumeOffset")

                val remaining = originalText.substring(wordStartPos)
                println("📝 Remaining text: '${remaining.take(50)}...'")
                remaining
            } else {
                println("⚠️ No remaining text to speak")
                return // Nothing left to speak
            }

            // Resume speaking with the remaining text
            onWordBoundaryCallback?.let { callback ->
                onCompleteCallback?.let { complete ->
                    println("🔄 Calling speak with remaining text, resumeOffset should stay: $resumeOffset")
                    speak(remainingText, callback, {}, complete)
                    println("📍 After speak call, resumeOffset is: $resumeOffset")
                }
            }
        } else {
            println("⚠️ Cannot resume - not in paused state or no original text")
        }
    }

    override fun isPlaying(): Boolean {
        val playing = tts?.isSpeaking == true && !isPausedState
        println("❓ Android isPlaying: $playing (speaking: ${tts?.isSpeaking}, paused: $isPausedState)")
        return playing
    }

    override fun isPaused(): Boolean {
        println("❓ Android isPaused: $isPausedState")
        return isPausedState
    }

    override fun release() {
        println("🗑️ Android Release called")
        tts?.shutdown()
        tts = null
        isPausedState = false
        pausedPosition = 0
        resumeOffset = 0
        originalText = ""
        println("🔄 Android TTS completely released")
    }

    private fun findWordStart(text: String, position: Int): Int {
        var start = maxOf(0, minOf(position, text.length - 1))
        while (start > 0 && !text[start - 1].isWhitespace()) {
            start--
        }
        return start
    }

    private fun findWordEnd(text: String, position: Int): Int {
        var end = maxOf(0, minOf(position, text.length - 1))
        while (end < text.length - 1 && !text[end + 1].isWhitespace()) {
            end++
        }
        return end
    }
}


private var activityProvider: () -> Activity? = {
    null
}

fun setActivityProvider(provider: () -> Activity?) {
    activityProvider = provider
}

actual fun getTTSProvider(): TTSProvider {
    return AndroidTTSProvider()
}