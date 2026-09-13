package com.dailyprojects.readingtracker

/** Each platform reports its own name and version; the shared UI just displays whatever comes back. */
expect fun currentPlatformLabel(): String
