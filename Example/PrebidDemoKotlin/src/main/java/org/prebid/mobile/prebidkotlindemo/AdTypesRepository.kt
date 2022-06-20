package org.prebid.mobile.prebidkotlindemo

import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.prebidkotlindemo.ads.*
import org.prebid.mobile.prebidkotlindemo.ads.inapp.*
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobBanner
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobNative
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobRewarded
import org.prebid.mobile.prebidkotlindemo.ads.inappgam.*
import org.prebid.mobile.prebidkotlindemo.ads.inappmax.*
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
                        "/21808260008/prebid_demo_app_original_api_banner",
                        "imp-prebid-banner-320-50",
                        "response-prebid-banner-320-50"
                    )
                },
                onDestroy = { GamBanner.destroy() }
            ),
            AdType("Banner Multisize", onCreate = { _, wrapper, autorefreshTime ->
                GamBanner.create(
                    wrapper, autorefreshTime, 320, 50,
                    "/21808260008/prebid_demo_app_original_api_banner_multisize",
                    "imp-prebid-banner-multisize",
                    "response-prebid-banner-multisize"
                )
            }),
            AdType(
                "Banner 300x250",
                onCreate = { _, wrapper, autoRefreshTime ->
                    GamBanner.create(
                        wrapper, autoRefreshTime,
                        300, 250,
                        "/21808260008/prebid_demo_app_original_api_banner_300x250_order",
                        "imp-prebid-banner-300-250",
                        "response-prebid-banner-300-250"
                    )
                },
                onDestroy = { GamBanner.destroy() }
            ),
            AdType(
                "Native Ad",
                onCreate = { _, wrapper, autoRefreshTime ->
                    GamNative.create(
                        wrapper,
                        "/21808260008/unified_native_ad_unit",
                        "imp-prebid-banner-native-styles",
                        autoRefreshTime,
                        "response-prebid-banner-native-styles"
                    )
                },
                onDestroy = { GamNative.destroy() }
            ),
            AdType(
                "Display Interstitial",
                onCreate = { activity, _, autoRefreshTime ->
                    GamInterstitial.create(
                        activity, autoRefreshTime,
                        // TODO: Problem with ids
                        "/21808260008/prebid-demo-app-original-api-display-interstitial",
                        "imp-prebid-display-interstitial-320-480",
                        "response-prebid-display-interstitial-320-480"
                    )
                },
                onDestroy = { GamInterstitial.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { activity, _, _ ->
                    GamVideoInterstitial.create(
                        activity,
                        "/21808260008/prebid-demo-app-original-api-video-interstitial",
                        "imp-prebid-video-interstitial-320-480",
                        "response-prebid-video-interstitial-320-480-original-api"
                    )
                },
                onDestroy = { GamVideoInterstitial.destroy() }
            ),AdType(
                "Rewarded Interstitial",
                onCreate = { activity, _, _ ->
                    GamVideoRewarded.create(
                        activity,
                        "/21808260008/prebid-demo-app-original-api-video-interstitial",
                        "imp-prebid-video-rewarded-320-480",
                        "response-prebid-video-rewarded-320-480-original-api"
                    )
                },
                onDestroy = { GamVideoRewarded.destroy() }
            )
        ),

        "In-App" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "imp-prebid-banner-320-50",
                        "response-prebid-banner-320-50"
                    )
                },
                onDestroy = { InAppBanner.destroy() }
            ),
            AdType(
                "Banner Multisize",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        728, 90,
                        "imp-prebid-banner-multisize",
                        "response-prebid-banner-multisize"
                    )
                },
                onDestroy = { InAppBanner.destroy() }
            ),
            AdType(
                "Native Ad",
                onCreate = { activity, wrapper, _ ->
                    InAppNative.create(
                        "imp-prebid-banner-native-styles",
                        wrapper, activity,
                        "response-prebid-banner-native-styles"
                    )
                },
                onDestroy = { InAppNative.destroy() }
            ),
            AdType(
                "MRAID Resize",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "imp-prebid-mraid-resize",
                        "response-prebid-mraid-resize"
                    )
                },
                onDestroy = { GamBanner.destroy() }
            ),
            AdType(
                "MRAID Expand 1-part",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "imp-prebid-mraid-expand-1-part",
                        "response-prebid-mraid-expand-1-part"
                    )
                },
                onDestroy = { GamBanner.destroy() }
            ),
            AdType(
                "MRAID Resize with errors",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        300, 100,
                        "imp-prebid-mraid-resize-with-errors",
                        "response-prebid-mraid-resize-with-errors"
                    )
                },
                onDestroy = { GamBanner.destroy() }
            ),
            AdType(
                "Display Interstitial",
                onCreate = { context, _, _ ->
                    InAppInterstitial.create(
                        context,
                        30, 30,
                        "imp-prebid-display-interstitial-320-480",
                        "response-prebid-display-interstitial-320-480",
                        EnumSet.of(AdUnitFormat.DISPLAY)
                    )
                },
                onDestroy = { InAppInterstitial.destroy() }
            ),
            AdType(
                "Video Banner",
                onCreate = { _, wrapper, _ ->
                    InAppVideoBanner.create(
                        wrapper,
                        300, 250,
                        "imp-prebid-video-outstream",
                        "response-prebid-video-outstream"
                    )
                },
                onDestroy = { InAppVideoBanner.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { context, _, _ ->
                    InAppVideoInterstitial.create(
                        context,
                        "imp-prebid-video-interstitial-320-480",
                        "response-prebid-video-interstitial-320-480"
                    )
                },
                onDestroy = { InAppVideoInterstitial.destroy() }
            ),
            AdType(
                "Video Interstitial With End Card",
                onCreate = { context, _, _ ->
                    InAppVideoInterstitial.create(
                        context,
                        "imp-prebid-video-interstitial-320-480-with-end-card",
                        "response-prebid-video-interstitial-320-480-with-end-card"
                    )
                },
                onDestroy = { InAppVideoInterstitial.destroy() }
            ),
            AdType(
                "Multiformat Interstitial",
                onCreate = { context, _, _ ->
                    val storedAuctionResponses = listOf(
                        "response-prebid-video-interstitial-320-480",
                        "response-prebid-display-interstitial-320-480"
                    )
                    InAppInterstitial.create(
                        context, 30, 30,
                        "imp-prebid-video-interstitial-320-480",
                        storedAuctionResponses.random(),
                        EnumSet.of(AdUnitFormat.VIDEO, AdUnitFormat.DISPLAY)
                    )
                },
                onDestroy = { InAppVideoInterstitial.destroy() }
            ),
            AdType(
                "Rewarded Interstitial",
                onCreate = { context, _, _ ->
                    InAppRewardedInterstitial.create(
                        context,
                        "imp-prebid-video-rewarded-320-480",
                        "response-prebid-video-rewarded-320-480"
                    )
                },
                onDestroy = { InAppRewardedInterstitial.destroy() }
            ),
        ),

        "In-App + Google Ad Manager" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppGamBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "/21808260008/prebid_oxb_320x50_banner",
                        "imp-prebid-banner-320-50",
                        "response-prebid-banner-320-50"
                    )
                },
                onDestroy = { InAppGamBanner.destroy() }
            ),
            AdType(
                "Native Ad",
                onCreate = { _, wrapper, _ ->
                   InAppGamNative.create(
                       wrapper,
                       "/21808260008/apollo_custom_template_native_ad_unit",
                       "imp-prebid-banner-native-styles",
                       "11934135",
                       "response-prebid-banner-native-styles"
                   )
                },
                onDestroy = { InAppGamNative.destroy() }
            ),
            AdType(
                "Display Interstitial",
                onCreate = { context, _, _ ->
                    InAppGamInterstitial.create(
                        context,
                        30, 30,
                        "/21808260008/prebid_oxb_html_interstitial",
                        "imp-prebid-display-interstitial-320-480",
                        "response-prebid-display-interstitial-320-480"
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
                        "imp-prebid-video-outstream",
                        "response-prebid-video-outstream"
                    )
                },
                onDestroy = { InAppGamVideoBanner.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { activity, _, _ ->
                    InAppGamVideoInterstitial.create(
                        activity,
                        "/21808260008/prebid-demo-app-original-api-video-interstitial",
                        "imp-prebid-video-interstitial-320-480",
                        "response-prebid-video-interstitial-320-480"
                    )
                },
                onDestroy = { InAppGamVideoInterstitial.destroy() }
            ),
            AdType(
                "Rewarded Interstitial",
                onCreate = { activity, _, _ ->
                    InAppGamRewardedInterstitial.create(
                        activity,
                        "/21808260008/prebid-demo-app-original-api-video-interstitial",
                        "imp-prebid-video-rewarded-320-480",
                        "response-prebid-video-rewarded-320-480"
                    )
                },
                onDestroy = { InAppGamRewardedInterstitial.destroy() }
            )
        ),

        "In-App + AdMob" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { activity, wrapper, autoRefreshTime ->
                    InAppAdMobBanner.create(
                        activity, wrapper, autoRefreshTime,
                        320, 50,
                        "ca-app-pub-1875909575462531/3793078260",
                        "imp-prebid-banner-320-50",
                        "response-prebid-banner-320-50"
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
                        EnumSet.of(AdUnitFormat.DISPLAY),
                        "response-prebid-display-interstitial-320-480"
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
                        EnumSet.of(AdUnitFormat.VIDEO), "" +
                                "response-prebid-video-interstitial-320-480"
                    )
                },
                onDestroy = { InAppAdMobInterstitial.destroy() }
            ),
            AdType(
                "Rewarded Interstitial",
                onCreate = { activity, _, _ ->
                    InAppAdMobRewarded.create(
                        activity,
                        "ca-app-pub-1875909575462531/1908212572",
                        "imp-prebid-video-rewarded-320-480",
                        "response-prebid-video-rewarded-320-480"
                    )
                },
                onDestroy = { InAppAdMobRewarded.destroy() }
            ),
            AdType(
                "Native Ad",
                onCreate = { _, wrapper, _ ->
                    // TODO: Problems with ids (current example's type is not Native)
                    InAppAdMobNative.create(
                        wrapper,
                        "ca-app-pub-1875909575462531/9720985924",
                        "imp-prebid-banner-native-styles",
                        "response-prebid-banner-native-styles"
                    )
                },
                onDestroy = {
                    InAppAdMobNative.destroy()
                }
            )
        ),

        "In-App + Applovin MAX" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    PrebidMobile.setStoredAuctionResponse("response-prebid-banner-320-50")
                    InAppMaxBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        "3d8a0bcbb6d571d5",
                        "imp-prebid-banner-320-50"
                    )
                },
                onDestroy = { InAppMaxBanner.destroy() }
            ),
            AdType(
                "MREC",
                onCreate = { _, wrapper, autoRefreshTime ->
                    PrebidMobile.setStoredAuctionResponse("response-prebid-banner-300-250")
                    InAppMaxMrec.create(
                        wrapper, autoRefreshTime / 1000,
                        "550e6c2fe979a641",
                        "imp-prebid-banner-300-250"
                    )
                },
                onDestroy = { InAppMaxMrec.destroy() }
            ),
            AdType(
                "Interstitial",
                onCreate = { activity, _, _ ->
                    PrebidMobile.setStoredAuctionResponse("response-prebid-display-interstitial-320-480")
                    InAppMaxInterstitial.create(
                        activity,
                        "393697e649678807",
                        "imp-prebid-display-interstitial-320-480"
                    )
                },
                onDestroy = { InAppMaxInterstitial.destroy() }
            ),
            AdType(
                "Rewarded",
                onCreate = { activity, _, _ ->
                    PrebidMobile.setStoredAuctionResponse("response-prebid-video-rewarded-320-480")
                    InAppMaxRewarded.create(
                        activity,
                        "897f2fc59d617715",
                        "imp-prebid-video-rewarded-320-480"
                    )
                },
                onDestroy = { InAppMaxRewarded.destroy() }
            ),
            AdType(
                "Native",
                onCreate = { activity, wrapper, _ ->
                    PrebidMobile.setStoredAuctionResponse("response-prebid-banner-native-styles")
                    InAppMaxNative.create(
                        activity,
                        wrapper,
                        "f3bdfa9dd8da1c4d",
                        "imp-prebid-banner-native-styles"
                    )
                },
                onDestroy = { InAppMaxNative.destroy() }
            ),
        )
    )

    fun usePrebidServer() {
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
        PrebidMobile.setPrebidServerHost(Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction"))
    }

}