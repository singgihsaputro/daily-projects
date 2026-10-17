package com.dailyprojects.breadcrumbs.sample

import android.app.Application
import com.dailyprojects.breadcrumbs.Breadcrumbs

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Breadcrumbs.install(this, capacity = 50)
        Breadcrumbs.log("app", "cold start")
    }
}
