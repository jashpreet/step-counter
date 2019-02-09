package com.nistix.stepcounter.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.nistix.stepcounter.R
import com.nistix.stepcounter.app.App
import com.nistix.stepcounter.db.toLiveData
import com.nistix.stepcounter.repository.SensorDirectory
import com.nistix.stepcounter.util.AbsentLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StepsViewModel : ViewModel() {
  private val dao by lazy { App.db.stepsDao() }
  private val prefs by lazy { App.prefs }

  private var selectedSensor
    get() = prefs.getInt(PREFS_KEY_SENSOR, 0)
    set(value) {
      prefs.edit().putInt(PREFS_KEY_SENSOR, value).apply()
      buildMenuItems()
    }

  val menuItems = MutableLiveData<Array<MenuItem>>().apply { arrayOf<MenuItem>() }

  val stepsCount = Transformations
    .switchMap(prefs.toLiveData(PREFS_KEY_SENSOR, 0)) { id ->
      if (id != 0) dao.getObservableCount(id) else AbsentLiveData.create()
    }!!

  val message = Transformations
    .switchMap(prefs.toLiveData(PREFS_KEY_SENSOR, 0)) { id ->
      MutableLiveData<String>().apply {
        postValue(App.context.getString(if (id != 0) R.string.text_steps else R.string.text_not_available))
      }
    }!!

  init {
    val availableSensors = SensorDirectory.sensors.filter { it.isAvailable() }
    when {
      availableSensors.isEmpty() -> selectedSensor = 0
      selectedSensor == 0 -> selectedSensor = availableSensors.first().uniqueId
      else -> buildMenuItems()
    }
  }

  fun resetCounters() {
    GlobalScope.launch { dao.resetAllToZero() }
  }

  fun selectSensor(id: Int) {
    selectedSensor = id
  }

  private fun buildMenuItems() {
    val items = mutableListOf<MenuItem>()
    var order = 0
    SensorDirectory.sensors.forEach { sensor ->
      items.add(
        MenuItem(
          group = SENSORS_MENU_GROUP_ID,
          id = sensor.uniqueId,
          order = order++,
          title = App.context.getString(sensor.titleRes),
          isChecked = sensor.isAvailable() && selectedSensor == sensor.uniqueId,
          isEnabled = sensor.isAvailable()
        )
      )
    }
    menuItems.postValue(items.toTypedArray())
  }

  companion object {
    const val SENSORS_MENU_GROUP_ID = 1
    const val PREFS_KEY_SENSOR = "sensor"
  }
}

data class MenuItem(
  val group: Int,
  val id: Int,
  val order: Int,
  val title: String,
  val isChecked: Boolean,
  val isEnabled: Boolean
)
