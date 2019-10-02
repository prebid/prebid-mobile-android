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
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.webkit.WebView
import android.widget.*
import java.util.*

class MainActivity : AppCompatActivity() {

    // Default values
    private var adType = "Banner"
    private var adServer = "DFP"
    private var adSize = "300x250"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        // Get all the components
        val adTypeSpinner = findViewById(R.id.adTypeSpinner) as Spinner
        // Ad Type Spinner set up
        val adTypeAdapter = ArrayAdapter.createFromResource(
            this, R.array.adTypeArray,
            android.R.layout.simple_spinner_item
        )
        adTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adTypeSpinner.adapter = adTypeAdapter
        adTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            internal var adTypes = Arrays.asList(*resources.getStringArray(R.array.adTypeArray))

            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                if (pos > adTypes.size) {
                    return
                }
                adType = adTypes[pos]
                if (adType == "Banner") {
                    // show size and refresh millis
                    val adSizeRow = findViewById(R.id.adSizeRow) as LinearLayout
                    adSizeRow.visibility = View.VISIBLE
                } else {
                    // hide size selection and refresh millis
                    val adSizeRow = findViewById(R.id.adSizeRow) as LinearLayout
                    adSizeRow.visibility = View.GONE
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        // Ad Server Spinner
        val adServerSpinner = findViewById(R.id.adServerSpinner) as Spinner
        val adServerAdapter = ArrayAdapter.createFromResource(
            this, R.array.adServerArray,
            android.R.layout.simple_spinner_item
        )
        adServerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adServerSpinner.adapter = adServerAdapter
        adServerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            internal var adServers = Arrays.asList(*resources.getStringArray(R.array.adServerArray))

            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                if (pos > adServers.size) {
                    return
                }
                adServer = adServers[pos]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        // Ad Size Spinner
        val adSizeSpinner = findViewById(R.id.adSizeSpinner) as Spinner
        val adSizeAdapter = ArrayAdapter.createFromResource(
            this, R.array.adSizeArray,
            android.R.layout.simple_spinner_item
        )
        adSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adSizeSpinner.adapter = adSizeAdapter
        adSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            internal var adSizes = Arrays.asList(*resources.getStringArray(R.array.adSizeArray))

            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                if (pos > adSizes.size) {
                    return
                }
                adSize = adSizes[pos]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    fun showAd(view: View) {
        val demoActivityIntent = Intent(this, DemoActivity::class.java)
        demoActivityIntent.putExtra(Constants.AD_SERVER_NAME, adServer)
        demoActivityIntent.putExtra(Constants.AD_TYPE_NAME, adType)
        if (adType == "Banner") {
            demoActivityIntent.putExtra(Constants.AD_SIZE_NAME, adSize)
        }
        val autoRefreshMillis = findViewById(R.id.autoRefreshInput) as EditText
        val refreshMillisString = autoRefreshMillis.text.toString()
        if (!TextUtils.isEmpty(refreshMillisString)) {
            val refreshMillis = Integer.valueOf(refreshMillisString)
            demoActivityIntent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis)
        }
        startActivity(demoActivityIntent)
    }
}

