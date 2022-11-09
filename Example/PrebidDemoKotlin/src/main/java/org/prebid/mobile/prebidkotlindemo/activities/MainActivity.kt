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

package org.prebid.mobile.prebidkotlindemo.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.MobileAds
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityMainBinding
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase
import org.prebid.mobile.prebidkotlindemo.testcases.TestCaseAdapter
import org.prebid.mobile.prebidkotlindemo.testcases.TestCaseRepository
import org.prebid.mobile.prebidkotlindemo.utils.ActionBarUtils

class MainActivity : AppCompatActivity() {

    companion object {
        private const val FIRST_AD_SERVER = "In-App + AdMob"
        private const val FIRST_AD_TYPE = ""
    }

    private var adType = ""
    private var adServer = ""
    private var isFirstInit = true

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ActionBarUtils.setTitle(getString(R.string.app_name), this)

        initAdServerSpinner()
        initDefaultServer()
        initList()

        PrebidMobile.checkGoogleMobileAdsCompatibility(MobileAds.getVersion().toString())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.settings) {
            startActivity(SettingsActivity.getIntent(this))
            return true
        }
        return false
    }


    private fun showAd() {
        val intent = DemoActivity.getIntent(this, adServer, adType)
        startActivity(intent)
    }

    private fun initAdServerSpinner() {
        val repository = TestCaseRepository.get()
        val primaryAdServers = ArrayList(repository.keys)
        val spinner = binding.spinnerAdServer

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, primaryAdServers)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                adServer = primaryAdServers[pos]
                val types = repository[adServer]?.map { it.name } ?: listOf()
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
                adType = list[pos]
                initDefaultAdType()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun initDefaultServer() {
        val indexOfServer = TestCaseRepository.get().keys.indexOf(FIRST_AD_SERVER)
        if (indexOfServer >= 0) {
            binding.spinnerAdServer.setSelection(indexOfServer, false)
        }
    }

    private fun initDefaultAdType() {
        if (isFirstInit) {
            isFirstInit = false
            val adTypes = TestCaseRepository.get()[FIRST_AD_SERVER]
            adTypes?.firstOrNull { it.name == FIRST_AD_TYPE }?.let {
                binding.spinnerAdType.setSelection(adTypes.indexOf(it), false)
            }
        }
    }

    private fun initList() {
        binding.rvAdTypes.apply {
            val currentAdapter = TestCaseAdapter()
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = currentAdapter
            currentAdapter.setList(
                arrayListOf(
                    TestCase("Banner 320x50 (GAM)", { _, _, _ -> }, {}),
                    TestCase("Banner 300x250 (GAM)", { _, _, _ -> }, {}),
                    TestCase("Banner Multisize (GAM)", { _, _, _ -> }, {}),
                    TestCase("Display Interstitial (GAM)", { _, _, _ -> }, {}),
                    TestCase("Video Interstitial (GAM)", { _, _, _ -> }, {}),
                    TestCase("Video Rewarded (GAM)", { _, _, _ -> }, {}),
                    TestCase("Banner 320x50 (AdMob)", { _, _, _ -> }, {}),
                    TestCase("Banner 300x250 (AdMob)", { _, _, _ -> }, {}),
                    TestCase("Banner Multisize (AdMob)", { _, _, _ -> }, {}),
                    TestCase("Display Interstitial (AdMob)", { _, _, _ -> }, {}),
                    TestCase("Video Interstitial (AdMob)", { _, _, _ -> }, {}),
                    TestCase("Video Rewarded (AdMob)", { _, _, _ -> }, {}),
                )
            )
        }
    }

}

