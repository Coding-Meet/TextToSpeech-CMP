package com.example.texttospeech

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSScreen() {
    val viewModel = viewModel {
        TTSViewModel()
    }
    val currentWordRange by viewModel.currentWordRange.collectAsState()
    val ttsState by viewModel.ttsState.collectAsState()
    val isInitialized by viewModel.isInitialized.collectAsState()
    val sampleTexts = listOf(
        "Welcome to Text-to-Speech with real-time highlighting. This demonstration shows how words are highlighted as they are spoken.",
        "The quick brown fox jumps over the lazy dog. This sentence contains every letter in the English alphabet.",
        "Technology has revolutionized the way we communicate, learn, and work in the modern world.",
        "Reading aloud helps improve pronunciation, comprehension, and overall language skills."
    )

    var customText by rememberSaveable { mutableStateOf(sampleTexts[0]) }
    var selectedSampleIndex by rememberSaveable { mutableStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.release()
        }
    }
    LifecycleResumeEffect(Unit) {
        onPauseOrDispose { viewModel.pause() }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Text-to-Speech",
                            style = MaterialTheme.typography.titleLarge,
                            color = TTSTheme.onPrimaryLight
                        )
                        Text(
                            text = "Real-time word highlighting",
                            style = MaterialTheme.typography.bodySmall,
                            color = TTSTheme.onPrimaryLight.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettings = !showSettings }
                    ) {
                        Icon(
                            if (showSettings) Icons.Default.ExpandLess else Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TTSTheme.onPrimaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TTSTheme.primaryLight
                )
            )
        },
        containerColor = TTSTheme.backgroundLight,
        contentColor = TTSTheme.onBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Settings Panel
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = TTSTheme.secondaryContainerLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Sample Texts",
                            style = MaterialTheme.typography.titleMedium,
                            color = TTSTheme.onSecondaryContainerLight,
                            fontWeight = FontWeight.Bold
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(sampleTexts) { index, text ->
                                FilterChip(
                                    onClick = {
                                        if (viewModel.isIdle()) {
                                            selectedSampleIndex = index
                                            customText = text
                                        }
                                    },
                                    label = {
                                        Text(
                                            "Sample ${index + 1}",
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    selected = selectedSampleIndex == index,
                                    enabled = ttsState == TTSState.IDLE,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TTSTheme.primaryLight,
                                        selectedLabelColor = TTSTheme.onPrimaryLight
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Text Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TTSTheme.surfaceLight
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Text to speak:",
                            style = MaterialTheme.typography.titleMedium,
                            color = TTSTheme.primaryLight,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "${customText.length} characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = TTSTheme.onSurfaceVariantLight
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customText,
                        onValueChange = {
                            customText = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                        enabled = ttsState == TTSState.IDLE,
                        placeholder = {
                            Text(
                                "Enter your text here to convert to speech...",
                                color = TTSTheme.onSurfaceVariantLight.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TTSTheme.primaryLight,
                            cursorColor = TTSTheme.primaryLight,
                            disabledBorderColor = TTSTheme.outlineLight.copy(alpha = 0.5f)
                        )
                    )

                    if (ttsState != TTSState.IDLE) {
                        Text(
                            "Text editing disabled during speech",
                            style = MaterialTheme.typography.bodySmall,
                            color = TTSTheme.warningLight,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Live Text Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = TTSTheme.surfaceVariantLight
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Live Speech:",
                            style = MaterialTheme.typography.titleMedium,
                            color = TTSTheme.primaryLight,
                            fontWeight = FontWeight.Bold
                        )

                        // Enhanced Status Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val (statusColor, statusText, statusIcon) = when (ttsState) {
                                TTSState.PLAYING -> Triple(
                                    TTSTheme.successLight,
                                    "Speaking",
                                    Icons.AutoMirrored.Filled.VolumeUp
                                )

                                TTSState.PAUSED -> Triple(
                                    TTSTheme.warningLight,
                                    "Paused",
                                    Icons.Default.Pause
                                )

                                TTSState.IDLE -> Triple(
                                    TTSTheme.onSurfaceVariantLight,
                                    "Ready",
                                    Icons.AutoMirrored.Filled.VolumeOff
                                )
                            }

                            Icon(
                                statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(statusColor, CircleShape)
                            )

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HighlightedText(
                        text = customText,
                        highlightRange = currentWordRange,
                        modifier = Modifier.fillMaxWidth(),
                        normalTextColor = TTSTheme.onSurfaceVariantLight,
                        highlightColor = TTSTheme.primaryLight.copy(alpha = 0.3f),
                        highlightTextColor = TTSTheme.primaryLight
                    )
                }
            }

            // Control Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = TTSTheme.surfaceLight
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Controls",
                        style = MaterialTheme.typography.titleMedium,
                        color = TTSTheme.primaryLight,
                        fontWeight = FontWeight.Bold
                    )

                    // Main Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play Button
                        Button(
                            onClick = {
                                viewModel.speak(customText)
                            },
                            enabled = isInitialized && ttsState == TTSState.IDLE && customText.isNotBlank(),
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TTSTheme.primaryLight,
                                contentColor = TTSTheme.onPrimaryLight,
                                disabledContainerColor = TTSTheme.outlineLight.copy(alpha = 0.3f),
                                disabledContentColor = TTSTheme.onSurfaceVariantLight.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play", fontWeight = FontWeight.Bold)
                        }

                        // Pause Button
                        Button(
                            onClick = { viewModel.pause() },
                            enabled = ttsState == TTSState.PLAYING,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TTSTheme.warningLight,
                                contentColor = Color.White,
                                disabledContainerColor = TTSTheme.outlineLight.copy(alpha = 0.3f),
                                disabledContentColor = TTSTheme.onSurfaceVariantLight.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Resume Button
                        Button(
                            onClick = { viewModel.resume() },
                            enabled = viewModel.isPaused(),
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TTSTheme.successLight,
                                contentColor = Color.White,
                                disabledContainerColor = TTSTheme.outlineLight.copy(alpha = 0.3f),
                                disabledContentColor = TTSTheme.onSurfaceVariantLight.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }

                        // Stop Button
                        Button(
                            onClick = { viewModel.stop() },
                            enabled = ttsState != TTSState.IDLE,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TTSTheme.errorLight,
                                contentColor = TTSTheme.onErrorLight,
                                disabledContainerColor = TTSTheme.outlineLight.copy(alpha = 0.3f),
                                disabledContentColor = TTSTheme.onSurfaceVariantLight.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Clear Text Button
                        OutlinedButton(
                            onClick = {
                                customText = ""
                            },
                            enabled = ttsState == TTSState.IDLE && customText.isNotEmpty(),
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TTSTheme.errorLight,
                                disabledContentColor = TTSTheme.onSurfaceVariantLight.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                2.dp,
                                if (ttsState == TTSState.IDLE && customText.isNotEmpty()) TTSTheme.errorLight
                                else TTSTheme.outlineLight.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontWeight = FontWeight.Medium)
                        }

                        // Random Sample Button
                        OutlinedButton(
                            onClick = {
                                val randomIndex = sampleTexts.indices.random()
                                selectedSampleIndex = randomIndex
                                customText = sampleTexts[randomIndex]
                            },
                            enabled = ttsState == TTSState.IDLE,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TTSTheme.primaryLight,
                                disabledContentColor = TTSTheme.onSurfaceVariantLight.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                2.dp,
                                if (ttsState == TTSState.IDLE) TTSTheme.primaryLight
                                else TTSTheme.outlineLight.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Random", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Initialization Status
            if (!isInitialized) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = TTSTheme.errorContainerLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = TTSTheme.onErrorContainerLight
                        )

                        Column {
                            Text(
                                "Initializing Text-to-Speech",
                                style = MaterialTheme.typography.titleSmall,
                                color = TTSTheme.onErrorContainerLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Please wait while we set up the speech engine...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TTSTheme.onErrorContainerLight.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

