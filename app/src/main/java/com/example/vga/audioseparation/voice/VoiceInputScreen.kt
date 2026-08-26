package com.example.vga.audioseparation.voice

import com.example.vga.audioseparation.audio.AudioPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.vga.audioseparation.audio.VoiceRecorder
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
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
    val voiceRecorder = VoiceRecorder(context)
    val audioPlayer = AudioPlayer()
    val outputFile = context.getExternalFilesDir(null)
        ?.resolve("my_voice_sample.m4a")
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Back button and title
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

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Audio Input",
                color = Slate,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

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

        Spacer(modifier = Modifier.height(28.dp))

        // Your Voice card
        VoiceInputCard(
            title = "Your Voice",
            description = "Record a sample to help us recognize you.",
            actionText = "Record My Voice",
            accentColor = Color(0xFFFFE5EC),
            onActionClick = {
                if (!isRecording) {
                    outputFile?.let {
                        voiceRecorder.startRecording(it.absolutePath)
                        isRecording = true
                    }
                } else {
                    voiceRecorder.stopRecording()
                    isRecording = false
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(12.dp))

        VoiceInputCard(
            title = "Your Saved Voice",
            description = "Listen to your recorded voice sample.",
            actionText = if (isPlaying) "Stop Playback" else "Play My Voice",
            accentColor = Color(0xFFE6F4EA),
            onActionClick = {
                if (!isPlaying) {
                    outputFile?.let {
                        audioPlayer.play(it.absolutePath)
                        isPlaying = true
                    }
                } else {
                    audioPlayer.stop()
                    isPlaying = false
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Call Recording card
        VoiceInputCard(
            title = "Call Recording",
            description = "Choose a recording you want to process.",
            actionText = "Add Recording",
            accentColor = Color(0xFFE3F2FD),
            onActionClick = {
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

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            color = Muted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

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