package com.esheeq.app

import android.app.Application
import com.esheeq.app.data.api.ApiClient
import com.esheeq.app.data.repository.AppUpdateManager
import com.esheeq.app.data.repository.PlaybackHistoryManager
import com.esheeq.app.data.repository.SearchHistoryManager

class EsheeqApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        PlaybackHistoryManager.init(this)
        SearchHistoryManager.init(this)
        AppUpdateManager.init(this)
    }
}
