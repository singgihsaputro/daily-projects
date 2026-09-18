package com.dailyprojects.unitconverter

import android.content.Context

/** Reads the fixture bundled as an Android asset at `assets/mock/units.json`. */
class AndroidUnitJsonSource(private val context: Context) : UnitJsonSource {
    override fun readJson(): String =
        context.assets.open("mock/units.json").bufferedReader().use { it.readText() }
}
