package com.nistix.stepcounter.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface StepsDao {
  @Query("SELECT count FROM steps WHERE sensor = :sensor")
  fun getCount(sensor: Int): Int

  @Query("SELECT count FROM steps WHERE sensor = :sensor")
  fun getObservableCount(sensor: Int): LiveData<Int>

  @Insert(onConflict = REPLACE)
  fun insertOrUpdate(steps: Steps)

  @Query("UPDATE steps SET count = 0")
  fun resetAllToZero()

  @Query("UPDATE steps SET count = count + :value WHERE sensor = :sensor")
  fun addStepsCount(sensor: Int, value: Int)
}
