package com.nistix.stepcounter.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Steps(
  @PrimaryKey var sensor: Int,
  var count: Int = 0
)
