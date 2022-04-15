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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    companion object {
        private const val ARGS_AD_SERVER_NAME = "adServer"
        private const val ARGS_AD_TYPE_NAME = "adType"
        private const val ARGS_AD_REFRESH_TIME = "autoRefresh"

        fun getIntent(
            context: Context,
            adPrimaryServerName: String,
            adTypeName: String,
            adAutoRefreshTime: Int
        ): Intent {
            return Intent(context, DemoActivity::class.java).apply {
                putExtra(ARGS_AD_SERVER_NAME, adPrimaryServerName)
                putExtra(ARGS_AD_TYPE_NAME, adTypeName)
                putExtra(ARGS_AD_REFRESH_TIME, adAutoRefreshTime)
            }
        }
    }

    private var adPrimaryServerName = ""
    private var adTypeName = ""
    private var adAutoRefreshTime = 0

    private lateinit var binding: ActivityDemoBinding
    private lateinit var currentAdType: AdType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo)

        AdTypesRepository.usePrebidServer()
        useFakeGDPR()
        parseArguments()
        initViews()
        createBanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentAdType.onDestroy?.let { it() }
        PrebidMobile.setStoredAuctionResponse(null)
    }

    private fun parseArguments() {
        intent.apply {
            adPrimaryServerName = getStringExtra(ARGS_AD_SERVER_NAME) ?: ""
            adTypeName = getStringExtra(ARGS_AD_TYPE_NAME) ?: ""
            adAutoRefreshTime = getIntExtra(ARGS_AD_REFRESH_TIME, 0)
        }
    }

    private fun initViews() {
        binding.tvPrimaryAdServer.text = adPrimaryServerName
        binding.tvAdType.text = adTypeName
    }

    private fun createBanner() {
        binding.frameAdWrapper.removeAllViews()

        val allAdTypes = AdTypesRepository.get()
        val currentPrimaryAdServerTypes = allAdTypes[adPrimaryServerName]!!
        currentAdType = currentPrimaryAdServerTypes.find { it.name == adTypeName }!!
        currentAdType.onCreate(this, binding.frameAdWrapper, adAutoRefreshTime)
    }

    private fun useFakeGDPR() {
        // Only for test cases!!!
        PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
            putInt("IABTCF_gdprApplies", 0)
            putInt("IABTCF_CmpSdkID", 123)
            apply()
        }
    }

}
