package com.example.vga.audioseparation.processing

import com.example.vga.dementia.acoustic.AcousticAnalysisManager
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

        val inputPath =
            inputData.getString("input_path")
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

            val file =
                File(inputPath)

            if (!file.exists()) {

                Log.e(
                    "VGA_PROCESSING",
                    "File does not exist"
                )

                return Result.failure()
            }

            // --------------------------------
            // Load saved reference embedding
            // --------------------------------

            val referenceEmbedding =
                ReferenceVoiceManager.loadEmbedding(
                    applicationContext
                )

            if (referenceEmbedding == null) {

                Log.e(
                    "VGA_MATCH",
                    "No reference voice embedding found"
                )

                return Result.failure()
            }

            Log.d(
                "VGA_MATCH",
                "Reference embedding loaded"
            )

            Log.d(
                "VGA_MATCH",
                "Reference embedding size=" +
                        referenceEmbedding.size
            )

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
                AudioPreprocessor.resampleTo16k(
                    audio
                )

            Log.d(
                "VGA_PROCESSING",
                "Resampled WAV: " +
                        "sampleRate=${audio16k.sampleRate}, " +
                        "channels=${audio16k.channels}, " +
                        "samples=${audio16k.samples.size}"
            )

            // --------------------------------
            // Keep ORIGINAL audio timeline
            //
            // IMPORTANT:
            // Do NOT use trimLongSilences() here.
            //
            // The extracted voice must have the
            // same duration and timestamps as
            // the original call recording.
            // --------------------------------

            val originalSamples =
                audio16k.samples.copyOf()

            // --------------------------------
            // Normalize for speaker matching
            // --------------------------------

            val normalized =
                AudioPreprocessor.normalizeVolume(
                    originalSamples
                )

            Log.d(
                "VGA_PROCESSING",
                "Normalized samples=${normalized.size}"
            )

            Log.d(
                "VGA_PROCESSING",
                "Original duration=" +
                        "${normalized.size / 16000.0}s"
            )

            // --------------------------------
            // IMPORTANT
            //
            // We previously did:
            //
            // normalized -> VAD trim -> mel
            //
            // That changed the timeline.
            //
            // Now:
            //
            // normalized -> mel
            //
            // This keeps the original timestamps.
            // --------------------------------

            val processingSamples =
                normalized

            // --------------------------------
            // Mel spectrogram
            // --------------------------------

            Log.d(
                "VGA_PROCESSING",
                "Starting mel spectrogram..."
            )

            val mel =
                MelSpectrogram.compute(
                    processingSamples
                )

            Log.d(
                "VGA_PROCESSING",
                "Mel spectrogram completed: " +
                        "frames=${mel.size}"
            )

            MelSpectrogram.logSummary(
                mel,
                applicationContext
            )

            // --------------------------------
            // Create 160 × 40 chunks
            // --------------------------------

            val chunks =
                MelChunker.chunk(
                    mel
                )

            MelChunker.logSummary(
                chunks
            )

            if (chunks.isEmpty()) {

                Log.e(
                    "VGA_PROCESSING",
                    "No valid mel chunks"
                )

                return Result.failure()
            }

            MelChunker.logFirstChunk(
                chunks[0]
            )

            // --------------------------------
            // Generate speaker embeddings
            // --------------------------------

            val embeddings =
                ArrayList<FloatArray>()

            val confidences =
                ArrayList<Float>()

            SpeakerEncoder(
                applicationContext
            ).use { encoder ->

                for (index in chunks.indices) {

                    val chunk =
                        chunks[index]

                    // --------------------------------
                    // Encode chunk
                    // --------------------------------

                    val currentEmbedding =
                        encoder.encode(
                            chunk
                        )

                    embeddings.add(
                        currentEmbedding
                    )

                    // --------------------------------
                    // Calculate embedding norm
                    // --------------------------------

                    var normSquared =
                        0.0

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
                        "Chunk $index: norm=$norm"
                    )

                    // --------------------------------
                    // Compare with reference voice
                    // --------------------------------

                    val similarity =
                        VoiceMatcher.cosineSimilarity(
                            referenceEmbedding,
                            currentEmbedding
                        )

                    val confidence =
                        VoiceMatcher.similarityToConfidence(
                            similarity
                        )

                    confidences.add(
                        confidence
                    )

                    Log.d(
                        "VGA_MATCH",
                        "Chunk $index: " +
                                "similarity=$similarity, " +
                                "confidence=$confidence"
                    )
                }
            }

            // --------------------------------
            // Confidence summary
            // --------------------------------

            Log.d(
                "VGA_MATCH",
                "Confidence values=${confidences.size}"
            )

            // --------------------------------
            // Create sample-level mask
            //
            // IMPORTANT:
            // Mask is now based on the ORIGINAL
            // audio length.
            // --------------------------------

            val mask =
                AudioMaskProcessor.createMask(
                    processingSamples.size,
                    confidences
                )

            Log.d(
                "VGA_MASK",
                "Mask created: samples=${mask.size}"
            )

            // --------------------------------
            // Smooth mask
            // --------------------------------

            val smoothedMask =
                AudioMaskProcessor.smoothMask(
                    mask,
                    200
                )

            Log.d(
                "VGA_MASK",
                "Mask smoothing completed"
            )

            // --------------------------------
            // Fade protection
            // --------------------------------

            val finalMask =
                AudioMaskProcessor.applyFadeProtection(
                    smoothedMask,
                    150
                )

            Log.d(
                "VGA_MASK",
                "Fade protection completed"
            )

            // --------------------------------
            // Apply voice mask
            // --------------------------------

            val separated =
                AudioMaskProcessor.applyMask(
                    processingSamples,
                    finalMask
                )

            // --------------------------------
            // Normalize separated audio
            // --------------------------------

            val normalizedOutput =
                AudioMaskProcessor.normalizeOutput(
                    separated
                )

            // --------------------------------
            // Save into extracted_voice
            // --------------------------------

            val extractedVoiceDirectory =
                File(
                    applicationContext.filesDir,
                    "extracted_voice"
                )

            if (!extractedVoiceDirectory.exists()) {

                val created =
                    extractedVoiceDirectory.mkdirs()

                Log.d(
                    "VGA_OUTPUT",
                    "Created extracted_voice directory=$created"
                )
            }

            // --------------------------------
            // Unique output filename
            // --------------------------------

            val timestamp =
                System.currentTimeMillis()

            val outputFile =
                File(
                    extractedVoiceDirectory,
                    "my_voice_only_$timestamp.wav"
                )

            // --------------------------------
            // Save WAV
            // --------------------------------

            AudioPreprocessor.saveFloatWav(
                normalizedOutput,
                16000,
                outputFile
            )

            Log.d(
                "VGA_OUTPUT",
                "Voice-only audio saved: " +
                        outputFile.absolutePath
            )
            // --------------------------------
// Acoustic feature extraction
// --------------------------------

            Log.d(
                "VGA_ACOUSTIC",
                "Starting eGeMAPSv02 acoustic analysis"
            )

            val acousticAnalysisManager =
                AcousticAnalysisManager(
                    applicationContext
                )

            val acousticFeatures =
                acousticAnalysisManager.analyze(
                    outputFile
                )

            if (acousticFeatures == null) {

                Log.e(
                    "VGA_ACOUSTIC",
                    "Acoustic feature extraction failed"
                )

            } else {

                Log.d(
                    "VGA_ACOUSTIC",
                    "Acoustic feature extraction successful"
                )

                Log.d(
                    "VGA_ACOUSTIC",
                    "Feature count=${acousticFeatures.size}"
                )

                Log.d(
                    "VGA_ACOUSTIC",
                    "Features=${acousticFeatures.values.contentToString()}"
                )
            }

            Log.d(
                "VGA_OUTPUT",
                "Output samples=" +
                        normalizedOutput.size
            )

            Log.d(
                "VGA_OUTPUT",
                "Output duration=" +
                        "${normalizedOutput.size / 16000.0}s"
            )

            // --------------------------------
            // Call-level embedding
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
                    "Call embedding size=" +
                            callEmbedding.size
                )

                Log.d(
                    "VGA_EMBEDDING",
                    "Call embedding norm=" +
                            callNorm
                )

                val callSimilarity =
                    VoiceMatcher.cosineSimilarity(
                        referenceEmbedding,
                        callEmbedding
                    )

                Log.d(
                    "VGA_MATCH",
                    "Call-level similarity=" +
                            callSimilarity
                )
            }

            // --------------------------------
            // Processing completed
            // --------------------------------

            Log.d(
                "VGA_PROCESSING",
                "Voice separation completed successfully"
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