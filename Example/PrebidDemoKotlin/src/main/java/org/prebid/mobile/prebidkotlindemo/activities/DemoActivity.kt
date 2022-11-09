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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityDemoBinding
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase
import org.prebid.mobile.prebidkotlindemo.testcases.TestCaseRepository
import org.prebid.mobile.prebidkotlindemo.utils.Settings

class DemoActivity : AppCompatActivity() {

    companion object {
        private const val ARGS_AD_SERVER_NAME = "adServer"
        private const val ARGS_AD_TYPE_NAME = "adType"

        fun getIntent(
            context: Context,
            adPrimaryServerName: String,
            adTypeName: String,
        ): Intent {
            return Intent(context, DemoActivity::class.java).apply {
                putExtra(ARGS_AD_SERVER_NAME, adPrimaryServerName)
                putExtra(ARGS_AD_TYPE_NAME, adTypeName)
            }
        }
    }

    private var adPrimaryServerName = ""
    private var adTypeName = ""

    private lateinit var binding: ActivityDemoBinding
    private lateinit var currentTestCase: TestCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo)

        TestCaseRepository.usePrebidServer()
        useFakeGDPR()
        parseArguments()
        initViews()
        createBanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentTestCase.onDestroy?.let { it() }
        PrebidMobile.setStoredAuctionResponse(null)
    }

    private fun parseArguments() {
        intent.apply {
            adPrimaryServerName = getStringExtra(ARGS_AD_SERVER_NAME) ?: ""
            adTypeName = getStringExtra(ARGS_AD_TYPE_NAME) ?: ""
        }
    }

    private fun initViews() {
        binding.tvPrimaryAdServer.text = adPrimaryServerName
        binding.tvAdType.text = adTypeName
    }

    private fun createBanner() {
        binding.frameAdWrapper.removeAllViews()

        val allAdTypes = TestCaseRepository.get()
        val currentPrimaryAdServerTypes = allAdTypes[adPrimaryServerName]!!

        currentTestCase = currentPrimaryAdServerTypes.find { it.name == adTypeName }!!
        currentTestCase.onCreate(this, binding.frameAdWrapper, Settings.get().refreshTimeSeconds)
    }
}
