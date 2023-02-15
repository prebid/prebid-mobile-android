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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.test.espresso.IdlingResource
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.prebid.mobile.rendering.sdk.deviceData.listeners.SdkInitListener
import org.prebid.mobile.renderingtestapp.plugplay.utilities.consent.ConsentUpdateManager
import org.prebid.mobile.renderingtestapp.utils.CommandLineArgumentParser
import org.prebid.mobile.renderingtestapp.utils.PermissionHelper

class MainActivity : AppCompatActivity(), SdkInitListener {

    companion object {
        const val CURRENT_AD_TYPE = "CURRENT_AD_TYPE"
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

        CommandLineArgumentParser.parse(intent, this)

        initUi()

        PermissionHelper.requestPermission(this)
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

}