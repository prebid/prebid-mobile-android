package com.openx.internal_test_app.plugplay.utilities.version

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.MobileAds
import com.mopub.common.MoPub
import com.openx.apollo.sdk.ApolloSettings
import com.openx.internal_test_app.R
import com.openx.internal_test_app.utils.BaseFragment
import kotlinx.android.synthetic.main.fragments_version_info.*

class VersionInfoFragment : BaseFragment() {
    override val layoutRes: Int = R.layout.fragments_version_info

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        MobileAds.initialize(requireContext())
        tvApolloVersion.text = ApolloSettings.SDK_VERSION
        tvMopubVersion.text = MoPub.SDK_VERSION
        tvGamVersion.text = MobileAds.getVersionString()
    }
}