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

package com.conena.anrdetective;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.conena.therlock.BlockedThreadDetector;
import com.conena.therlock.BlockedThreadEvent;
import com.conena.therlock.BlockedThreadListener;
import com.conena.therlock.DetectionExemption;
import com.conena.therlock.ThreadAccessor;
import com.conena.therlock.ThreadProvider;

/**
 * {@link Lifecycle} aware implementation of {@link BlockedThreadDetector}.
 */
public class ANRDetector extends BlockedThreadDetector implements DefaultLifecycleObserver {

    /**
     * Handler associated with the MainLooper.
     * Needed because the lifecycle observer must be added and removed on the main thread.
     */
    @NonNull
    private final static Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * The lifecycle to which the Detector is bound.
     */
    @Nullable
    private final Lifecycle lifecycle;

    /**
     * @param threadAccessor     Used to post {@link Runnable} instances on the monitored thread.
     * @param threadProvider     If a blocked thread is detected, the {@link ThreadProvider} is used to retrieve
     *                           the threads for which a stack trace should be saved in the {@link BlockedThreadEvent}.
     *                           The sort order of the returned threads will also be the order of the corresponding
     *                           stack traces in the {@link BlockedThreadEvent}.
     * @param listener           To receive {@link BlockedThreadEvent} instances.
     *                           The listener is called on a separate background thread.
     * @param exemption          Defines an exemption when a thread should not be considered as blocked.
     *                           Can be used e.g. to create an exemption for debuggers.
     *                           This does not stop monitoring, it only suppresses reporting.
     * @param lifecycle          The lifecycle to which the Detector should be bound.
     *                           The detection is automatically paused when an ON_STOP event occurs and automatically resumed when ON_START occurs.
     * @param threshold          The minimum time a thread must be blocked for a {@link BlockedThreadEvent}
     *                           to be triggered.
     * @param inspectionInterval The interval in milliseconds in which it is checked whether
     *                           a thread is blocked. Together with the threshold this value decides if and how soon
     *                           blocked threads are detected.
     */
    public ANRDetector(
            @NonNull ThreadAccessor threadAccessor,
            @NonNull ThreadProvider threadProvider,
            @NonNull BlockedThreadListener listener,
            @Nullable DetectionExemption exemption,
            @Nullable Lifecycle lifecycle,
            long threshold,
            long inspectionInterval
    ) {
        super(threadAccessor, threadProvider, listener, exemption, threshold, inspectionInterval);
        this.lifecycle = lifecycle;
    }

    @Override
    public synchronized ANRDetector startDetection() {
        return startDetection(0L);
    }

    @Override
    public synchronized ANRDetector startDetection(long delay) {
        super.startDetection(delay);
        if (lifecycle != null) {
            mainHandler.post(() -> lifecycle.addObserver(this));
        }
        return this;
    }

    @Override
    public synchronized void stopDetection() {
        super.stopDetection();
        if (lifecycle != null) {
            mainHandler.post(() -> lifecycle.removeObserver(this));
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        super.startDetection(0L);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        super.stopDetection();
    }

}