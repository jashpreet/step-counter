package com.nistix.stepcounter.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.nistix.stepcounter.R

class StepsFragment : Fragment() {
  private val viewModel: StepsViewModel by lazy {
    ViewModelProviders.of(activity!!).get(StepsViewModel::class.java)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_steps, container, false)?.apply {

      findViewById<TextView>(R.id.txt_count)?.apply {
        viewModel.stepsCount.observe(this@StepsFragment, Observer { value ->
          visibility = if (value != null) VISIBLE else GONE
          text = value?.toString()
        })
      }

      findViewById<TextView>(R.id.txt_message)?.apply {
        viewModel.message.observe(this@StepsFragment, Observer { value ->
          text = value
        })
      }

    }
  }
}
