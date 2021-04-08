package org.prebid.mobile.renderingtestapp.plugplay.utilities

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.prebid.mobile.renderingtestapp.R

class AppSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_app_settings)
    }
}