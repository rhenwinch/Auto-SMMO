package com.xcape.simplemmomod

import android.app.Application
import android.content.Context
import androidx.datastore.dataStore
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.xcape.simplemmomod.domain.AppPreferencesSerializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

val Context.dataStore by dataStore("app-preferences.json", AppPreferencesSerializer)

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}