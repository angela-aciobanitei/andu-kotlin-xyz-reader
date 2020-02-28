package com.ang.acb.materialme

import android.app.Activity
import android.app.Application
import com.ang.acb.materialme.di.DaggerAppComponent
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import timber.log.Timber
import javax.inject.Inject

/**
 * When using Dagger for injecting Activities, you need to make your
 * Application implement HasAndroidInjector . See: https://dagger.dev/android.html.
 */
class ArticlesApplication: Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector() = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        DaggerAppComponent
            .builder()
            .application(this)
            .build()
            .inject(this)
    }
}