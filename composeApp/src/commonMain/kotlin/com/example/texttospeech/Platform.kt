package com.example.texttospeech

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform