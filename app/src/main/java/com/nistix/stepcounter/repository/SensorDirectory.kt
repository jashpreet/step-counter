package com.nistix.stepcounter.repository

object SensorDirectory {
  val sensors = listOf(
    AccelerometerSensor(),
    StepCounterSensor()
  )
}
