package com.nistix.stepcounter.repository

import android.arch.lifecycle.LiveData

abstract class AbstractSensor : LiveData<Int>() {

  abstract val uniqueId: Int

  abstract val titleRes: Int

  abstract fun isAvailable(): Boolean

}
