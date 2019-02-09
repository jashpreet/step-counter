package com.nistix.stepcounter.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.nistix.stepcounter.app.App
import com.nistix.stepcounter.repository.AbstractSensor
import java.util.concurrent.Executors

@Database(
  entities = [Steps::class],
  version = 1
)
abstract class StepsDatabase : RoomDatabase() {

  abstract fun stepsDao(): StepsDao

  companion object {
    private const val DB_FILE = "steps-db"

    fun build(context: Context, sensors: List<AbstractSensor>? = null): StepsDatabase {
      val builder = Room.databaseBuilder(context, StepsDatabase::class.java, DB_FILE)

      if (sensors != null) {
        builder.addCallback(object : RoomDatabase.Callback() {
          override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Executors.newSingleThreadScheduledExecutor().execute {
              for (sensor in sensors) App.db.stepsDao().insertOrUpdate(Steps(sensor.uniqueId))
            }
          }
        })
      }

      return builder.build()
    }
  }
}
