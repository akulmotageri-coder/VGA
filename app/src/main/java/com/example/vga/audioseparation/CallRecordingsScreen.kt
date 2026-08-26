package com.example.vga.audioseparation

import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.vga.audioseparation.audio.AudioPlayer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalContext
import com.example.vga.audioseparation.repository.CallRecordingRepository
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private val WarmBackground = Color(0xFFF9F8F6)
private val Slate = Color(0xFF2D3142)

@Composable
fun CallRecordingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = CallRecordingRepository(context)
    val recordings = repository.getRecordings()
    val audioPlayer = remember { AudioPlayer() }
    var playingFilePath by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(20.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Row(
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
                text = "Call Recordings",
                color = Slate,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (recordings.isEmpty()) {

            Text(
                text = "No call recordings yet.",
                color = Color(0xFF6C727F),
                fontSize = 15.sp
            )


        } else {

            recordings.forEach { recording ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = recording.displayName,
                            color = Slate,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "WAV recording",
                            color = Color(0xFF6C727F),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (playingFilePath == recording.file.absolutePath) {
                                "Stop"
                            } else {
                                "Play"
                            },
                            color = Color(0xFF9E2A4B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                if (playingFilePath == recording.file.absolutePath) {
                                    audioPlayer.stop()
                                    playingFilePath = null
                                } else {
                                    audioPlayer.play(recording.file.absolutePath)
                                    playingFilePath = recording.file.absolutePath
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}