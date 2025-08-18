package com.example.texttospeech


private var ttsProvider: () -> TTSProvider? = {
    null
}

fun setTTSProvider(provider: () -> TTSProvider) {
    ttsProvider = provider
}

actual fun getTTSProvider(): TTSProvider {
    return ttsProvider.invoke() ?: throw IllegalStateException("TTS provider not set")
}