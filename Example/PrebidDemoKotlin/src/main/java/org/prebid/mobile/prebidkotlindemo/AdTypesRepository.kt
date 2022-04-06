package org.prebid.mobile.prebidkotlindemo

import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.prebidkotlindemo.ads.GamBanner
import org.prebid.mobile.prebidkotlindemo.ads.GamInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.inapp.*
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobBanner
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobNative
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobRewarded
import org.prebid.mobile.prebidkotlindemo.ads.inappgam.*

import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import java.util.*

object AdTypesRepository {

    fun get() = mapOf(
        "Google Ad Manager" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    GamBanner.create(
                        wrapper, autoRefreshTime,
                        320, 50,
                        // TODO: Problem with ids
                        "/5300653/pavliuchyk_test_adunit_1x1_puc",
                        "imp-prebid-banner-320-50", "response-prebid-banner-320-50"
                    )
                },
                onDestroy = { GamBanner.destroy() }
            ),
            AdType(
                "Banner 300x250",
                onCreate = { _, wrapper, autoRefreshTime ->
                    GamBanner.create(
                        wrapper, autoRefreshTime,
                        300, 250,
                        "/5300653/pavliuchyk_test_adunit_1x1_puc",
                        "imp-prebid-banner-300-250","response-prebid-banner-300-250"
                    )
                },
                onDestroy = { GamBanner.destroy() }
            ),
            AdType(
                "Interstitial",
                onCreate = { activity, _, autoRefreshTime ->
                    GamInterstitial.create(
                        activity, autoRefreshTime,
                        // TODO: Problem with ids
                        "/5300653/pavliuchyk_test_adunit_1x1_puc",
                        "imp-prebid-display-interstitial-320-480","response-prebid-display-interstitial-320-480"
                    )
                },
                onDestroy = { GamInterstitial.destroy() }
            )
        ),

        "In-App" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "imp-prebid-banner-320-50","response-prebid-banner-320-50"
                    )
                },
                onDestroy = { InAppBanner.destroy() }
            ),
            AdType(
                "Interstitial",
                onCreate = { context, _, _ ->
                    InAppInterstitial.create(
                        context,
                        30, 30,
                        "imp-prebid-display-interstitial-320-480","response-prebid-display-interstitial-320-480"
                    )
                },
                onDestroy = { InAppInterstitial.destroy() }
            ),
            AdType(
                "Video Banner",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppVideoBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        300, 250,
                        "imp-prebid-video-outstream","response-prebid-video-outstream"
                    )
                },
                onDestroy = { InAppVideoBanner.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { context, _, _ ->
                    InAppVideoInterstitial.create(
                        context,
                        "imp-prebid-video-interstitial-320-480","response-prebid-video-interstitial-320-480"
                    )
                },
                onDestroy = { InAppVideoInterstitial.destroy() }
            ),
            AdType(
                "Rewarded Interstitial",
                onCreate = { context, _, _ ->
                    InAppRewardedInterstitial.create(
                        context,
                        "imp-prebid-video-rewarded-320-480","response-prebid-video-rewarded-320-480"
                    )
                },
                onDestroy = { InAppRewardedInterstitial.destroy() }
            )
        ),

        "In-App + Google Ad Manager" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppGamBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "/21808260008/prebid_oxb_320x50_banner",
                        "imp-prebid-banner-320-50","response-prebid-banner-320-50"
                    )
                },
                onDestroy = { InAppGamBanner.destroy() }
            ),
            AdType(
                "Interstitial",
                onCreate = { context, _, _ ->
                    InAppGamInterstitial.create(
                        context,
                        30, 30,
                        "/21808260008/prebid_oxb_html_interstitial",
                        "imp-prebid-display-interstitial-320-480","response-prebid-display-interstitial-320-480"
                    )
                },
                onDestroy = { InAppGamInterstitial.destroy() }
            ),
            AdType(
                "Video Banner",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppGamVideoBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        300, 250,
                        "/21808260008/prebid_oxb_300x250_banner",
                        "imp-prebid-video-outstream","response-prebid-video-outstream"
                    )
                },
                onDestroy = { InAppGamVideoBanner.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { activity, _, _ ->
                    InAppGamVideoInterstitial.create(
                        activity,
                        "/21808260008/prebid_oxb_320x480_interstitial_video_static",
                        "imp-prebid-video-interstitial-320-480","response-prebid-video-interstitial-320-480"
                    )
                },
                onDestroy = { InAppGamVideoInterstitial.destroy() }
            ),
            AdType(
                "Rewarded Interstitial",
                onCreate = { activity, _, _ ->
                    InAppGamRewardedInterstitial.create(
                        activity,
                        "/21808260008/prebid_oxb_rewarded_video_test",
                        "imp-prebid-video-rewarded-320-480","response-prebid-video-rewarded-320-480"
                    )
                },
                onDestroy = { InAppGamRewardedInterstitial.destroy() }
            )
        ),

        "In-App + AdMob" to listOf(
            AdType(
                "Banner",
                onCreate = { activity, wrapper, autoRefreshTime ->
                    InAppAdMobBanner.create(
                        activity, wrapper, autoRefreshTime,
                        320, 50,
                        "ca-app-pub-1875909575462531/3793078260",
                        "imp-prebid-banner-320-50","response-prebid-banner-320-50"
                    )
                },
                onDestroy = { InAppAdMobBanner.destroy() }
            ),
            AdType(
                "Display Interstitial",
                onCreate = { activity, _, _ ->
                    InAppAdMobInterstitial.create(
                        activity,
                        "ca-app-pub-1875909575462531/6393291067",
                        "imp-prebid-display-interstitial-320-480",
                        EnumSet.of(AdUnitFormat.DISPLAY),"response-prebid-display-interstitial-320-480"
                    )
                },
                onDestroy = { InAppAdMobInterstitial.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { activity, _, _ ->
                    InAppAdMobInterstitial.create(
                        activity,
                        "ca-app-pub-1875909575462531/6393291067",
                        "imp-prebid-video-interstitial-320-480",
                        EnumSet.of(AdUnitFormat.VIDEO),"response-prebid-video-interstitial-320-480"
                    )
                },
                onDestroy = { InAppAdMobInterstitial.destroy() }
            ),
            AdType(
                "Rewarded",
                onCreate = { activity, _, _ ->
                    InAppAdMobRewarded.create(
                        activity,
                        "ca-app-pub-1875909575462531/1908212572",
                        "imp-prebid-video-rewarded-320-480","response-prebid-video-rewarded-320-480"
                    )
                },
                onDestroy = { InAppAdMobRewarded.destroy() }
            ),
            AdType(
                "Native",
                onCreate = { _, wrapper, _ ->
                    // TODO: Problems with ids (current example's type is not Native)
                    InAppAdMobNative.create(
                        wrapper,
                        "ca-app-pub-1875909575462531/9720985924",
                        "imp-prebid-banner-native-styles","response-prebid-banner-native-styles"
                    )
                },
                onDestroy = {
                    InAppAdMobNative.destroy()
                }
            )
        )
    )

    fun usePrebidServer() {
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
        PrebidMobile.setPrebidServerHost(Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction"))
    }

//    private fun useTestServer() {
//        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
//        PrebidMobile.setPrebidServerHost(Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction"))
//    }
//
//    private fun useAppNexusServer() {
//        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0")
//        PrebidMobile.setPrebidServerHost(Host.APPNEXUS)
//    }

}