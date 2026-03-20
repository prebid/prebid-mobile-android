package org.prebid.mobile.prebidnextgendemo.testcases

import android.os.Bundle
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiBanner320x50Activity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiDisplayBannerMultiSizeActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiDisplayInterstitialActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiMultiformatBannerActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiMultiformatBannerVideoNativeInAppActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiMultiformatBannerVideoNativeStylesActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiMultiformatInterstitialActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiNativeInAppActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiNativeStylesActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiVideoBannerActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiVideoInterstitialActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.original.OriginalApiVideoRewardedActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.rendering.RenderingApiDisplayBanner320x50Activity
import org.prebid.mobile.prebidnextgendemo.activities.ads.rendering.RenderingApiDisplayInterstitialActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.rendering.RenderingApiNativeActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.rendering.RenderingApiVideoBannerActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.rendering.RenderingApiVideoInterstitialActivity
import org.prebid.mobile.prebidnextgendemo.activities.ads.rendering.RenderingApiVideoRewardedActivity

object TestCaseRepository {

    private const val NO_BIDS_CONFIG_ID = "prebid-demo-no-bids"

    lateinit var lastTestCase: TestCase

    private fun extrasOf(
        adUnitId: String? = null,
        configId: String? = null,
        width: Int? = null,
        height: Int? = null,
        customFormatId: String? = null,
    ) = Bundle().apply {
        adUnitId?.let { putString(BaseAdActivity.EXTRA_AD_UNIT_ID, it) }
        configId?.let { putString(BaseAdActivity.EXTRA_CONFIG_ID, it) }
        width?.let { putInt(BaseAdActivity.EXTRA_WIDTH, it) }
        height?.let { putInt(BaseAdActivity.EXTRA_HEIGHT, it) }
        customFormatId?.let { putString(BaseAdActivity.EXTRA_CUSTOM_FORMAT_ID, it) }
    }

    fun getList() = arrayListOf(
        TestCase(
            R.string.original_display_banner_320x50,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.ORIGINAL,
            OriginalApiBanner320x50Activity::class.java
        ),
        TestCase(
            R.string.original_display_banner_multi_size,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.ORIGINAL,
            OriginalApiDisplayBannerMultiSizeActivity::class.java
        ),
        TestCase(
            R.string.original_video_banner,
            AdFormat.VIDEO_BANNER,
            IntegrationKind.ORIGINAL,
            OriginalApiVideoBannerActivity::class.java
        ),
        TestCase(
            R.string.original_multiformat_banner,
            AdFormat.MULTIFORMAT,
            IntegrationKind.ORIGINAL,
            OriginalApiMultiformatBannerActivity::class.java
        ),
        TestCase(
            R.string.original_display_interstitial,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.ORIGINAL,
            OriginalApiDisplayInterstitialActivity::class.java
        ),
        TestCase(
            R.string.original_video_interstitial,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.ORIGINAL,
            OriginalApiVideoInterstitialActivity::class.java
        ),
        TestCase(
            R.string.original_video_rewarded,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.ORIGINAL,
            OriginalApiVideoRewardedActivity::class.java
        ),
        TestCase(
            R.string.original_multiformat_interstitial,
            AdFormat.MULTIFORMAT,
            IntegrationKind.ORIGINAL,
            OriginalApiMultiformatInterstitialActivity::class.java
        ),
        TestCase(
            R.string.original_native_in_app,
            AdFormat.NATIVE,
            IntegrationKind.ORIGINAL,
            OriginalApiNativeInAppActivity::class.java
        ),
        TestCase(
            R.string.original_native_styles,
            AdFormat.NATIVE,
            IntegrationKind.ORIGINAL,
            OriginalApiNativeStylesActivity::class.java
        ),
        TestCase(
            R.string.original_multiformat_banner_video_native_in_app,
            AdFormat.MULTIFORMAT,
            IntegrationKind.ORIGINAL,
            OriginalApiMultiformatBannerVideoNativeInAppActivity::class.java
        ),
        TestCase(
            R.string.original_multiformat_banner_video_native_styles,
            AdFormat.MULTIFORMAT,
            IntegrationKind.ORIGINAL,
            OriginalApiMultiformatBannerVideoNativeStylesActivity::class.java
        ),
        TestCase(
            R.string.rendering_display_banner_320x50,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.RENDERING,
            RenderingApiDisplayBanner320x50Activity::class.java
        ),
        TestCase(
            R.string.rendering_video_banner,
            AdFormat.VIDEO_BANNER,
            IntegrationKind.RENDERING,
            RenderingApiVideoBannerActivity::class.java
        ),
        TestCase(
            R.string.rendering_display_interstitial,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.RENDERING,
            RenderingApiDisplayInterstitialActivity::class.java
        ),
        TestCase(
            R.string.rendering_video_interstitial,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.RENDERING,
            RenderingApiVideoInterstitialActivity::class.java
        ),
        TestCase(
            R.string.rendering_video_rewarded,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.RENDERING,
            RenderingApiVideoRewardedActivity::class.java
        ),
        TestCase(
            R.string.rendering_native,
            AdFormat.NATIVE,
            IntegrationKind.RENDERING,
            RenderingApiNativeActivity::class.java,
            extrasOf(customFormatId = RenderingApiNativeActivity.CUSTOM_FORMAT_ID)
        ),
        TestCase(
            R.string.rendering_display_banner_320x50_no_bids,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.RENDERING,
            RenderingApiDisplayBanner320x50Activity::class.java,
            extrasOf(
                adUnitId = "/21808260008/prebid_oxb_320x50_banner_static",
                configId = NO_BIDS_CONFIG_ID,
                width = 320,
                height = 50
            )
        ),
        TestCase(
            R.string.rendering_video_banner_no_bids,
            AdFormat.VIDEO_BANNER,
            IntegrationKind.RENDERING,
            RenderingApiVideoBannerActivity::class.java,
            extrasOf(
                adUnitId = "/21808260008/prebid_oxb_outsream_video",
                configId = NO_BIDS_CONFIG_ID,
                width = 300,
                height = 250
            )
        ),
        TestCase(
            R.string.rendering_display_interstitial_no_bids,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.RENDERING,
            RenderingApiDisplayInterstitialActivity::class.java,
            extrasOf(
                adUnitId = "/21808260008/prebid_oxb_320x480_html_interstitial_static",
                configId = NO_BIDS_CONFIG_ID
            )
        ),
        TestCase(
            R.string.rendering_video_interstitial_no_bids,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.RENDERING,
            RenderingApiVideoInterstitialActivity::class.java,
            extrasOf(
                adUnitId = "/21808260008/prebid_oxb_320x480_interstitial_video_static",
                configId = NO_BIDS_CONFIG_ID
            )
        ),
        TestCase(
            R.string.rendering_video_rewarded_no_bids,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.RENDERING,
            RenderingApiVideoRewardedActivity::class.java,
            extrasOf(
                adUnitId = "/21808260008/prebid_oxb_rewarded_video_static",
                configId = NO_BIDS_CONFIG_ID
            )
        ),
        TestCase(
            R.string.rendering_native_no_bids,
            AdFormat.NATIVE,
            IntegrationKind.RENDERING,
            RenderingApiNativeActivity::class.java,
            extrasOf(
                adUnitId = "/21808260008/apollo_custom_template_native_ad_unit",
                configId = NO_BIDS_CONFIG_ID,
                customFormatId = "11982639"
            )
        )
    )

}