package org.prebid.mobile.prebidnextgendemo.testcases

import org.prebid.mobile.prebidnextgendemo.R
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

    lateinit var lastTestCase: TestCase

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
            RenderingApiNativeActivity::class.java
        )
    )

}