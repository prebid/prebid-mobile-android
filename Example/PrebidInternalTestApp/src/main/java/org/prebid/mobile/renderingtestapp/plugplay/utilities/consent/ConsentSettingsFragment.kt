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

package org.prebid.mobile.renderingtestapp.plugplay.utilities.consent

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.widgets.EditTextPreferenceWithValue
import org.prebid.mobile.renderingtestapp.widgets.IntegerEditTextPreferenceWithValue

class ConsentSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_consent_settings)
        getDefaultSharedPreference()?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        when (val preference = key?.let { findPreference<Preference>(it) }) {
            is IntegerEditTextPreferenceWithValue ->
                preference.summary = sharedPreferences?.getInt(key, -1).toString()
            is EditTextPreferenceWithValue ->
                preference.summary = sharedPreferences?.getString(key, "") ?: ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getDefaultSharedPreference()?.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun getDefaultSharedPreference() = PreferenceManager
        .getDefaultSharedPreferences(requireContext())
}