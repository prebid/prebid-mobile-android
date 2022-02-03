package org.prebid.mobile.prebidkotlindemo

import com.mopub.mobileads.MoPubView
import org.prebid.mobile.prebidkotlindemo.ads.GamBanner
import org.prebid.mobile.prebidkotlindemo.ads.GamInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.MoPubBanner
import org.prebid.mobile.prebidkotlindemo.ads.MoPubInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.inapp.*
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobBanner
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.inappadmob.InAppAdMobRewarded
import org.prebid.mobile.prebidkotlindemo.ads.inappgam.*
import org.prebid.mobile.prebidkotlindemo.ads.inappmopub.InAppMoPubBanner
import org.prebid.mobile.prebidkotlindemo.ads.inappmopub.InAppMoPubInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.inappmopub.InAppMoPubRewardedInterstitial
import org.prebid.mobile.prebidkotlindemo.ads.inappmopub.InAppMoPubVideoInterstitial

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
                        "625c6125-f19e-4d5b-95c5-55501526b2a4"
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
                        "6ace8c7d-88c0-4623-8117-75bc3f0a2e45"
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
                        "625c6125-f19e-4d5b-95c5-55501526b2a4"
                    )
                },
                onDestroy = { GamInterstitial.destroy() }
            )
        ),

        "MoPub" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    MoPubBanner.create(
                        wrapper, autoRefreshTime,
                        320, 50, MoPubView.MoPubAdSize.HEIGHT_50,
                        "42b99af979cd474ea32f497c044b5d71",
                        "625c6125-f19e-4d5b-95c5-55501526b2a4"
                    )
                },
                onDestroy = { MoPubBanner.destroy() }
            ),
            AdType(
                "Banner 300x250",
                onCreate = { _, wrapper, autoRefreshTime ->
                    MoPubBanner.create(
                        wrapper, autoRefreshTime,
                        300, 250, MoPubView.MoPubAdSize.HEIGHT_250,
                        // TODO: Problem with ids
                        "a935eac11acd416f92640411234fbba6",
                        "6ace8c7d-88c0-4623-8117-75bc3f0a2e45"
                    )
                },
                onDestroy = { MoPubBanner.destroy() }
            ),
            AdType(
                "Interstitial",
                onCreate = { activity, _, autoRefreshTime ->
                    MoPubInterstitial.create(
                        activity, autoRefreshTime,
                        // TODO: Problem with ids
                        "2829868d308643edbec0795977f17437",
                        "625c6125-f19e-4d5b-95c5-55501526b2a4"
                    )
                },
                onDestroy = { MoPubInterstitial.destroy() }
            )
        ),

        "In-App" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "50699c03-0910-477c-b4a4-911dbe2b9d42"
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
                        "5a4b8dcf-f984-4b04-9448-6529908d6cb6"
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
                        "9007b76d-c73c-49c6-b0a8-1c7890a84b33"
                    )
                },
                onDestroy = { InAppVideoBanner.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { context, _, _ ->
                    InAppVideoInterstitial.create(
                        context,
                        "12f58bc2-b664-4672-8d19-638bcc96fd5c"
                    )
                },
                onDestroy = { InAppVideoInterstitial.destroy() }
            ),
            AdType(
                "Rewarded Interstitial",
                onCreate = { context, _, _ ->
                    InAppRewardedInterstitial.create(
                        context,
                        "12f58bc2-b664-4672-8d19-638bcc96fd5c"
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
                        "50699c03-0910-477c-b4a4-911dbe2b9d42"
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
                        "5a4b8dcf-f984-4b04-9448-6529908d6cb6"
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
                        "9007b76d-c73c-49c6-b0a8-1c7890a84b33"
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
                        "28259226-68de-49f8-88d6-f0f2fab846e3"
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
                        "12f58bc2-b664-4672-8d19-638bcc96fd5c"
                    )
                },
                onDestroy = { InAppGamRewardedInterstitial.destroy() }
            )
        ),

        "In-App + MoPub" to listOf(
            AdType(
                "Banner 320x50",
                onCreate = { _, wrapper, autoRefreshTime ->
                    InAppMoPubBanner.create(
                        wrapper, autoRefreshTime / 1000,
                        320, 50,
                        "093eef131ec7455b9bda52b7eb456c51",
                        "50699c03-0910-477c-b4a4-911dbe2b9d42"
                    )
                },
                onDestroy = { InAppMoPubBanner.destroy() }
            ),
            AdType(
                "Interstitial",
                onCreate = { activity, _, _ ->
                    InAppMoPubInterstitial.create(
                        activity,
                        30, 30,
                        "d6cc98e81ef44d648bd93c79d372c451",
                        "5a4b8dcf-f984-4b04-9448-6529908d6cb6"
                    )
                },
                onDestroy = { InAppMoPubInterstitial.destroy() }
            ),
            AdType(
                "Video Interstitial",
                onCreate = { activity, _, _ ->
                    InAppMoPubVideoInterstitial.create(
                        activity,
                        "062a5be1c0764e84b45244ecd58b237f",
                        "28259226-68de-49f8-88d6-f0f2fab846e3"
                    )
                },
                onDestroy = { InAppMoPubVideoInterstitial.destroy() }
            ),
            AdType(
                "Rewarded Interstitial",
                onCreate = { activity, _, _ ->
                    InAppMoPubRewardedInterstitial.create(
                        activity,
                        "7c8fe21705a948c8a89dc6b496e8ad35",
                        "12f58bc2-b664-4672-8d19-638bcc96fd5c",
                        hashMapOf()
                    )
                },
                onDestroy = { InAppMoPubRewardedInterstitial.destroy() }
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
                        "50699c03-0910-477c-b4a4-911dbe2b9d42"
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
                        "5a4b8dcf-f984-4b04-9448-6529908d6cb6"
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
                        "12f58bc2-b664-4672-8d19-638bcc96fd5c"
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
                        "9007b76d-c73c-49c6-b0a8-1c7890a84b33"
                    )
                },
                onDestroy = { InAppAdMobRewarded.destroy() }
            )
        )

    )

}