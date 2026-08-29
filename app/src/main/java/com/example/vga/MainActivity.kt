
package com.example.vga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vga.audioseparation.AudioSeparationEntry
import com.example.vga.ui.theme.VGATheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            VGATheme {
                AudioSeparationEntry(
                    onBack = { }
                )
            }
        }
        val openSmileTest = OpenSmileTest(this)

        val success = openSmileTest.initialize()
        if (success) {
            openSmileTest.sendTestAudio()
        }

        android.util.Log.d(
            "OpenSMILE_TEST",
            "Initialization result = $success"
        )
    }
}

