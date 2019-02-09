package com.nistix.stepcounter.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.nistix.stepcounter.BuildConfig
import com.nistix.stepcounter.db.StepsDatabase
import com.nistix.stepcounter.repository.SensorDirectory
import timber.log.Timber

class App : Application() {

  init {
    context = this
  }

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    db = StepsDatabase.build(context, SensorDirectory.sensors)
    prefs = getSharedPreferences("$packageName.prefs", Context.MODE_PRIVATE)
  }

  companion object {
    lateinit var context: App
      private set

    lateinit var db: StepsDatabase
      private set

    lateinit var prefs: SharedPreferences
      private set
  }
}
