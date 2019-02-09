package com.nistix.stepcounter.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.nistix.stepcounter.R
import com.nistix.stepcounter.app.App

class StepCounterSensor : AbstractSensor() {
  private val sensorManager by lazy {
    App.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  }
  private val sensor: Sensor? by lazy {
    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
  }

  private var lastValue: Int? = null

  override val uniqueId: Int = Sensor.TYPE_STEP_COUNTER

  override val titleRes: Int = R.string.sensor_step_counter

  override fun isAvailable(): Boolean = sensor != null

  override fun onActive() {
    super.onActive()
    if (isAvailable())
      sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
  }

  //override fun onInactive() {}
  // Do NOT unregister for this type of sensor
  // See: https://developer.android.com/reference/android/hardware/Sensor#TYPE_STEP_COUNTER

  private val listener = object : SensorEventListener {
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
      event?.let {
        val currentValue = it.values[0].toInt()
        if (lastValue == null) lastValue = currentValue
        else {
          value = currentValue - lastValue!!
          lastValue = currentValue
        }
      }
    }
  }
}
