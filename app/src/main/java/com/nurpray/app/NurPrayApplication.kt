package com.nurpray.app

import android.app.Application
import com.nurpray.app.core.notifications.NotificationHelper

class NurPrayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Notification Channels
        NotificationHelper(this)
    }
}
