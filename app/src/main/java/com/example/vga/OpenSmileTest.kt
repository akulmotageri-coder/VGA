package com.example.vga

import android.content.Context
import android.util.Log
import com.audeering.opensmile.OpenSmileAdapter
import java.io.File
import java.io.IOException

class OpenSmileTest(private val context: Context) {

    private var smile: OpenSmileAdapter? = null

    /**
     * Recursively copies an entire directory from Android assets
     * into the application's cache directory.
     */
    private fun copyAssetTree(
        assetPath: String,
        destination: File
    ) {
        val files = context.assets.list(assetPath)

        if (files.isNullOrEmpty()) {
            destination.parentFile?.mkdirs()

            context.assets.open(assetPath).use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return
        }

        destination.mkdirs()

        for (file in files) {
            val childAssetPath =
                if (assetPath.isEmpty()) {
                    file
                } else {
                    "$assetPath/$file"
                }

            val childDestination = File(destination, file)

            copyAssetTree(
                childAssetPath,
                childDestination
            )
        }
    }

    /**
     * Copies the OpenSMILE configuration tree required by eGeMAPS.
     */
    private fun copyOpenSmileConfigs() {

        // gemaps/v01b
        copyAssetTree(
            "gemaps",
            File(context.cacheDir, "gemaps")
        )

        // egemaps/v02
        copyAssetTree(
            "egemaps",
            File(context.cacheDir, "egemaps")
        )

        // shared configuration files
        copyAssetTree(
            "shared",
            File(context.cacheDir, "shared")
        )

        // Main test configuration
        val mainConfig = File(
            context.cacheDir,
            "egemaps_test.conf"
        )

        context.assets.open("egemaps_test.conf").use { input ->
            mainConfig.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Initializes OpenSMILE using the eGeMAPS configuration.
     */
    fun initialize(): Boolean {
        return try {

            Log.d(
                "OpenSMILE_TEST",
                "Starting OpenSMILE initialization"
            )

            // Copy all required configuration files
            copyOpenSmileConfigs()

            val configFile = File(
                context.cacheDir,
                "egemaps_test.conf"
            )

            Log.d(
                "OpenSMILE_TEST",
                "Config path = ${configFile.absolutePath}"
            )

            Log.d(
                "OpenSMILE_TEST",
                "Config exists = ${configFile.exists()}"
            )

            // Check important dependencies
            val gemapsLld = File(
                context.cacheDir,
                "gemaps/v01b/GeMAPSv01b_core.lld.conf.inc"
            )

            val gemapsFunc = File(
                context.cacheDir,
                "gemaps/v01b/GeMAPSv01b_core.func.conf.inc"
            )

            val egemapsLld = File(
                context.cacheDir,
                "egemaps/v02/eGeMAPSv02_core.lld.conf.inc"
            )

            val egemapsFunc = File(
                context.cacheDir,
                "egemaps/v02/eGeMAPSv02_core.func.conf.inc"
            )

            Log.d(
                "OpenSMILE_TEST",
                "GeMAPS LLD exists = ${gemapsLld.exists()}"
            )

            Log.d(
                "OpenSMILE_TEST",
                "GeMAPS FUNC exists = ${gemapsFunc.exists()}"
            )

            Log.d(
                "OpenSMILE_TEST",
                "eGeMAPS LLD exists = ${egemapsLld.exists()}"
            )

            Log.d(
                "OpenSMILE_TEST",
                "eGeMAPS FUNC exists = ${egemapsFunc.exists()}"
            )

            // Create OpenSMILE instance
            smile = OpenSmileAdapter()

            /**
             * IMPORTANT:
             *
             * OpenSmileAdapter.smile_initialize() expects:
             *
             * configFile
             * options
             * loglevel
             * debug
             * consoleOutput
             *
             * The existing working test used:
             *
             * 16000, 1, 16
             *
             * but these are actually OpenSMILE logging/debug parameters,
             * not sample rate/channel/bit depth.
             */
            val result = smile!!.smile_initialize(
                configFile.absolutePath,
                HashMap(),
                3,   // loglevel
                1,   // debug
                1    // console output
            )

            Log.d(
                "OpenSMILE_TEST",
                "Initialize result = $result"
            )

            if (result.toString() == "SMILE_SUCCESS") {

                Log.d(
                    "OpenSMILE_TEST",
                    "OpenSMILE initialized successfully"
                )

                true

            } else {

                Log.e(
                    "OpenSMILE_TEST",
                    "OpenSMILE initialization failed: $result"
                )

                false
            }

        } catch (e: Exception) {

            Log.e(
                "OpenSMILE_TEST",
                "Initialization exception",
                e
            )

            false
        }
    }

    /**
     * Sends test PCM audio to the externalAudioSource component.
     *
     * The test configuration expects:
     * 16 kHz
     * mono
     * 16-bit PCM
     */
    fun sendTestAudio(): Boolean {
        return try {

            if (smile == null) {
                Log.e(
                    "OpenSMILE_TEST",
                    "Cannot send audio: OpenSMILE is not initialized"
                )

                return false
            }

            /**
             * 3200 bytes of 16-bit mono PCM at 16 kHz:
             *
             * 3200 / 2 = 1600 samples
             * 1600 / 16000 = 0.1 seconds
             */
            val audio = ByteArray(3200)

            val result =
                smile!!.smile_extaudiosource_write_data(
                    "externalAudioSource",
                    audio
                )

            Log.d(
                "OpenSMILE_TEST",
                "Audio write result = $result"
            )

            result.toString() == "SMILE_SUCCESS"

        } catch (e: IOException) {

            Log.e(
                "OpenSMILE_TEST",
                "Audio write failed",
                e
            )

            false

        } catch (e: Exception) {

            Log.e(
                "OpenSMILE_TEST",
                "Audio write exception",
                e
            )

            false
        }
    }

    /**
     * Signals end-of-input and releases OpenSMILE.
     */
    fun close() {
        try {

            smile?.smile_extaudiosource_set_external_eoi(
                "externalAudioSource"
            )

            smile?.smile_free()

            smile = null

            Log.d(
                "OpenSMILE_TEST",
                "OpenSMILE closed"
            )

        } catch (e: Exception) {

            Log.e(
                "OpenSMILE_TEST",
                "Close failed",
                e
            )
        }
    }
}
