package com.example.texttospeech

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TTSScreen() {
    val viewModel = viewModel {
        TTSViewModel()
    }
    val currentWordRange by viewModel.currentWordRange.collectAsState()
    val ttsState by viewModel.ttsState.collectAsState()
    val isInitialized by viewModel.isInitialized.collectAsState()

    val sampleText = """
        Welcome to Text-to-Speech with real-time highlighting. 
        This demonstration shows how words are highlighted as they are spoken. 
        You can see each word being emphasized as the speech synthesis progresses through the text.
    """.trimIndent()

    var customText by rememberSaveable { mutableStateOf(sampleText) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.release()
        }
    }
    LifecycleResumeEffect(Unit) {
        onPauseOrDispose { viewModel.pause() }
    }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Text-to-Speech Demo", style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Text to speak:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = ttsState == TTSState.IDLE
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Live Text:", style = MaterialTheme.typography.labelLarge)

                    // Status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColor = when (ttsState) {
                            TTSState.PLAYING -> Color.Green
                            TTSState.PAUSED -> Color.Yellow
                            TTSState.IDLE -> Color.Gray
                        }

                        Box(
                            modifier = Modifier.size(8.dp).background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (ttsState) {
                                TTSState.PLAYING -> "Playing"
                                TTSState.PAUSED -> "Paused"
                                TTSState.IDLE -> "Ready"
                            }, style = MaterialTheme.typography.bodySmall, color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HighlightedText(
                    text = customText,
                    highlightRange = currentWordRange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column {
            Button(
                onClick = {
                    viewModel.speak(customText)
                },
                enabled = isInitialized && ttsState == TTSState.IDLE,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play")
            }

            Button(
                onClick = { viewModel.pause() },
                enabled = ttsState == TTSState.PLAYING,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Pause, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pause")
            }

            Button(
                onClick = { viewModel.resume() },
                enabled = ttsState == TTSState.PAUSED,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Resume")
            }

            Button(
                onClick = { viewModel.stop() },
                enabled = ttsState != TTSState.IDLE,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop")
            }
        }
        if (!isInitialized) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text("Initializing Text-to-Speech...")
        }
    }
}

