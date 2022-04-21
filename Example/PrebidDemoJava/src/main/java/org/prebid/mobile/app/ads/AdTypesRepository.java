package org.prebid.mobile.app.ads;

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.app.ads.rubicon.*;
import org.prebid.mobile.app.ads.xandr.XandrGamNativeInApp;
import org.prebid.mobile.app.ads.xandr.XandrGamNativeInBanner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdTypesRepository {

    public static Map<String, List<AdType>> get() {
        Map<String, List<AdType>> result = new HashMap<>();

        result.put("Google Ad Manager (Rubicon)",
                Arrays.asList(
                        new AdType(
                                "Banner 300x250",
                                (activity, wrapper, autoRefreshTime) -> {
                                    PrebidMobile.setStoredAuctionResponse("1001-rubicon-300x250");
                                    RubiconGamBanner.create(
                                            wrapper,
                                            "/5300653/pavliuchyk_test_adunit_1x1_puc",
                                            "1001-1",
                                            300, 250,
                                            autoRefreshTime
                                    );
                                },
                                RubiconGamBanner::destroy
                        ),
                        new AdType(
                                "Interstitial",
                                (activity, wrapper, autoRefreshTime) -> {
                                    PrebidMobile.setStoredAuctionResponse("1001-rubicon-300x250");
                                    RubiconGamInterstitial.create(
                                            activity,
                                            "/5300653/pavliuchyk_test_adunit_1x1_puc",
                                            "1001-1",
                                            autoRefreshTime
                                    );
                                },
                                RubiconGamInterstitial::destroy
                        ),
                        new AdType(
                                "Video Banner",
                                (activity, wrapper, autoRefreshTime) -> {
                                    PrebidMobile.setStoredAuctionResponse("sample_video_response");
                                    RubiconGamVideoBanner.create(
                                            wrapper,
                                            "/5300653/test_adunit_vast_pavliuchyk",
                                            "1001-1",
                                            autoRefreshTime
                                    );
                                },
                                RubiconGamVideoBanner::destroy
                        ),
                        new AdType(
                                "Video Interstitial",
                                (activity, wrapper, autoRefreshTime) -> {
                                    PrebidMobile.setStoredAuctionResponse("sample_video_response");
                                    RubiconGamVideoInterstitial.create(
                                            activity,
                                            "/5300653/test_adunit_vast_pavliuchyk",
                                            "1001-1",
                                            autoRefreshTime
                                    );
                                },
                                RubiconGamVideoInterstitial::destroy
                        ),
                        new AdType(
                                "Rewarded",
                                (activity, wrapper, autoRefreshTime) -> {
                                    PrebidMobile.setStoredAuctionResponse("sample_video_response");
                                    RubiconGamRewarded.create(
                                            activity,
                                            "/5300653/test_adunit_vast_rewarded-video_pavliuchyk",
                                            "1001-1"
                                    );
                                },
                                RubiconGamRewarded::destroy
                        ),
                        new AdType(
                                "Video Instream",
                                (activity, wrapper, autoRefreshTime) -> {
                                    PrebidMobile.setStoredAuctionResponse("sample_video_response");
                                    RubiconGamVideoInstream.create(
                                            wrapper,
                                            "/5300653/test_adunit_vast_pavliuchyk",
                                            "1001-1"
                                    );
                                },
                                RubiconGamVideoInstream::destroy
                        ),
                        new AdType(
                                "Native In App",
                                (activity, wrapper, autoRefreshTime) -> {
                                    XandrGamNativeInApp.create(
                                            wrapper,
                                            "/19968336/Abhas_test_native_native_adunit",
                                            "25e17008-5081-4676-94d5-923ced4359d3"
                                    );
                                },
                                XandrGamNativeInApp::destroy
                        ),
                        new AdType(
                                "Native In Banner",
                                (activity, wrapper, autoRefreshTime) -> {
                                    XandrGamNativeInBanner.create(
                                            wrapper,
                                            "/19968336/Wei_Prebid_Native_Test",
                                            "03f3341f-1737-402c-bc7d-bc81dfebe9cf",
                                            autoRefreshTime
                                    );
                                },
                                XandrGamNativeInBanner::destroy
                        )
                )
        );

        result.put("Google Ad Manager (AWS)",
                Arrays.asList(
                        new AdType(
                                "Banner 320x50",
                                (activity, wrapper, autoRefreshTime) -> {

                                },
                                () -> {
                                }
                        ),
                        new AdType(
                                "Banner 300x250",
                                (activity, wrapper, autoRefreshTime) -> {

                                },
                                () -> {
                                }
                        )
                )
        );

        return result;
    }

}
