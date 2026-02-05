package org.prebid.mobile.prebidnextgendemo.testcases

import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiBanner320x50Activity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiDisplayBannerMultiSizeActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiDisplayInterstitialActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiMultiformatBannerActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiMultiformatBannerVideoNativeInAppActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiNativeInAppActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiNativeStylesActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiVideoBannerActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiVideoInterstitialActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiVideoRewardedActivity

object TestCaseRepository {

    lateinit var lastTestCase: TestCase

    fun getList() = arrayListOf(
        TestCase(
            R.string.gam_original_display_banner_320x50,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiBanner320x50Activity::class.java,
        ),
        TestCase(
            R.string.gam_original_display_banner_multi_size,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiDisplayBannerMultiSizeActivity::class.java
        ),
        TestCase(
            R.string.gam_original_video_banner,
            AdFormat.VIDEO_BANNER,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiVideoBannerActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_multiformat_banner,
            AdFormat.MULTIFORMAT,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiMultiformatBannerActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_display_interstitial,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiDisplayInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_video_interstitial,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiVideoInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_video_rewarded,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiVideoRewardedActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_native_in_app,
            AdFormat.NATIVE,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiNativeInAppActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_native_styles,
            AdFormat.NATIVE,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiNativeStylesActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_multiformat_banner_video_native_in_app,
            AdFormat.MULTIFORMAT,
            IntegrationKind.GAM_ORIGINAL,
            OriginalApiMultiformatBannerVideoNativeInAppActivity::class.java,
        )
    )

}