package com.linhavital.app

import android.app.Application
import com.linhavital.app.data.api.ApiClient

class LinhaVitalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ApiClient.initialize(this)
    }
}