package com.dailyprojects.readingtracker

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.NSUTF8StringEncoding

/** Reads the fixture bundled as an iOS app resource named `books.json`. Not compiled in this environment (no Xcode). */
class IosBookJsonSource : BookJsonSource {
    override fun readJson(): String {
        val path = NSBundle.mainBundle.pathForResource("books", ofType = "json")
            ?: error("books.json not found in app bundle")
        return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null) as String
    }
}
