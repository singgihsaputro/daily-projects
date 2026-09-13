package com.dailyprojects.readingtracker

import platform.UIKit.UIDevice

actual fun currentPlatformLabel(): String {
    val device = UIDevice.currentDevice
    return "${device.systemName()} ${device.systemVersion}"
}
