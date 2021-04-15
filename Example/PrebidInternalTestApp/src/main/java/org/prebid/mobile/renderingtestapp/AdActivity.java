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

package org.prebid.mobile.renderingtestapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;

public abstract class AdActivity extends BaseActivity {
    protected boolean settingsState = false;
    protected MenuItem settingsMenuItem;
    protected View settingsPane = null;
    protected View ad = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsState = false;

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayUseLogoEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setIcon(R.drawable.topbar_logo);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                supportActionBar.setTitle(extras.getCharSequence(MainActivity.CURRENT_AD_TYPE, ""));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (settingsPane != null) {
            getMenuInflater().inflate(R.menu.main, menu);
            settingsMenuItem = menu.findItem(R.id.action_settings);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("OptionItemSelected", getClass().getSimpleName());
        // Handle action bar actions click
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                if (!settingsState) {
                    showSettings();
                }
                else {
                    hideSettings();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void setAd(View currentAd) {
        this.ad = currentAd;
    }

    protected void setSettingsPane(View currentSettingsPane) {
        this.settingsPane = currentSettingsPane;
    }

    protected void showSettings() {
        if (ad != null && settingsPane != null) {
            if (settingsMenuItem != null) {
                settingsMenuItem.setIcon(R.drawable.icon_topbar_adparams_active);
            }
            settingsPane.setVisibility(View.VISIBLE);
            settingsState = true;
        }
    }

    protected void hideSettings() {
        if (ad != null && settingsPane != null) {
            if (settingsMenuItem != null) {
                settingsMenuItem.setIcon(R.drawable.icon_topbar_adparams);
            }
            settingsPane.setVisibility(View.GONE);
            settingsState = false;
        }
    }
}
