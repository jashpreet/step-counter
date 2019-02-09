package com.nistix.stepcounter.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.nistix.stepcounter.R
import com.nistix.stepcounter.app.App
import com.nistix.stepcounter.util.Complex

class AccelerometerSensor : AbstractSensor() {
  private val sensorManager by lazy {
    App.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  }
  private val sensor: Sensor? by lazy {
    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  }
  private var deltas: Array<Complex?>

  init {
    deltas = arrayOfNulls<Complex?>(ACCELEROMETER_RING_SIZE)
  }

  //region AbstractSensor
  override val uniqueId: Int = Sensor.TYPE_ACCELEROMETER

  override val titleRes: Int = R.string.sensor_accelerometer

  override fun isAvailable(): Boolean = sensor != null
  //endregion

  //region LiveData
  override fun onActive() {
    super.onActive()
    if (isAvailable())
      sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
  }

  override fun onInactive() {
    super.onInactive()
    if (isAvailable())
      sensorManager.unregisterListener(listener)
  }
  //endregion

  private val lowPassFilter = LowPassFilter()
  private var index = 0
  private var lastStepTimeNs: Long = 0

  private val listener = object : SensorEventListener {
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
      if (event != null) {
        val filtered = lowPassFilter.lowPassFilter(event.values)
        val delta = Math.sqrt(
          (filtered[0] * filtered[0] + filtered[1] * filtered[1] + filtered[2] * filtered[2]).toDouble()
        )
        deltas[index++ % ACCELEROMETER_RING_SIZE] = Complex(delta, 0.0)

        if (index % ACCELEROMETER_RING_SIZE == 0) {
          val result = Complex.fft(deltas)
          val isStepPattern =
            STEP_THRESHOLD_RANGE_MIN < result[0]!!.re() && result[0]!!.re() < STEP_THRESHOLD_RANGE_MAX
          if (isStepPattern) {
            for (complex in result) {
              var isStep = false
              if (complex!!.im() != 0.0) {
                if (complex.im() > IM_POSITIVE_THRESHOLD) {
                  IM_POSITIVE_THRESHOLD = IM_POSITIVE_THRESHOLD / complex.im() / 2
                  if (complex.im() > IM_POSITIVE_THRESHOLD) isStep = true
                } else if (complex.im() < IM_NEGATIVE_THRESHOLD) {
                  IM_NEGATIVE_THRESHOLD = IM_NEGATIVE_THRESHOLD / complex.im() / 2
                  if (complex.im() > IM_NEGATIVE_THRESHOLD) isStep = true
                }
              }
              isStep = isStep and (event.timestamp - lastStepTimeNs > STEP_DELAY_NS)

              if (isStep) {
                postValue(1)
                lastStepTimeNs = event.timestamp
                break
              }
            }
          }
        }

      }
    }
  }

  companion object {
    private const val ACCELEROMETER_RING_SIZE = 8 // n ^ 2
    private const val STEP_THRESHOLD_RANGE_MIN = 90
    private const val STEP_THRESHOLD_RANGE_MAX = 130
    private const val STEP_DELAY_NS = 35 * 10000000
    private var IM_POSITIVE_THRESHOLD = 3.0
    private var IM_NEGATIVE_THRESHOLD = -3.0
  }

  private class LowPassFilter {
    private val timestampOld = System.nanoTime().toFloat()
    private val output = FloatArray(3)
    private var count = 0

    internal fun lowPassFilter(input: FloatArray): FloatArray {
      val timestamp = System.nanoTime().toFloat()
      val dt = 1 / (count / ((timestamp - timestampOld) / 1000000000.0f))
      val alpha = timeConstant / (timeConstant + dt)
      output[0] = output[0] + alpha * (input[0] - output[0])
      output[1] = output[1] + alpha * (input[1] - output[1])
      output[2] = output[2] + alpha * (input[2] - output[2])
      count++
      return output
    }

    companion object {
      private const val timeConstant = 0.230f
    }
  }
}
