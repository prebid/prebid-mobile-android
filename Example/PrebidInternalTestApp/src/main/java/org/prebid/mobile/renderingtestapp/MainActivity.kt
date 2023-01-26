/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.renderingtestapp

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.test.espresso.IdlingResource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.prebid.mobile.ExternalUserId
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.rendering.sdk.deviceData.listeners.SdkInitListener
import org.prebid.mobile.renderingtestapp.plugplay.utilities.consent.ConsentUpdateManager
import org.prebid.mobile.renderingtestapp.utils.GppHelper
import org.prebid.mobile.renderingtestapp.utils.OpenRtbConfigs
import org.prebid.mobile.renderingtestapp.utils.OpenRtbExtra
import org.prebid.mobile.renderingtestapp.utils.PermissionHelper

class MainActivity : AppCompatActivity(), SdkInitListener {

    companion object {
        const val CURRENT_AD_TYPE = "CURRENT_AD_TYPE"
        const val MRAID_AD_NUMBER = "ad_number"

        const val EXTRA_OPEN_RTB = "EXTRA_OPEN_RTB"
        const val EXTRA_CONSENT_V1 = "EXTRA_CONSENT_V1"
        const val EXTRA_EIDS = "EXTRA_EIDS"
        const val EXTRA_NATIVE = "EXTRA_NATIVE"
    }

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var progress: ProgressDialog
    private lateinit var bottomAppBar: BottomNavigationView
    private lateinit var titleText: TextView

    private var consentUpdateManager: ConsentUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_main)
        addParametersFromCommandLine()
        initUi()

        handleLaunchOptions()

        PermissionHelper.requestPermission(this)
    }

    private fun addParametersFromCommandLine() {
        val geo = intent.extras?.getBoolean("shareGeo")
        val domain = intent.extras?.getString("targetingDomain")
        val gppString = intent.extras?.getString("gppString")
        val gppSid = intent.extras?.getString("gppSid")
        val gppHelper = GppHelper(PreferenceManager.getDefaultSharedPreferences(this))
        geo?.let { shareGeo ->
            PrebidMobile.setShareGeoLocation(shareGeo)
        }
        domain?.let { targetingDomain ->
            TargetingParams.setDomain(targetingDomain)
        }
        gppString?.let { gppStringValue ->
            gppHelper.addGppStringTestValue(gppStringValue)
        }
        gppSid?.let { gppSidValue ->
            gppHelper.addGppSidTestValue(gppSidValue)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        consentUpdateManager?.stopTimer()
    }

    override fun onSDKInit() {
        Log.i(TAG, "Prebid rendering SDK initialized successfully")
        progress.dismiss()
    }

    fun getIdlingResource(): IdlingResource? {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val idlingResource: IdlingResource? = if (fragment is AdFragment) fragment.idlingResource else null
        Log.d(TAG, "idling $idlingResource")
        return idlingResource
    }

    private fun initUi() {
        initProgressDialog()
        initBarNavigation()
    }

    private fun initProgressDialog() {
        progress = ProgressDialog(this)
        progress.setTitle("Please wait")
        progress.setMessage("Caching video ads")
        progress.setCancelable(false)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
    }

    private fun initBarNavigation() {
        bottomAppBar = findViewById(R.id.bottomNavigation)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        titleText = toolbar.findViewById(R.id.customTitle)
        val navController = findNavController(R.id.nav_host_fragment)
        setupToolbarNavigation(toolbar, navController)
        setupBottomBar(navController)
    }

    private fun handleLaunchOptions() {
        if (intent.extras?.containsKey(EXTRA_OPEN_RTB) == true) {
            extractOpenRtbExtra()
        }

        if (intent.extras?.containsKey(EXTRA_CONSENT_V1) == true) {
            handleConsentExtra()
        }

        if (intent.extras?.containsKey(EXTRA_EIDS) == true) {
            extractEidsExtras()
        }
    }


    private fun setupToolbarNavigation(toolbar: Toolbar, navController: NavController) {

        val topLevelDestinationIdSet = setOf(R.id.mainUtilitiesFragment, R.id.headerBiddingFragment)
        val appBarConfiguration = AppBarConfiguration(topLevelDestinationIdSet)

        toolbar.setupWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            handleNavControllerDestinationChange(destination, toolbar)
        }
    }

    private fun handleNavControllerDestinationChange(destination: NavDestination, toolbar: Toolbar) {
        val destinationId = destination.id

        setupSettingsVisibility(toolbar, destinationId)
        destination.label?.let { setTitleString(it.toString()) }
        toolbar.title = null

        val menuId = when (destinationId) {
            R.id.mainUtilitiesFragment -> R.id.menu_utilities
            R.id.headerBiddingFragment -> R.id.menu_bidding
            else -> -1
        }

        if (menuId != -1) {
            bottomAppBar.menu.findItem(menuId).isChecked = true
        }
    }

    private fun setupSettingsVisibility(toolbar: Toolbar, id: Int) {
        if (toolbar.menu.size() > 0) {
            val shouldShow = true
            toolbar.menu.getItem(0).isVisible = shouldShow
        }
    }

    private fun setupBottomBar(navController: NavController) {
        bottomAppBar.setupWithNavController(navController)
        bottomAppBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_utilities -> navController.navigate(R.id.utilitiesNavigation)
                R.id.menu_bidding -> navController.navigate(R.id.bidding_navigation)
                else -> navController.navigate(R.id.bidding_navigation)
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    fun setTitleString(title: String) {
        titleText.text = title
    }

    private fun parseOpenRtbJson(openRtbJson: String?): OpenRtbExtra? {
        try {
            return Gson().fromJson<OpenRtbExtra>(openRtbJson, object : TypeToken<OpenRtbExtra>() {}.type)
        } catch (ex: Exception) {
            Log.d(TAG, "Unable to parse provided OpenRTB list ${Log.getStackTraceString(ex)}")
            Toast.makeText(
                this,
                "Unable to parse provided OpenRTB. Provided JSON might contain an error",
                Toast.LENGTH_LONG
            ).show()
        }
        return null
    }

    private fun extractOpenRtbExtra() {
        val openRtbListJson = intent.extras?.getString(EXTRA_OPEN_RTB)
        val openRtbExtrasList = parseOpenRtbJson(openRtbListJson)
        if (openRtbExtrasList != null) {
            OpenRtbConfigs.setTargeting(openRtbExtrasList)
        }
    }

    private fun extractEidsExtras() {
        val eidsJsonString = intent.extras?.getString(EXTRA_EIDS)
        val eidsJsonArray = JSONArray(eidsJsonString)
        for (i in 0 until eidsJsonArray.length()) {
            val jsonObject = eidsJsonArray.get(i)
            if (jsonObject is JsonObject) {
                val source = jsonObject.get("source").asString
                val identifier = jsonObject.get("identifier").asString
                if (source == null || identifier == null) {
                    val aType = jsonObject.get("atype")
                    TargetingParams.storeExternalUserId(
                        if (aType == null) {
                            ExternalUserId(source, identifier, null, null)
                        } else {
                            ExternalUserId(source, identifier, aType.asInt, null)
                        }
                    )
                }
            }
        }
    }

    private fun handleConsentExtra() {
        consentUpdateManager = ConsentUpdateManager(PreferenceManager.getDefaultSharedPreferences(this))

        val configurationJson = intent.extras?.getString(EXTRA_CONSENT_V1)
        consentUpdateManager?.updateConsentConfiguration(configurationJson)
    }
}