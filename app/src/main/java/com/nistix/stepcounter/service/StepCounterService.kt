package com.nistix.stepcounter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.nistix.stepcounter.R
import com.nistix.stepcounter.app.App
import com.nistix.stepcounter.repository.SensorDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class StepCounterService : LifecycleService(), CoroutineScope {
  private lateinit var job: Job
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private val dao by lazy { App.db.stepsDao() }

  override fun onCreate() {
    super.onCreate()
    job = Job()

    SensorDirectory.sensors.filter { it.isAvailable() }.forEach { sensor ->
      sensor.observe(this, Observer { value ->
        value?.let { launch { dao.addStepsCount(sensor.uniqueId, it) } }
      })
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    startForeground(1, buildNotification(applicationContext))
    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    job.cancel()
  }

  //region Notifications
  private val channelId = "messages"

  private fun createMessagesNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = context.getString(R.string.message_channel_name)
      val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
      val notificationManager = context.getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(context: Context): Notification {
    createMessagesNotificationChannel(context)

    val builder = NotificationCompat.Builder(context, channelId)
    builder
      .setSmallIcon(R.drawable.ic_notification)
      .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
      .setContentText(getString(R.string.text_notification))
      .setAutoCancel(false)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      builder.priority = NotificationCompat.PRIORITY_LOW
    }

    val launchIntent = context.packageManager
      .getLaunchIntentForPackage(context.packageName)
    if (launchIntent != null) {
      val contentIntent = TaskStackBuilder.create(context)
        .addNextIntentWithParentStack(launchIntent)
        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
      builder.setContentIntent(contentIntent)
    }

    return builder.build()
  }
  //endregion
}