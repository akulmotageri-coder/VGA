package com.example.vga.audioseparation

import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.vga.audioseparation.voice.VoiceInputScreen
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private val WarmBackground = Color(0xFFF9F8F6)
private val Berry = Color(0xFF9E2A4B)
private val Slate = Color(0xFF2D3142)
private val Muted = Color(0xFF6C727F)
private val White = Color(0xFFFFFFFF)

@Composable
fun AudioSeparationScreen(
    onBack: () -> Unit
) {
    var showVoiceInput by remember { mutableStateOf(false) }
    var showCallRecordings by remember { mutableStateOf(false) }

    if (showCallRecordings) {
        CallRecordingsScreen(
            onBack = {
                showCallRecordings = false
            }
        )
        return
    }

    if (showVoiceInput) {
        VoiceInputScreen(
            onBack = {
                showVoiceInput = false
            }
        )
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Top bar
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
                text = "Audio Separation",
                color = Slate,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Header
        Text(
            text = "Keep your voice.",
            color = Slate,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Leave the rest behind. ♡",
            color = Muted,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        AudioFeatureCard(
            title = "Your Voice",
            description = "Record a clean sample of your voice.",
            icon = "♪",
            onClick = {
                showVoiceInput = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AudioFeatureCard(
            title = "Call Recordings",
            description = "Add recordings that you want to process.",
            icon = "♪",
            onClick = {
                showCallRecordings = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AudioFeatureCard(
            title = "Extracted Voice",
            description = "Your processed recordings will appear here.",
            icon = "♪"
        )
    }
}

@Composable
private fun AudioFeatureCard(
    title: String,
    description: String,
    icon: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(22.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = icon,
            color = Berry,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = title,
                color = Slate,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = Muted,
                fontSize = 14.sp
            )
        }
    }
}