# ANR Detective

ANR Detective is a powerful library for Kotlin and Java that helps developers identify and resolve potential Application Not Responding (ANR) issues during the development phase, as well as monitor production applications. The library builds on the functionality of [therlock][therlock link], a Java library for detecting blocked threads, while also being lifecycle-aware and optimized for Android development with useful default settings.

The documentation is clear and easy to understand, with helpful comments and annotations. ANR Detective follows semantic versioning principles and will be actively maintained and improved over time. The library is available in both Kotlin and Java versions, and can be used from API level 16.

Whether you're building a new app from scratch or maintaining an existing one, ANR Detective is an invaluable tool for ensuring a smooth and responsive user experience.

# Getting started

To start using ANR Detective in your project, add the dependency.

### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    // If you use java
    implementation 'com.conena.anrdetective:anrdetective:1.0.0'
    // If you use kotlin
    implementation 'com.conena.anrdetective:anrdetective-kotlin:1.0.0'
}
```
# Usage (Kotlin)

### ANRDetector example
```kotlin
val anrDetector = ANRDetector(
    // Through the ThreadAccessor the check code is executed in the monitored thread.
    threadAccessor = MainThreadAccessor,
    // Provides the threads for which the stack traces are collected in case the detector hits.
    threadProvider = MainThreadProvider,
    // Defines exemptions when no BlockedThreadEvent should be raised.
    // Do not issue BlockedThreadEvents when a debugger is connected.
    exemption = DebuggerExemption,
    // Defines a lifecycle after which the detection is automatically stopped as well as restarted.
    lifecycle = ProcessLifecycleOwner.get().lifecycle,
    // Defines how long the thread must be continuously blocked for a BlockedThreadEvent to be triggered.
    threshold = 1_000L,
    // Defines how often the thread should be pinged.
    inspectionInterval = 200L
) { _, event ->
    // Log the exception or print the stack trace.
    event.printStackTrace()
}.startDetection(delay = 1_000L)
// If you want to stop detection at some point.
anrDetector.stopDetection()
// You can start it again at any time.
anrDetector.startDetection()
```
Considering the default values, the creation of the above detector can be shortened as follows:
```kotlin
val anrDetector = ANRDetector().startDetection(delay = 1_000L)
```

### ThreadProvider examples
```kotlin
// Provides only the main thread
val mainThread: ThreadProvider = MainThreadProvider

// Provides all active threads
val activeThreads: ThreadProvider = ActiveThreadProvider()

// Provides all user threads
val activeUserThreads: ThreadProvider = FilteredThreadProvider(DaemonThreadFilter())

// Provides all threads not created by therlock (the underlying library)
val activeNonLibraryThreads: ThreadProvider = FilteredThreadProvider(LibraryThreadFilter())

// Provides all threads with priority >=5
val activeHghPriorityThreads: ThreadProvider = FilteredThreadProvider(PriorityThreadFilter(5))

// Provides all daemon threads without library threads
val activeNonLibraryDaemonThreads: ThreadProvider = FilteredThreadProvider(
    CombinedThreadFilter(Thread::isDaemon, LibraryThreadFilter())
)
```

# Usage (Java)

### ANRDetector example
```java
ANRDetector anrDetector = new ANRDetectorBuilder()
        // Through the ThreadAccessor the check code is executed in the monitored thread.
        .setThreadAccessor(new MainThreadAccessor())
        // Provides the threads for which the stack traces are collected in case the detector hits.
        .setThreadProvider(new MainThreadProvider())
        // Log the exception or print the stack trace.
        .setListener((detector, event) -> event.printStackTrace())
        // Defines exemptions when no BlockedThreadEvent should be raised.
        // Do not issue BlockedThreadEvents when a debugger is connected.
        .setExemption(new DebuggerExemption())
        // Defines a lifecycle after which the detection is automatically stopped as well as restarted.
        .setLifecycle(ProcessLifecycleOwner.get().getLifecycle())
        // Defines how long the thread must be continuously blocked for a BlockedThreadEvent to be triggered.
        .setThreshold(1_000L)
        // Defines how often the thread should be pinged.
        .setInspectionInterval(200L)
        // Build the detector.
        .build()
        // Start the detection.
        // The optional parameter is a delay from when the detection should start.
        .startDetection(1_000L);
// If you want to stop detection at some point.
anrDetector.stopDetection();
//You can start it again at any time.
anrDetector.startDetection();
```
Considering the default values, the creation of the above detector can be shortened as follows:
```java
ANRDetector anrDetector = new ANRDetectorBuilder()
        .build()
        .startDetection(1000L);
```

### ThreadProvider examples
```java
// Provides only the main thread
ThreadProvider mainThread = new MainThreadProvider();

// Provides all active threads
ThreadProvider activeThreads = new ActiveThreadProvider();
        
// Provides all user threads
ThreadProvider activeUserThreads = new FilteredThreadProvider(new DaemonThreadFilter());

// Provides all threads not created by therlock (the underlying library)
ThreadProvider activeNonLibraryThreads = new FilteredThreadProvider(new LibraryThreadFilter());
        
// Provides all threads with priority >=5
ThreadProvider activeHghPriorityThreads = new FilteredThreadProvider(new PriorityThreadFilter(5));
        
// Provides all daemon threads without library threads
ThreadProvider activeNonLibraryDaemonThreads = new FilteredThreadProvider(
        new CombinedThreadFilter(Thread::isDaemon, new LibraryThreadFilter())
);
```

### Interpretation of a BlockedThreadEvent
If the monitored thread is found to be blocked for as long as the specified threshold, a BlockedThreadEvent is created and passed to the listener.

BlockedThreadEvent inherits from Throwable, but is never thrown by this library. The main reason for this is that most logging and tracking solutions support logging of throwables and their stacktrace. Furthermore, the event can therefore be printed very easily and well-structured in the log.

The stack trace represents the stack trace of all threads returned by the ThreadProvider at the time of the BlockedThreadEvent. The order is also respected and the stacktrace of the thread with index 0 in the array will be the first in the stacktrace of the BlockedThreadEvent.

Below is an example where the "main" thread is monitored and the ThreadProvider has returned the three threads "main", "worker-1" and "worker-2". We see that the main thread is blocked by a call to Thread.sleep. The stacktraces of the other threads have nothing to do with the cause of the event in this case, but might be the cause in other cases. It is up to you to judge this, as the library cannot do this for you.
```
com.conena.therlock.BlockedThreadEvent: The monitored thread was blocked for at least 1000 milliseconds. The stack trace contains the stack traces of all threads selected for reporting. Please refer to the documentation when interpreting the stack traces.
Caused by: com.conena.therlock.ThreadInfo: Stacktrace of the thread 'main'.
	at java.lang.Thread.sleep(Native Method)
	at java.lang.Thread.sleep(Thread.java:450)
	at java.lang.Thread.sleep(Thread.java:355)
	at com.conena.sample.App.onCreate$lambda$1(App.kt:87)
	at com.conena.sample.App.$r8$lambda$RyyZsiiZRbAhnfOCj96-SMpRwi0(Unknown Source:0)
	at com.conena.sample.App$$ExternalSyntheticLambda1.run(Unknown Source:0)
	at android.os.Handler.handleCallback(Handler.java:942)
	at android.os.Handler.dispatchMessage(Handler.java:99)
	at android.os.Looper.loopOnce(Looper.java:201)
	at android.os.Looper.loop(Looper.java:288)
	at android.app.ActivityThread.main(ActivityThread.java:7898)
	at java.lang.reflect.Method.invoke(Native Method)
	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:936)
Caused by: com.conena.therlock.ThreadInfo: Stacktrace of the thread 'worker-1'.
	at android.os.MessageQueue.nativePollOnce(Native Method)
	at android.os.MessageQueue.next(MessageQueue.java:335)
	at android.os.Looper.loopOnce(Looper.java:161)
	at android.os.Looper.loop(Looper.java:288)
	at android.os.HandlerThread.run(HandlerThread.java:67)
Caused by: com.conena.therlock.ThreadInfo: Stacktrace of the thread 'worker-2'.
	at jdk.internal.misc.Unsafe.park(Native Method)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2081)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:433)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1063)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1123)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:637)
	at java.lang.Thread.run(Thread.java:1012)
```

# FAQ
### How does it work?
The ThreadAccessor executes code on the monitored thread at the specified interval. The library checks if this code was executed. If the code is not executed for longer than the specified threshold, a BlockedThreadEvent is passed to the listener. The BlockedThreadEvents contains the stack traces of all threads that should be reported.

### Is there an additional load on the monitored thread?
All checks happen on background threads. On the monitored thread, a callback is submitted to an ExecutorService of the library at the specified interval only. The additional load on the monitored thread is negligible.

### Can I integrate the library into my existing monitoring/logging solution?
Yes, as long as your solution supports the logging of Throwables. You can just log the BlockedThreadEvent in this case, since it inherits from Throwable. The stack trace contains the necessary information to investigate the event.

# Contribution

Please feel free to open an issue or submit a pull request if you have any suggestions for improvement. When submitting a pull request, please confirm that you wrote the code yourself, waive any copyright rights, and agree that the code will be placed under the original license of the library.

# License
```
Copyright (C) 2023 Fabian Andera

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
[therlock link]:https://github.com/conena/therlock