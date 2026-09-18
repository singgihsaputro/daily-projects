package com.dailyprojects.unitconverter

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.NSUTF8StringEncoding

/** Reads the fixture bundled as an iOS app resource named `units.json`. Not compiled in this environment (no Xcode). */
class IosUnitJsonSource : UnitJsonSource {
    override fun readJson(): String {
        val path = NSBundle.mainBundle.pathForResource("units", ofType = "json")
            ?: error("units.json not found in app bundle")
        return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null) as String
    }
}
