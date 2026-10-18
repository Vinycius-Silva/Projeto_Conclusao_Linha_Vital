package com.linhavital.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.ui.demo.DemoExpiredActivity
import com.linhavital.app.utils.DemoAccess

class LinhaVitalApplication :
    Application(),
    Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()

        ApiClient.initialize(this)

        registerActivityLifecycleCallbacks(
            this
        )
    }

    override fun onActivityResumed(
        activity: Activity
    ) {

        if (!BuildConfig.DEMO_MODE) {
            return
        }

        if (
            activity is DemoExpiredActivity
        ) {
            return
        }

        if (
            DemoAccess.isAvailableNow()
        ) {
            return
        }

        val intent =
            Intent(
                activity,
                DemoExpiredActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        activity.startActivity(
            intent
        )
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) = Unit

    override fun onActivityStarted(
        activity: Activity
    ) = Unit

    override fun onActivityPaused(
        activity: Activity
    ) = Unit

    override fun onActivityStopped(
        activity: Activity
    ) = Unit

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) = Unit

    override fun onActivityDestroyed(
        activity: Activity
    ) = Unit
}
