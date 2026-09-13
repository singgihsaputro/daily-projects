package com.dailyprojects.readingtracker

import android.os.Build

actual fun currentPlatformLabel(): String = "Android ${Build.VERSION.SDK_INT}"
