package com.conena.anrdetective.kotlin

import android.os.Debug
import com.conena.therlock.BlockedThreadEvent
import com.conena.therlock.DetectionExemption

/**
 * A [DetectionExemption] that prevents [BlockedThreadEvent]s when a debugger is connected.
 */
object DebuggerExemption : DetectionExemption {

    override fun isExemptionActive(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

}