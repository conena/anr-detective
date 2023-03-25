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

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.conena.therlock.ActiveThreadProvider;
import com.conena.therlock.BlockedThreadEvent;
import com.conena.therlock.BlockedThreadListener;
import com.conena.therlock.BlockedThreadLogWriter;
import com.conena.therlock.DetectionExemption;
import com.conena.therlock.FilteredThreadProvider;
import com.conena.therlock.ThreadAccessor;
import com.conena.therlock.ThreadProvider;

/**
 * Build a {@link ANRDetector} instance.
 */
public class ANRDetectorBuilder {

    /**
     * Used to post {@link Runnable} instances on the monitored thread.
     */
    @Nullable
    private ThreadAccessor threadAccessor;

    /**
     * If a blocked thread is detected, the {@link ThreadProvider} is used to retrieve
     * the threads for which a stack trace should be saved in the {@link BlockedThreadEvent}.
     * The sort order of the returned threads will also be the order of the corresponding
     * stack traces in the {@link BlockedThreadEvent}.
     */
    @Nullable
    private ThreadProvider threadProvider;

    /**
     * To receive {@link BlockedThreadEvent} instances.
     * The listener is called on a separate background thread.
     */
    @Nullable
    private BlockedThreadListener listener;

    /**
     * Defines an exemption when a thread should not be considered as blocked.
     * Can be used e.g. to create an exemption for debuggers.
     * This does not stop monitoring, it only suppresses reporting.
     */
    @Nullable
    private DetectionExemption exemption = new DebuggerExemption();

    /**
     * The lifecycle to which the Detector should be bound.
     * The detection is automatically paused when an ON_STOP event occurs and automatically resumed when ON_START occurs.
     */
    @Nullable
    private Lifecycle lifecycle = ProcessLifecycleOwner.get().getLifecycle();

    /**
     * The minimum time in milliseconds a thread must be blocked for a {@link BlockedThreadEvent} to be triggered.
     */
    @Nullable
    private Long threshold;

    /**
     * The interval in milliseconds in which it is checked whether a thread is blocked.
     */
    @Nullable
    private Long inspectionInterval;

    /**
     * @param threadAccessor Used to post {@link Runnable} instances on the monitored thread.
     * @return A reference to this instance.
     */
    @NonNull
    public ANRDetectorBuilder setThreadAccessor(@NonNull ThreadAccessor threadAccessor) {
        this.threadAccessor = threadAccessor;
        return this;
    }

    /**
     * @param threadProvider If a blocked thread is detected, the {@link ThreadProvider} is used to retrieve
     *                       the threads for which a stack trace should be saved in the {@link BlockedThreadEvent}.
     *                       The sort order of the returned threads will also be the order of the corresponding
     *                       stack traces in the {@link BlockedThreadEvent}.
     * @return A reference to this instance.
     * @see ActiveThreadProvider
     * @see FilteredThreadProvider
     */
    @NonNull
    public ANRDetectorBuilder setThreadProvider(@NonNull ThreadProvider threadProvider) {
        this.threadProvider = threadProvider;
        return this;
    }

    /**
     * @param listener To receive {@link BlockedThreadEvent} instances.
     *                 The listener is called on a separate background thread.
     * @return A reference to this instance.
     */
    @NonNull
    public ANRDetectorBuilder setListener(@NonNull BlockedThreadListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * @param exemption Defines an exemption when a thread should not be considered as blocked.
     *                  Can be used e.g. to create an exemption for debuggers.
     *                  This does not stop monitoring, it only suppresses reporting.
     * @return A reference to this instance.
     */
    @NonNull
    public ANRDetectorBuilder setExemption(@NonNull DetectionExemption exemption) {
        this.exemption = exemption;
        return this;
    }

    /**
     * @param lifecycle The lifecycle to which the Detector should be bound.
     *                  The detection is automatically paused when an ON_STOP event occurs
     *                  and automatically resumed when ON_START occurs.
     * @return A reference to this instance.
     */
    @NonNull
    public ANRDetectorBuilder setLifecycle(@Nullable Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
        return this;
    }

    /**
     * @param threshold The minimum time in milliseconds a thread must be blocked for a {@link BlockedThreadEvent} to be triggered.
     * @return A reference to this instance.
     */
    @NonNull
    public ANRDetectorBuilder setThreshold(long threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * @param interval The interval in milliseconds in which it is checked whether a thread is blocked.
     * @return A reference to this instance.
     */
    @NonNull
    public ANRDetectorBuilder setInspectionInterval(long interval) {
        this.inspectionInterval = interval;
        return this;
    }

    /**
     * Build an {@link ANRDetectorBuilder} with the parameters supplied to the builder methods.
     * If {@link #setThreadAccessor(ThreadAccessor)} was not called, a {@link MainThreadAccessor} will be used.
     * If {@link #setThreadProvider(ThreadProvider)} was not called, a {@link MainThreadProvider} will be used.
     * If {@link #setListener(BlockedThreadListener)} was not called a {@link BlockedThreadLogWriter} will be used.
     * If {@link #setExemption(DetectionExemption)} was not called a {@link DebuggerExemption} will be used.
     * If {@link #setLifecycle(Lifecycle)} was not called the process lifecycle will be used.
     * If {@link #setThreshold(long)} was not called 1000 milliseconds will be used.
     * If {@link #setInspectionInterval(long)} was not called, one fifth of the threshold value,
     * but at least 100 ms and at most 500ms, is used..
     *
     * @return The created {@link ANRDetectorBuilder}.
     */
    @CheckResult
    @NonNull
    public ANRDetector build() {
        long threshold = this.threshold == null ? 1_000L : this.threshold;
        return new ANRDetector(
                threadAccessor == null ? new MainThreadAccessor() : threadAccessor,
                threadProvider == null ? new MainThreadProvider() : threadProvider,
                listener == null ? new BlockedThreadLogWriter() : listener,
                exemption,
                lifecycle,
                threshold,
                inspectionInterval == null ? Math.min(500L, Math.max(100L, threshold / 5L)) : inspectionInterval
        );
    }

}