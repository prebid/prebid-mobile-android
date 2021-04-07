package com.openx.internal_test_app.plugplay.utilities.consent

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.openx.internal_test_app.R
import com.openx.widgets.EditTextPreferenceWithValue
import com.openx.widgets.IntegerEditTextPreferenceWithValue

class ConsentSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_consent_settings)
        getDefaultSharedPreference()?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {

        when (val preference = findPreference<Preference>(key)) {
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

    private fun getDefaultSharedPreference() = PreferenceManager.getDefaultSharedPreferences(context)
}