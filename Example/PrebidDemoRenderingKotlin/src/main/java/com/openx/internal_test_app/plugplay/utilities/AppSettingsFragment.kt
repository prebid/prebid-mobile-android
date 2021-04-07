package com.openx.internal_test_app.plugplay.utilities

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.openx.internal_test_app.R

class AppSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_app_settings)
    }
}