package com.indisparte.pothole.di.component

import android.app.Application
import android.content.Context
import dagger.Provides
import dagger.hilt.android.HiltAndroidApp

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
@HiltAndroidApp
class PotholeApplication : Application() {
    // No need to override onCreate()

    // Declare a @Provides method for the Context dependency
    @Provides
    fun provideContext(): Context = applicationContext
}

