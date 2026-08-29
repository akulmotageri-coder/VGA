package com.example.vga

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var openSmileTest: OpenSmileTest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openSmileTest = OpenSmileTest(this)

        Log.d(
            "OpenSMILE_TEST",
            "Starting OpenSMILE test"
        )

        val initialized = openSmileTest.initialize()

        Log.d(
            "OpenSMILE_TEST",
            "Initialization result = $initialized"
        )

        if (initialized) {

            /*
             * IMPORTANT:
             * Start the OpenSMILE processing thread BEFORE
             * sending audio into externalAudioSource.
             */
            val started = openSmileTest.start()

            Log.d(
                "OpenSMILE_TEST",
                "Start result = $started"
            )

            if (started) {

                /*
                 * Give OpenSMILE a moment to enter its run loop.
                 */
                Thread.sleep(200)

                /*
                 * Send enough audio for the eGeMAPSv02
                 * functionals pipeline to actually process data.
                 */
                val audioSent = openSmileTest.sendSilence(5)

                Log.d(
                    "OpenSMILE_TEST",
                    "Audio sent result = $audioSent"
                )

            } else {

                Log.e(
                    "OpenSMILE_TEST",
                    "Could not start OpenSMILE"
                )
            }

        } else {

            Log.e(
                "OpenSMILE_TEST",
                "OpenSMILE initialization failed"
            )
        }
    }

    override fun onDestroy() {

        try {
            openSmileTest.close()
        } catch (e: Exception) {
            Log.e(
                "OpenSMILE_TEST",
                "Error closing OpenSMILE",
                e
            )
        }

        super.onDestroy()
    }
}
