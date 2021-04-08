package org.prebid.mobile.renderingtestapp.plugplay.utilities.version

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.MobileAds
import com.mopub.common.MoPub
import kotlinx.android.synthetic.main.fragments_version_info.*
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.utils.BaseFragment

class VersionInfoFragment : BaseFragment() {
    override val layoutRes: Int = R.layout.fragments_version_info

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        MobileAds.initialize(requireContext())
        tvPrebidRenderingVersion.text = PrebidRenderingSettings.SDK_VERSION
        tvMopubVersion.text = MoPub.SDK_VERSION
        tvGamVersion.text = MobileAds.getVersionString()
    }
}