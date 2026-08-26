package com.example.vga.audioseparation.voice

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vga.audioseparation.audio.AudioPlayer
import com.example.vga.audioseparation.audio.VoiceRecorder
import com.example.vga.audioseparation.processing.ReferenceVoiceProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val WarmBackground = Color(0xFFF9F8F6)
private val Berry = Color(0xFF9E2A4B)
private val Slate = Color(0xFF2D3142)
private val Muted = Color(0xFF6C727F)
private val White = Color(0xFFFFFFFF)

@Composable
fun VoiceInputScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val voiceRecorder =
        remember {
            VoiceRecorder(context)
        }

    val audioPlayer =
        remember {
            AudioPlayer()
        }

    val outputFile =
        remember {
            context.getExternalFilesDir(null)
                ?.resolve("my_voice_sample.m4a")
        }

    var isRecording by remember {
        mutableStateOf(false)
    }

    var isPlaying by remember {
        mutableStateOf(false)
    }

    var isProcessingReference by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------
        // Back button and title
        // --------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Text(
                    text = "‹",
                    color = Slate,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Text(
                text = "Audio Input",
                color = Slate,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Add your audio",
            color = Slate,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Record your voice or add a call recording.",
            color = Muted,
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // --------------------------------
        // Your Voice card
        // --------------------------------

        VoiceInputCard(
            title = "Your Voice",
            description =
                when {
                    isRecording ->
                        "Recording... Tap to stop and process your voice."

                    isProcessingReference ->
                        "Processing your voice sample..."

                    else ->
                        "Record a sample to help us recognize you."
                },
            actionText =
                when {
                    isRecording ->
                        "Stop Recording"

                    isProcessingReference ->
                        "Processing..."

                    else ->
                        "Record My Voice"
                },
            accentColor = Color(0xFFFFE5EC),
            onActionClick = {

                if (isProcessingReference) {
                    return@VoiceInputCard
                }

                if (!isRecording) {

                    outputFile?.let { file ->

                        voiceRecorder.startRecording(
                            file.absolutePath
                        )

                        isRecording = true

                        Log.d(
                            "VGA_REFERENCE",
                            "Started recording reference voice"
                        )
                    }

                } else {

                    // --------------------------------
                    // Stop recording
                    // --------------------------------

                    voiceRecorder.stopRecording()

                    isRecording = false
                    isProcessingReference = true

                    Log.d(
                        "VGA_REFERENCE",
                        "Reference recording stopped"
                    )

                    // --------------------------------
                    // Process M4A in background
                    // --------------------------------

                    outputFile?.let { file ->

                        CoroutineScope(
                            Dispatchers.IO
                        ).launch {

                            try {

                                Log.d(
                                    "VGA_REFERENCE",
                                    "Processing: ${file.absolutePath}"
                                )

                                val embedding =
                                    ReferenceVoiceProcessor
                                        .processAndSave(
                                            context,
                                            file
                                        )

                                Log.d(
                                    "VGA_REFERENCE",
                                    "Reference voice ready"
                                )

                                Log.d(
                                    "VGA_REFERENCE",
                                    "Embedding size=${embedding.size}"
                                )

                            } catch (e: Exception) {

                                Log.e(
                                    "VGA_REFERENCE",
                                    "Failed to process reference voice",
                                    e
                                )

                            } finally {

                                isProcessingReference = false
                            }
                        }
                    }
                }
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------
        // Saved Voice card
        // --------------------------------

        VoiceInputCard(
            title = "Your Saved Voice",
            description =
                "Listen to your recorded voice sample.",
            actionText =
                if (isPlaying) {
                    "Stop Playback"
                } else {
                    "Play My Voice"
                },
            accentColor = Color(0xFFE6F4EA),
            onActionClick = {

                if (!isPlaying) {

                    outputFile?.let { file ->

                        if (file.exists()) {

                            audioPlayer.play(
                                file.absolutePath
                            )

                            isPlaying = true
                        }
                    }

                } else {

                    audioPlayer.stop()

                    isPlaying = false
                }
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------
        // Call Recording card
        // --------------------------------

        VoiceInputCard(
            title = "Call Recording",
            description =
                "Choose a recording you want to process.",
            actionText = "Add Recording",
            accentColor = Color(0xFFE3F2FD),
            onActionClick = {
                // Existing call recording logic
                // will be connected here.
            }
        )
    }
}

@Composable
private fun VoiceInputCard(
    title: String,
    description: String,
    actionText: String,
    accentColor: Color,
    onActionClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(20.dp)
    ) {

        Text(
            text = title,
            color = Slate,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = description,
            color = Muted,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = actionText,
            color = Berry,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable {
                    onActionClick()
                }
                .background(
                    color = accentColor,
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 10.dp
                )
        )
    }
}

