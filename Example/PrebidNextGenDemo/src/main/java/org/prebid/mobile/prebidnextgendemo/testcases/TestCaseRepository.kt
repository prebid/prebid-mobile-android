package org.prebid.mobile.prebidnextgendemo.testcases

import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.Banner320x50OriginalApiActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.DisplayInterstitialOriginalApiActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.VideoRewardedOriginalApiActivity

object TestCaseRepository {

    lateinit var lastTestCase: TestCase

    fun getList() = arrayListOf(
        TestCase(
            R.string.gam_original_display_banner_320x50,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.GAM_ORIGINAL,
            Banner320x50OriginalApiActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_display_interstitial,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.GAM_ORIGINAL,
            DisplayInterstitialOriginalApiActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_video_rewarded,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.GAM_ORIGINAL,
            VideoRewardedOriginalApiActivity::class.java,
        )
    )

}