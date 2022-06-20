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

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val FIRST_AD_SERVER = "In-App"
        private const val FIRST_AD_TYPE = "Video Banner"
    }

    private var adType = ""
    private var adServer = ""
    private var isFirstInit = true

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.btnShowAd.setOnClickListener { showAd() }
        initAdServerSpinner()
        initDefaultServer()
    }

    private fun showAd() {
        val refreshTime = getRefreshTime()
        val intent = DemoActivity.getIntent(this, adServer, adType, refreshTime)
        startActivity(intent)
    }

    private fun getRefreshTime(): Int {
        val refreshTimeString = binding.etAutoRefreshTime.text.toString()
        return try {
            Integer.valueOf(refreshTimeString)
        } catch (exception: Exception) {
            0
        }
    }

    private fun initAdServerSpinner() {
        val repository = AdTypesRepository.get()
        val primaryAdServers = ArrayList(repository.keys)
        val spinner = binding.spinnerAdServer

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, primaryAdServers)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                adServer = primaryAdServers[pos]
                val types = repository[adServer]?.map { it.name } ?: listOf()
                binding.btnShowAd.isEnabled = types.isNotEmpty()
                initAdTypeSpinner(ArrayList(types))
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun initAdTypeSpinner(list: ArrayList<String>) {
        val spinner = binding.spinnerAdType
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                binding.btnShowAd.isEnabled = true
                adType = list[pos]
                initDefaultAdType()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun initDefaultServer() {
        val indexOfServer = AdTypesRepository.get().keys.indexOf(FIRST_AD_SERVER)
        if (indexOfServer >= 0) {
            binding.spinnerAdServer.setSelection(indexOfServer, false)
        }
    }

    private fun initDefaultAdType() {
        if (isFirstInit) {
            isFirstInit = false
            val adTypes = AdTypesRepository.get()[FIRST_AD_SERVER]
            adTypes?.firstOrNull { it.name == FIRST_AD_TYPE }?.let {
                binding.spinnerAdType.setSelection(adTypes.indexOf(it), false)
            }
        }
    }

}

