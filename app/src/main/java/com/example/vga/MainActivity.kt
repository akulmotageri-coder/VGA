package com.example.vga

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vga.audioseparation.processing.CallProcessingWorker
import com.example.vga.audioseparation.storage.AudioStorage
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.vga.ui.theme.VGATheme

import com.example.vga.audioseparation.AudioSeparationEntry

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSharedAudio(intent)
        enableEdgeToEdge()
        setContent {
            VGATheme {
                AudioSeparationEntry(
                    onBack = { }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSharedAudio(intent)
    }

    private fun handleSharedAudio(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {

            val audioUri = intent.getParcelableExtra<android.net.Uri>(
                Intent.EXTRA_STREAM
            )

            if (audioUri != null) {

                val mimeType = contentResolver.getType(audioUri)

                println("Shared audio MIME type: $mimeType")
                println("Shared audio URI: $audioUri")

                val storage = AudioStorage(this)
                val savedFile = storage.saveSharedAudio(audioUri)

                if (savedFile != null) {

                    println("Audio saved: ${savedFile.absolutePath}")

                    // Pass the saved WAV to the background processor
                    val inputData = Data.Builder()
                        .putString(
                            "input_path",
                            savedFile.absolutePath
                        )
                        .build()

                    val processingRequest =
                        OneTimeWorkRequestBuilder<CallProcessingWorker>()
                            .setInputData(inputData)
                            .build()

                    WorkManager
                        .getInstance(applicationContext)
                        .enqueue(processingRequest)

                    println("Background processing queued")

                } else {
                    println("Failed to save shared audio")
                }
            }
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VGATheme {
        Greeting("Android")
    }
}