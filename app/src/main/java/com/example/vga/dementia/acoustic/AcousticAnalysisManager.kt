package com.example.vga.dementia.acoustic

import android.content.Context
import android.util.Log
import com.example.vga.audioseparation.processing.AudioDecoder
import java.io.File

class AcousticAnalysisManager(
    private val context: Context
) {

    companion object {
        private const val TAG = "ACOUSTIC_ANALYSIS"
    }

    fun analyze(
        voiceFile: File
    ): AcousticFeatures? {

        Log.d(
            TAG,
            "Starting acoustic analysis"
        )

        Log.d(
            TAG,
            "Input file = ${voiceFile.absolutePath}"
        )

        if (!voiceFile.exists()) {
            Log.e(
                TAG,
                "Voice file does not exist"
            )
            return null
        }

        return try {

            // -----------------------------------------
            // Decode extracted voice
            // -----------------------------------------

            val audio =
                AudioDecoder.decodeToMonoFloat(
                    voiceFile
                )

            Log.d(
                TAG,
                "Decoded audio: " +
                        "samples=${audio.samples.size}, " +
                        "sampleRate=${audio.sampleRate}, " +
                        "channels=${audio.channels}"
            )

            // -----------------------------------------
            // Extract eGeMAPSv02 features
            // -----------------------------------------

            val values =
                OpenSmileFeatureExtractor(
                    context
                ).extract(audio)

            if (values == null) {

                Log.e(
                    TAG,
                    "Acoustic feature extraction failed"
                )

                return null
            }

            Log.d(
                TAG,
                "Acoustic features extracted: " +
                        "${values.size}"
            )

            // -----------------------------------------
            // Wrap in AcousticFeatures
            // -----------------------------------------

            AcousticFeatures(
                values = values
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Acoustic analysis failed",
                e
            )

            null
        }
    }

    fun analyzeLatest(): AcousticFeatures? {

        val directory =
            File(
                context.filesDir,
                "extracted_voice"
            )

        if (!directory.exists()) {
            Log.e(
                TAG,
                "extracted_voice directory does not exist"
            )
            return null
        }

        val latestFile =
            directory
                .listFiles()
                ?.filter {
                    it.isFile &&
                            it.extension.equals(
                                "wav",
                                ignoreCase = true
                            )
                }
                ?.maxByOrNull {
                    it.lastModified()
                }

        if (latestFile == null) {

            Log.e(
                TAG,
                "No extracted voice WAV files found"
            )

            return null
        }

        Log.d(
            TAG,
            "Latest extracted voice = " +
                    latestFile.absolutePath
        )

        return analyze(latestFile)
    }
}