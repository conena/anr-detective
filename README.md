# ANR Detective

ANR Detective is a powerful library for Kotlin and Java that helps developers identify and resolve potential Application Not Responding (ANR) issues during the development phase, as well as monitor production applications. The library builds on the functionality of [therlock][therlock link], a Java library for detecting blocked threads, while also being lifecycle-aware and optimized for Android development with useful default settings.

The documentation is clear and easy to understand, with helpful comments and annotations. ANR Detective follows semantic versioning principles and will be actively maintained and improved over time. The library is available in both Kotlin and Java versions, and can be used from API level 16.

Whether you're building a new app from scratch or maintaining an existing one, ANR Detective is an invaluable tool for ensuring a smooth and responsive user experience.

# Getting started
### The library is not yet available on Maven Central. This will happen in the next few days.

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

# FAQ
### How does it work?
The ThreadAccessor executes code on the monitored thread at the specified interval. The library checks if this code was executed. If the code is not executed for longer than the specified threshold, a BlockedThreadEvent is passed to the listener. The BlockedThreadEvents contains the stack traces of all threads that should be reported.

### Is there an additional load on the monitored thread?
All checks happen on background threads. On the monitored thread, a callback is submitted to an ExecutorService of the library at the specified interval only. The additional load on the monitored thread is negligible.

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