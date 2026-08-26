package com.example.vga.audioseparation.processing

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import kotlin.math.sqrt

class CallProcessingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val inputPath = inputData.getString("input_path")
            ?: return Result.failure()

        return try {

            // --------------------------------
            // Worker started
            // --------------------------------

            Log.d(
                "VGA_PROCESSING",
                "Worker started"
            )

            Log.d(
                "VGA_PROCESSING",
                "Input: $inputPath"
            )

            val file = File(inputPath)

            if (!file.exists()) {

                Log.e(
                    "VGA_PROCESSING",
                    "File does not exist"
                )

                return Result.failure()
            }

            // --------------------------------
            // Read original WAV
            // --------------------------------

            val audio =
                AudioPreprocessor.readWav(file)

            Log.d(
                "VGA_PROCESSING",
                "Original WAV: " +
                        "sampleRate=${audio.sampleRate}, " +
                        "channels=${audio.channels}, " +
                        "samples=${audio.samples.size}"
            )

            // --------------------------------
            // Resample to 16 kHz
            // --------------------------------

            val audio16k =
                AudioPreprocessor.resampleTo16k(audio)

            Log.d(
                "VGA_PROCESSING",
                "Resampled WAV: " +
                        "sampleRate=${audio16k.sampleRate}, " +
                        "channels=${audio16k.channels}, " +
                        "samples=${audio16k.samples.size}"
            )

            // --------------------------------
            // Normalize volume
            // --------------------------------

            val normalized =
                AudioPreprocessor.normalizeVolume(
                    audio16k.samples
                )

            Log.d(
                "VGA_PROCESSING",
                "Normalized samples=${normalized.size}"
            )

            // --------------------------------
            // WebRTC VAD + silence trimming
            // --------------------------------

            val trimmed =
                AudioPreprocessor.trimLongSilences(
                    normalized
                )

            Log.d(
                "VGA_PROCESSING",
                "After VAD: samples=${trimmed.size}"
            )

            // --------------------------------
            // Save VAD output for reference
            // --------------------------------

            val referenceFile =
                File(
                    applicationContext.filesDir,
                    "android_vad_reference.wav"
                )

            AudioPreprocessor.saveFloatWav(
                trimmed,
                16000,
                referenceFile
            )

            Log.d(
                "VGA_PROCESSING",
                "Reference WAV saved: " +
                        referenceFile.absolutePath
            )

            // --------------------------------
            // Mel spectrogram
            // --------------------------------

            Log.d(
                "VGA_PROCESSING",
                "Starting mel spectrogram..."
            )

            val mel =
                MelSpectrogram.compute(trimmed)

            Log.d(
                "VGA_PROCESSING",
                "Mel spectrogram completed: " +
                        "frames=${mel.size}"
            )

            // --------------------------------
            // Mel summary
            // --------------------------------

            MelSpectrogram.logSummary(
                mel,
                applicationContext
            )

            // --------------------------------
            // Split mel into 160 × 40 chunks
            // --------------------------------

            val chunks =
                MelChunker.chunk(mel)

            MelChunker.logSummary(chunks)

            // --------------------------------
            // Verify first chunk
            // --------------------------------

            if (chunks.isNotEmpty()) {

                MelChunker.logFirstChunk(
                    chunks[0]
                )
            }

            // --------------------------------
            // Generate speaker embeddings
            // --------------------------------

            val embeddings =
                ArrayList<FloatArray>()

            SpeakerEncoder(
                applicationContext
            ).use { encoder ->

                for (index in chunks.indices) {

                    val chunk =
                        chunks[index]

                    val currentEmbedding =
                        encoder.encode(chunk)

                    embeddings.add(
                        currentEmbedding
                    )

                    // Calculate embedding norm
                    var normSquared = 0.0

                    for (value in currentEmbedding) {

                        normSquared +=
                            value.toDouble() *
                                    value.toDouble()
                    }

                    val norm =
                        sqrt(normSquared)

                    Log.d(
                        "VGA_EMBEDDING",
                        "Chunk $index: " +
                                "embedding size=" +
                                currentEmbedding.size
                    )

                    Log.d(
                        "VGA_EMBEDDING",
                        "Chunk $index: " +
                                "norm=$norm"
                    )
                }
            }

            // --------------------------------
            // Embedding summary
            // --------------------------------

            Log.d(
                "VGA_EMBEDDING",
                "Total embeddings=${embeddings.size}"
            )
            // --------------------------------
// Create call-level embedding
// --------------------------------

            if (embeddings.isNotEmpty()) {

                val callEmbedding =
                    EmbeddingUtils.meanAndNormalize(
                        embeddings
                    )

                val callNorm =
                    EmbeddingUtils.calculateNorm(
                        callEmbedding
                    )

                Log.d(
                    "VGA_EMBEDDING",
                    "Call embedding size=${callEmbedding.size}"
                )

                Log.d(
                    "VGA_EMBEDDING",
                    "Call embedding norm=$callNorm"
                )

                Log.d(
                    "VGA_EMBEDDING",
                    "Call embedding first10=" +
                            callEmbedding
                                .take(10)
                                .joinToString(" ")
                )
            }

            if (embeddings.isNotEmpty()) {

                val firstEmbedding =
                    embeddings[0]

                Log.d(
                    "VGA_EMBEDDING",
                    "First embedding size=" +
                            firstEmbedding.size
                )

                Log.d(
                    "VGA_EMBEDDING",
                    "First10=" +
                            firstEmbedding
                                .take(10)
                                .joinToString(" ")
                )

                var normSquared = 0.0

                for (value in firstEmbedding) {

                    normSquared +=
                        value.toDouble() *
                                value.toDouble()
                }

                val norm =
                    sqrt(normSquared)

                Log.d(
                    "VGA_EMBEDDING",
                    "First embedding norm=$norm"
                )
            }

            // --------------------------------
            // Processing completed
            // --------------------------------

            Log.d(
                "VGA_PROCESSING",
                "Preprocessing completed successfully"
            )

            Result.success()

        } catch (e: Exception) {

            Log.e(
                "VGA_PROCESSING",
                "Processing failed",
                e
            )

            Result.failure()
        }
    }
}