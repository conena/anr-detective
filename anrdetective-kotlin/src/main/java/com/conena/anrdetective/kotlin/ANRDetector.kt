/*
 * Copyright (C) 2023 Fabian Andera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.conena.anrdetective.kotlin

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.conena.therlock.BlockedThreadDetector
import com.conena.therlock.BlockedThreadEvent
import com.conena.therlock.BlockedThreadListener
import com.conena.therlock.BlockedThreadLogWriter
import com.conena.therlock.DetectionExemption
import com.conena.therlock.ThreadAccessor
import com.conena.therlock.ThreadProvider

/**
 * [Lifecycle] aware implementation of [BlockedThreadDetector].
 * @param threadAccessor Used to post [Runnable] instances on the monitored thread.
 * By default this is a [MainThreadAccessor] which posts to the Android main thread.
 * @param threadProvider If a blocked thread is detected, the [ThreadProvider] is used to retrieve
 * the threads for which a stack trace should be saved in the [BlockedThreadEvent].
 * The sort order of the returned threads will also be the order of the corresponding
 * stack traces in the [BlockedThreadEvent].
 * By default this is a [MainThreadProvider], which returns only the Android main thread as thread for reporting.
 * @param exemption Defines an exemption when a thread should not be considered as blocked.
 * This does not stop monitoring, it only suppresses reporting.
 * By default this is a [DebuggerExemption] which suspends reporting when a debugger is connected.
 * @param lifecycle The lifecycle to which the Detector should be bound.
 * The detection is automatically paused when an ON_STOP event occurs and automatically resumed when ON_START occurs.
 * By default this is the lifecycle of the app process.
 * @param threshold The minimum time in milliseconds a thread must be blocked for a [BlockedThreadEvent]
 * to be triggered. Per default this is 1000 milliseconds.
 * @param inspectionInterval The interval in milliseconds in which it is checked whether
 * a thread is blocked. Together with the threshold this value decides if and how soon
 * blocked threads are detected.
 * Per default one fifth of the threshold value, but at least 100 ms and at most 500ms.
 * @param listener To receive [BlockedThreadEvent] instances.
 * The listener is called on a separate background thread.
 * By default this is a [BlockedThreadLogWriter] which prints the [BlockedThreadEvent] to
 * the default error stream.
 */
class ANRDetector constructor(
    threadAccessor: ThreadAccessor = MainThreadAccessor,
    threadProvider: ThreadProvider = MainThreadProvider,
    exemption: DetectionExemption? = DebuggerExemption,
    private val lifecycle: Lifecycle? = ProcessLifecycleOwner.get().lifecycle,
    threshold: Long = 1_000L,
    inspectionInterval: Long = (threshold / 5L).coerceIn(100L..500L),
    listener: BlockedThreadListener = BlockedThreadLogWriter()
) : BlockedThreadDetector(
    threadAccessor,
    threadProvider,
    listener,
    exemption,
    threshold,
    inspectionInterval
), DefaultLifecycleObserver {

    /**
     * Handler associated with the MainLooper.
     * Needed because the lifecycle observer must be added and removed on the main thread.
     */
    private val mainHandler = Handler(Looper.getMainLooper())

    @Synchronized
    override fun startDetection(): ANRDetector {
        return startDetection(delay = 0L)
    }

    @Synchronized
    override fun startDetection(delay: Long): ANRDetector {
        super.startDetection(delay)
        mainHandler.post {
            lifecycle?.addObserver(this)
        }
        return this
    }

    @Synchronized
    override fun stopDetection() {
        super.stopDetection()
        mainHandler.post {
            lifecycle?.removeObserver(this)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.startDetection(0L)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.stopDetection()
    }

}