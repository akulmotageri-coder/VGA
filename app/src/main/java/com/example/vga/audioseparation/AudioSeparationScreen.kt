package com.example.vga.audioseparation

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.vga.audioseparation.voice.VoiceInputScreen
import java.io.File

private val WarmBackground = Color(0xFFF9F8F6)
private val Berry = Color(0xFF9E2A4B)
private val Slate = Color(0xFF2D3142)
private val Muted = Color(0xFF6C727F)
private val White = Color(0xFFFFFFFF)


// ============================================================
// AUDIO SEPARATION SCREEN
// ============================================================

@Composable
fun AudioSeparationScreen(
    onBack: () -> Unit
) {

    var showVoiceInput by remember {
        mutableStateOf(false)
    }

    var showCallRecordings by remember {
        mutableStateOf(false)
    }

    var showExtractedVoice by remember {
        mutableStateOf(false)
    }


    // ========================================================
    // VOICE INPUT
    // ========================================================

    if (showVoiceInput) {

        VoiceInputScreen(
            onBack = {
                showVoiceInput = false
            }
        )

        return
    }


    // ========================================================
    // CALL RECORDINGS
    // ========================================================

    if (showCallRecordings) {

        CallRecordingsScreen(
            onBack = {
                showCallRecordings = false
            }
        )

        return
    }


    // ========================================================
    // EXTRACTED VOICE
    // ========================================================

    if (showExtractedVoice) {

        ExtractedVoiceScreen(
            onBack = {
                showExtractedVoice = false
            }
        )

        return
    }


    // ========================================================
    // MAIN SCREEN
    // ========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ----------------------------------------------------
        // Top bar
        // ----------------------------------------------------

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
                text = "Audio Separation",
                color = Slate,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ----------------------------------------------------
        // Header
        // ----------------------------------------------------

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


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ----------------------------------------------------
        // Your Voice
        // ----------------------------------------------------

        AudioFeatureCard(
            title = "Your Voice",
            description = "Record a clean sample of your voice.",
            icon = "♪",
            onClick = {
                showVoiceInput = true
            }
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ----------------------------------------------------
        // Call Recordings
        // ----------------------------------------------------

        AudioFeatureCard(
            title = "Call Recordings",
            description = "Add recordings that you want to process.",
            icon = "♪",
            onClick = {
                showCallRecordings = true
            }
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ----------------------------------------------------
        // Extracted Voice
        // ----------------------------------------------------

        AudioFeatureCard(
            title = "Extracted Voice",
            description = "Your processed recordings will appear here.",
            icon = "♪",
            onClick = {
                showExtractedVoice = true
            }
        )
    }
}


// ============================================================
// EXTRACTED VOICE SCREEN
// ============================================================

@Composable
fun ExtractedVoiceScreen(
    onBack: () -> Unit
) {

    val context =
        LocalContext.current


    // --------------------------------------------------------
    // Files
    // --------------------------------------------------------

    var files by remember {
        mutableStateOf<List<File>>(emptyList())
    }


    // --------------------------------------------------------
    // Currently playing file
    // --------------------------------------------------------

    var currentlyPlaying by remember {
        mutableStateOf<String?>(null)
    }


    // --------------------------------------------------------
    // MediaPlayer
    // --------------------------------------------------------

    val mediaPlayer =
        remember {
            MediaPlayer()
        }


    // --------------------------------------------------------
    // Load files when screen opens
    // --------------------------------------------------------

    LaunchedEffect(Unit) {

        files =
            loadExtractedVoiceFiles(
                context
            )
    }


    // --------------------------------------------------------
    // Cleanup MediaPlayer
    // --------------------------------------------------------

    DisposableEffect(Unit) {

        onDispose {

            try {

                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }

            } catch (_: Exception) {
            }

            mediaPlayer.release()
        }
    }


    // ========================================================
    // UI
    // ========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ----------------------------------------------------
        // Top bar
        // ----------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {

                    stopPlayback(
                        mediaPlayer
                    )

                    currentlyPlaying = null

                    onBack()
                }
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
                text = "Extracted Voice",
                color = Slate,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ----------------------------------------------------
        // Header
        // ----------------------------------------------------

        Text(
            text = "Your extracted voice.",
            color = Slate,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Processed recordings are stored here.",
            color = Muted,
            fontSize = 16.sp
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // ====================================================
        // FILE LIST
        // ====================================================

        if (files.isEmpty()) {

            EmptyExtractedVoiceCard()

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = files,
                    key = {
                        it.absolutePath
                    }
                ) { file ->

                    ExtractedVoiceCard(
                        file = file,

                        isPlaying =
                            currentlyPlaying ==
                                    file.absolutePath,

                        onPlay = {

                            if (
                                currentlyPlaying ==
                                file.absolutePath
                            ) {

                                stopPlayback(
                                    mediaPlayer
                                )

                                currentlyPlaying = null

                            } else {

                                try {

                                    mediaPlayer.reset()

                                    mediaPlayer.setDataSource(
                                        file.absolutePath
                                    )

                                    mediaPlayer.prepare()

                                    mediaPlayer.start()

                                    currentlyPlaying =
                                        file.absolutePath

                                    mediaPlayer.setOnCompletionListener {

                                        currentlyPlaying =
                                            null
                                    }

                                } catch (_: Exception) {

                                    currentlyPlaying = null
                                }
                            }
                        },

                        onDelete = {

                            if (
                                currentlyPlaying ==
                                file.absolutePath
                            ) {

                                stopPlayback(
                                    mediaPlayer
                                )

                                currentlyPlaying = null
                            }

                            file.delete()

                            files =
                                loadExtractedVoiceFiles(
                                    context
                                )
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// EMPTY STATE
// ============================================================

@Composable
private fun EmptyExtractedVoiceCard() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "No extracted recordings yet.",
            color = Slate,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text =
                "Share a call recording with VGA to process it.",
            color = Muted,
            fontSize = 14.sp
        )
    }
}


// ============================================================
// EXTRACTED VOICE CARD
// ============================================================

@Composable
private fun ExtractedVoiceCard(
    file: File,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit
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

        // ----------------------------------------------------
        // File information
        // ----------------------------------------------------

        Text(
            text = file.name,
            color = Slate,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = formatFileSize(
                file.length()
            ),
            color = Muted,
            fontSize = 13.sp
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ----------------------------------------------------
        // Buttons
        // ----------------------------------------------------

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    if (isPlaying) {
                        "Stop"
                    } else {
                        "Play"
                    },

                color = Berry,

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold,

                modifier = Modifier
                    .clickable {
                        onPlay()
                    }
                    .background(
                        color =
                            Color(0xFFFFE5EC),
                        shape =
                            RoundedCornerShape(50.dp)
                    )
                    .padding(
                        horizontal = 18.dp,
                        vertical = 10.dp
                    )
            )


            Spacer(
                modifier = Modifier.width(10.dp)
            )


            Text(
                text = "Delete",

                color = Muted,

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold,

                modifier = Modifier
                    .clickable {
                        onDelete()
                    }
                    .background(
                        color =
                            Color(0xFFF1F1F1),
                        shape =
                            RoundedCornerShape(50.dp)
                    )
                    .padding(
                        horizontal = 18.dp,
                        vertical = 10.dp
                    )
            )
        }
    }
}


// ============================================================
// LOAD EXTRACTED VOICE FILES
// ============================================================

private fun loadExtractedVoiceFiles(
    context: Context
): List<File> {

    val directory =
        File(
            context.filesDir,
            "extracted_voice"
        )

    if (!directory.exists()) {

        directory.mkdirs()

        return emptyList()
    }

    return directory
        .listFiles()
        ?.filter {

            it.isFile &&
                    it.extension.equals(
                        "wav",
                        ignoreCase = true
                    )
        }
        ?.sortedByDescending {
            it.lastModified()
        }
        ?: emptyList()
}


// ============================================================
// STOP PLAYBACK
// ============================================================

private fun stopPlayback(
    mediaPlayer: MediaPlayer
) {

    try {

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }

    } catch (_: Exception) {
    }

    try {
        mediaPlayer.reset()
    } catch (_: Exception) {
    }
}


// ============================================================
// FILE SIZE
// ============================================================

private fun formatFileSize(
    bytes: Long
): String {

    return when {

        bytes < 1024 -> {
            "$bytes B"
        }

        bytes < 1024 * 1024 -> {
            "${bytes / 1024} KB"
        }

        else -> {
            String.format(
                "%.2f MB",
                bytes /
                        (1024.0 * 1024.0)
            )
        }
    }
}


// ============================================================
// FEATURE CARD
// ============================================================

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

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = icon,
            color = Berry,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.width(14.dp)
        )

        Column(
            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text = title,
                color = Slate,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = description,
                color = Muted,
                fontSize = 14.sp
            )
        }
    }
}
