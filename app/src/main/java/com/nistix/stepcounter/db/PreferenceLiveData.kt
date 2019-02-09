package com.nistix.stepcounter.db

import android.arch.lifecycle.LiveData
import android.content.SharedPreferences

open class PreferenceLiveData<T>(
  private val preferences: SharedPreferences,
  private val key: String,
  private val block: SharedPreferences.() -> T?
) : LiveData<T>() {

  private val listener =
    SharedPreferences.OnSharedPreferenceChangeListener { preferences: SharedPreferences, key: String ->
      this.key.takeIf { it == key }?.let {
        value = block.invoke(preferences)
      }
    }

  override fun onActive() {
    super.onActive()
    value = block.invoke(preferences)
    preferences.registerOnSharedPreferenceChangeListener(listener)
  }

  override fun onInactive() {
    super.onInactive()
    preferences.unregisterOnSharedPreferenceChangeListener(listener)
  }
}

class IntPreferenceLiveData(
  preferences: SharedPreferences,
  key: String,
  defaultValue: Int
) : PreferenceLiveData<Int>(preferences, key, { getInt(key, defaultValue) })

fun SharedPreferences.toLiveData(key: String, defaultValue: Int): IntPreferenceLiveData {
  return IntPreferenceLiveData(this, key, defaultValue)
}
