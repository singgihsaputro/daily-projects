package com.dailyprojects.grocerylist

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.NSUTF8StringEncoding

/** Reads the fixture bundled as an iOS app resource named `groceries.json`. Not compiled in this environment (no Xcode). */
class IosGroceryJsonSource : GroceryJsonSource {
    override fun readJson(): String {
        val path = NSBundle.mainBundle.pathForResource("groceries", ofType = "json")
            ?: error("groceries.json not found in app bundle")
        return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null) as String
    }
}
