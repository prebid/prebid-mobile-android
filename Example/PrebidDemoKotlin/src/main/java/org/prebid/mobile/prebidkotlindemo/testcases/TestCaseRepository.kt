package org.prebid.mobile.prebidkotlindemo.testcases

import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original.*
import org.prebid.mobile.prebidkotlindemo.activities.ads.gam.rendering.*
import org.prebid.mobile.prebidkotlindemo.activities.ads.inapp.*

object TestCaseRepository {

    lateinit var lastTestCase: TestCase

    fun getList() = arrayListOf(
        /* GAM Original API */
        TestCase(
            R.string.gam_original_display_banner_320x50,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.GAM_ORIGINAL,
            GamOriginalApiDisplayBanner320x50Activity::class.java,
        ),
        TestCase(
            R.string.gam_original_display_banner_300x250,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.GAM_ORIGINAL,
            GamOriginalApiDisplayBanner300x250Activity::class.java,
        ),
        TestCase(
            R.string.gam_original_display_banner_multi_size,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.GAM_ORIGINAL,
            GamOriginalApiDisplayBannerMultiSizeActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_display_interstitial,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.GAM_ORIGINAL,
            GamOriginalApiDisplayInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_video_interstitial,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.GAM_ORIGINAL,
            GamOriginalApiVideoInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_video_rewarded,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.GAM_ORIGINAL,
            GamOriginalApiVideoRewardedActivity::class.java,
        ),
        TestCase(
            R.string.gam_original_native,
            AdFormat.NATIVE,
            IntegrationKind.GAM_ORIGINAL,
            GamOriginalApiNativeActivity::class.java,
        ),

        /* GAM Rendering API */
        TestCase(
            R.string.gam_rendering_display_banner_320x50,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.GAM_RENDERING,
            GamRenderingApiDisplayBanner320x50Activity::class.java,
        ),
        TestCase(
            R.string.gam_rendering_video_banner,
            AdFormat.VIDEO_BANNER,
            IntegrationKind.GAM_RENDERING,
            GamRenderingApiVideoBannerActivity::class.java,
        ),
        TestCase(
            R.string.gam_rendering_display_interstitial,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.GAM_RENDERING,
            GamRenderingApiDisplayInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.gam_rendering_video_interstitial,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.GAM_RENDERING,
            GamRenderingApiVideoInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.gam_rendering_video_rewarded,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.GAM_RENDERING,
            GamRenderingApiVideoRewardedActivity::class.java,
        ),
        TestCase(
            R.string.gam_rendering_native,
            AdFormat.NATIVE,
            IntegrationKind.GAM_RENDERING,
            GamRenderingApiNativeActivity::class.java,
        ),

        /* In-App (no ad server) */
        TestCase(
            R.string.in_app_display_banner_320x50,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.NO_AD_SERVER,
            InAppDisplayBanner320x50Activity::class.java,
        ),
        TestCase(
            R.string.in_app_display_banner_multi_size,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.NO_AD_SERVER,
            InAppDisplayBannerMultiSizeActivity::class.java,
        ),
        TestCase(
            R.string.in_app_display_banner_mraid_resize,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.NO_AD_SERVER,
            InAppDisplayBannerMraidResizeActivity::class.java,
        ),
        TestCase(
            R.string.in_app_display_banner_mraid_expand,
            AdFormat.DISPLAY_BANNER,
            IntegrationKind.NO_AD_SERVER,
            InAppDisplayBannerMraidExpandActivity::class.java,
        ),
        TestCase(
            R.string.in_app_video_banner,
            AdFormat.VIDEO_BANNER,
            IntegrationKind.NO_AD_SERVER,
            InAppVideoBannerActivity::class.java,
        ),
        TestCase(
            R.string.in_app_display_interstitial,
            AdFormat.DISPLAY_INTERSTITIAL,
            IntegrationKind.NO_AD_SERVER,
            InAppDisplayInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.in_app_video_interstitial,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.NO_AD_SERVER,
            InAppVideoInterstitialActivity::class.java,
        ),
        TestCase(
            R.string.in_app_video_interstitial_end_card,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.NO_AD_SERVER,
            InAppVideoInterstitialWithEndCardActivity::class.java,
        ),
        TestCase(
            R.string.in_app_video_interstitial_multi_format,
            AdFormat.VIDEO_INTERSTITIAL,
            IntegrationKind.NO_AD_SERVER,
            InAppVideoInterstitialMultiFormatActivity::class.java,
        ),
        TestCase(
            R.string.in_app_video_rewarded,
            AdFormat.VIDEO_REWARDED,
            IntegrationKind.NO_AD_SERVER,
            InAppVideoRewardedActivity::class.java,
        ),
        TestCase(
            R.string.in_app_native,
            AdFormat.NATIVE,
            IntegrationKind.NO_AD_SERVER,
            InAppNativeActivity::class.java,
        ),
    )

//        "In-App" to listOf(
//            AdType(
//                "Video Interstitial With End Card",
//                onCreate = { context, _, _ ->
//                    InAppVideoInterstitial.create(
//                        context,
//                        "imp-prebid-video-interstitial-320-480-with-end-card",
//                        "response-prebid-video-interstitial-320-480-with-end-card"
//                    )
//                },
//                onDestroy = { InAppVideoInterstitial.destroy() }
//            ),
//            AdType(
//                "Multiformat Interstitial",
//                onCreate = { context, _, _ ->
//                    val storedAuctionResponses = listOf(
//                        "response-prebid-video-interstitial-320-480",
//                        "response-prebid-display-interstitial-320-480"
//                    )
//                    InAppInterstitial.create(
//                        context, 30, 30,
//                        "imp-prebid-video-interstitial-320-480",
//                        storedAuctionResponses.random(),
//                        EnumSet.of(AdUnitFormat.VIDEO, AdUnitFormat.DISPLAY)
//                    )
//                },
//                onDestroy = { InAppVideoInterstitial.destroy() }
//            ),
//        ),
//
//        "In-App + AdMob" to listOf(
//            AdType(
//                "Banner 320x50",
//                onCreate = { activity, wrapper, autoRefreshTime ->
//                    InAppAdMobBanner.create(
//                        activity, wrapper, autoRefreshTime,
//                        320, 50,
//                        "ca-app-pub-1875909575462531/3793078260",
//                        "imp-prebid-banner-320-50",
//                        "response-prebid-banner-320-50"
//                    )
//                },
//                onDestroy = { InAppAdMobBanner.destroy() }
//            ),
//            AdType(
//                "Display Interstitial",
//                onCreate = { activity, _, _ ->
//                    InAppAdMobInterstitial.create(
//                        activity,
//                        "ca-app-pub-1875909575462531/6393291067",
//                        "imp-prebid-display-interstitial-320-480",
//                        EnumSet.of(AdUnitFormat.DISPLAY),
//                        "response-prebid-display-interstitial-320-480"
//                    )
//                },
//                onDestroy = { InAppAdMobInterstitial.destroy() }
//            ),
//            AdType(
//                "Video Interstitial",
//                onCreate = { activity, _, _ ->
//                    InAppAdMobInterstitial.create(
//                        activity,
//                        "ca-app-pub-1875909575462531/6393291067",
//                        "imp-prebid-video-interstitial-320-480",
//                        EnumSet.of(AdUnitFormat.VIDEO), "" +
//                                "response-prebid-video-interstitial-320-480"
//                    )
//                },
//                onDestroy = { InAppAdMobInterstitial.destroy() }
//            ),
//            AdType(
//                "Rewarded Interstitial",
//                onCreate = { activity, _, _ ->
//                    InAppAdMobRewarded.create(
//                        activity,
//                        "ca-app-pub-1875909575462531/1908212572",
//                        "imp-prebid-video-rewarded-320-480",
//                        "response-prebid-video-rewarded-320-480"
//                    )
//                },
//                onDestroy = { InAppAdMobRewarded.destroy() }
//            ),
//            AdType(
//                "Native Ad",
//                onCreate = { _, wrapper, _ ->
//                    // TODO: Problems with ids (current example's type is not Native)
//                    InAppAdMobNative.create(
//                        wrapper,
//                        "ca-app-pub-1875909575462531/9720985924",
//                        "imp-prebid-banner-native-styles",
//                        "response-prebid-banner-native-styles"
//                    )
//                },
//                onDestroy = {
//                    InAppAdMobNative.destroy()
//                }
//            )
//        ),
//
//        "In-App + Applovin MAX" to listOf(
//            AdType(
//                "Banner 320x50",
//                onCreate = { _, wrapper, autoRefreshTime ->
//                    PrebidMobile.setStoredAuctionResponse("response-prebid-banner-320-50")
//                    InAppMaxBanner.create(
//                        wrapper, autoRefreshTime / 1000,
//                        "3d8a0bcbb6d571d5",
//                        "imp-prebid-banner-320-50"
//                    )
//                },
//                onDestroy = { InAppMaxBanner.destroy() }
//            ),
//            AdType(
//                "MREC",
//                onCreate = { _, wrapper, autoRefreshTime ->
//                    PrebidMobile.setStoredAuctionResponse("response-prebid-banner-300-250")
//                    InAppMaxMrec.create(
//                        wrapper, autoRefreshTime / 1000,
//                        "550e6c2fe979a641",
//                        "imp-prebid-banner-300-250"
//                    )
//                },
//                onDestroy = { InAppMaxMrec.destroy() }
//            ),
//            AdType(
//                "Interstitial",
//                onCreate = { activity, _, _ ->
//                    PrebidMobile.setStoredAuctionResponse("response-prebid-display-interstitial-320-480")
//                    InAppMaxInterstitial.create(
//                        activity,
//                        "393697e649678807",
//                        "imp-prebid-display-interstitial-320-480"
//                    )
//                },
//                onDestroy = { InAppMaxInterstitial.destroy() }
//            ),
//            AdType(
//                "Rewarded",
//                onCreate = { activity, _, _ ->
//                    PrebidMobile.setStoredAuctionResponse("response-prebid-video-rewarded-320-480")
//                    InAppMaxRewarded.create(
//                        activity,
//                        "897f2fc59d617715",
//                        "imp-prebid-video-rewarded-320-480"
//                    )
//                },
//                onDestroy = { InAppMaxRewarded.destroy() }
//            ),
//            AdType(
//                "Native",
//                onCreate = { activity, wrapper, _ ->
//                    PrebidMobile.setStoredAuctionResponse("response-prebid-banner-native-styles")
//                    InAppMaxNative.create(
//                        activity,
//                        wrapper,
//                        "f3bdfa9dd8da1c4d",
//                        "imp-prebid-banner-native-styles"
//                    )
//                },
//                onDestroy = { InAppMaxNative.destroy() }
//            ),
//        )
//    )

    fun usePrebidServer() {
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
        PrebidMobile.setPrebidServerHost(Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction"))
    }

}