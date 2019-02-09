package com.nistix.stepcounter.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.nistix.stepcounter.R
import com.nistix.stepcounter.service.StepCounterService

class StepsActivity : AppCompatActivity() {
  private val viewModel: StepsViewModel by lazy {
    ViewModelProviders.of(this).get(StepsViewModel::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_steps)
    startService()
    viewModel.menuItems.observe(this, Observer { invalidateOptionsMenu() })
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    menu?.apply {
      viewModel.menuItems.value?.forEach {
        add(it.group, it.id, it.order, it.title)
          .setChecked(it.isChecked)
          .setEnabled(it.isEnabled)
          .isCheckable = true
      }
    }
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return when {
      item?.groupId == StepsViewModel.SENSORS_MENU_GROUP_ID -> {
        viewModel.selectSensor(item.itemId)
        true
      }
      item?.itemId == R.id.action_reset -> {
        viewModel.resetCounters()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun startService() {
    val intent = Intent(this, StepCounterService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
      startForegroundService(intent)
    else
      startService(intent)
  }

}
