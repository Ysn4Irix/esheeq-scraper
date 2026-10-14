package com.esheeq.app

import android.app.Application
import com.esheeq.app.data.api.ApiClient
import com.esheeq.app.data.repository.PlaybackHistoryManager

class EsheeqApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        PlaybackHistoryManager.init(this)
    }
}
