/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.prebidkotlindemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.annotation.ArrayRes
import androidx.databinding.DataBindingUtil
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var adType = "Banner"
    private var adServer = "DFP"
    private var adSize = "300x250"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        initAdTypeSpinner()
        initAdServerSpinner()
        initAdSizeSpinner()

        binding.btnShowAd.setOnClickListener {
            showAd()
        }
    }

    private fun showAd() {
        val refreshTime = getRefreshTime()
        val intent = Intent(this, DemoActivity::class.java).apply {
            putExtra(Constants.AD_SERVER_NAME, adServer)
            putExtra(Constants.AD_TYPE_NAME, adType)
            if (adType == "Banner") {
                putExtra(Constants.AD_SIZE_NAME, adSize)
            }
            if (refreshTime != null) {
                putExtra(Constants.AUTO_REFRESH_NAME, refreshTime)
            }
        }
        startActivity(intent)
    }

    private fun getRefreshTime(): Int? {
        val refreshTimeString = binding.etAutoRefreshTime.text.toString()
        return try {
            Integer.valueOf(refreshTimeString)
        } catch (exception: Exception) {
            null
        }
    }

    private fun initAdTypeSpinner() {
        val spinner = binding.spinnerAdType
        initSpinner(spinner, R.array.adTypeArray) {
            adType = it
            if (adType == "Banner") {
                showSizeSpinner()
            } else {
                hideSizeSpinner()
            }
        }
    }

    private fun initAdServerSpinner() {
        val spinner = binding.spinnerAdServer
        initSpinner(spinner, R.array.adServerArray) {
            adServer = it
        }
    }

    private fun initAdSizeSpinner() {
        val spinner = binding.spinnerAdSize
        initSpinner(spinner, R.array.adSizeArray) {
            adSize = it
        }
    }

    private fun initSpinner(spinner: Spinner, @ArrayRes arrayResId: Int, onNewItemSelected: (String) -> Unit) {
        val adapter = ArrayAdapter.createFromResource(
            this, arrayResId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var array = listOf(*resources.getStringArray(arrayResId))

            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                if (pos > array.size) {
                    return
                }
                onNewItemSelected(array[pos])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun showSizeSpinner() {
        val adSizeRow = binding.adSizeRow
        adSizeRow.visibility = View.VISIBLE
    }

    private fun hideSizeSpinner() {
        val adSizeRow = binding.adSizeRow
        adSizeRow.visibility = View.GONE
    }
}

