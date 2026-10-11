package com.esheeq.app

import android.app.Application
import com.esheeq.app.data.api.ApiClient

class EsheeqApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}
