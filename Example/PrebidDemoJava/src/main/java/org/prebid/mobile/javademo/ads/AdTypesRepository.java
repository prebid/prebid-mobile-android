package org.prebid.mobile.javademo.ads;

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.javademo.ads.gam.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdTypesRepository {

    public static Map<String, List<AdType>> get() {
        Map<String, List<AdType>> result = new HashMap<>();

        result.put(
            "Google Ad Manager (Rubicon)",
            Arrays.asList(
                new AdType(
                    "Banner 300x250",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("1001-rubicon-300x250");
                        GamBanner.create(
                            wrapper,
                            "/5300653/pavliuchyk_test_adunit_1x1_puc",
                            "1001-1",
                            300, 250,
                            autoRefreshTime
                        );
                    },
                    GamBanner::destroy
                ),
                new AdType(
                    "Interstitial",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("1001-rubicon-300x250");
                        GamInterstitial.create(
                            activity,
                            "/5300653/pavliuchyk_test_adunit_1x1_puc",
                            "1001-1",
                            autoRefreshTime
                        );
                    },
                    GamInterstitial::destroy
                ),
                new AdType(
                    "Video Banner",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("sample_video_response");
                        GamVideoBanner.create(
                            wrapper,
                            "/5300653/test_adunit_vast_pavliuchyk",
                            "1001-1",
                            autoRefreshTime
                        );
                    },
                    GamVideoBanner::destroy
                ),
                new AdType(
                    "Video Interstitial",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("sample_video_response");
                        GamVideoInterstitial.create(
                            activity,
                            "/5300653/test_adunit_vast_pavliuchyk",
                            "1001-1",
                            autoRefreshTime
                        );
                    },
                    GamVideoInterstitial::destroy
                ),
                new AdType(
                    "Rewarded",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("sample_video_response");
                        GamRewarded.create(
                            activity,
                            "/5300653/test_adunit_vast_rewarded-video_pavliuchyk",
                            "1001-1"
                        );
                    },
                    GamRewarded::destroy
                ),
                new AdType(
                    "Video Instream",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("sample_video_response");
                        GamVideoInstream.create(
                            wrapper,
                            "/5300653/test_adunit_vast_pavliuchyk",
                            "1001-1"
                        );
                    },
                    GamVideoInstream::destroy
                )
            )
        );

        result.put(
            "Google Ad Manager (AWS)",
            Arrays.asList(
                new AdType(
                    "Banner 320x50",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-banner-320-50");
                        GamBanner.create(
                            wrapper,
                            "/21808260008/prebid_demo_app_original_api_banner",
                            "imp-prebid-banner-320-50",
                            320, 50,
                            autoRefreshTime
                        );
                    },
                    GamBanner::destroy
                ),
                new AdType(
                    "Banner 300x250",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-banner-300-250");
                        GamBanner.create(
                            wrapper,
                            "/21808260008/prebid_demo_app_original_api_banner_300x250_order",
                            "imp-prebid-banner-300-250",
                            300, 250,
                            autoRefreshTime
                        );
                    },
                    GamBanner::destroy
                ),
                new AdType(
                    "Interstitial",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-display-interstitial-320-480");
                        GamInterstitial.create(
                            activity,
                            "/21808260008/prebid-demo-app-original-api-display-interstitial",
                            "imp-prebid-display-interstitial-320-480",
                            autoRefreshTime
                        );
                    },
                    GamInterstitial::destroy
                ),
                new AdType(
                    "Video Banner",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-video-interstitial-320-480");
                        PrebidMobile.setStoredAuctionResponse("response-prebid-video-outstream");
                        GamVideoBanner.create(
                            wrapper,
                            "/21808260008/prebid_oxb_outstream_video_reandom",
                            "imp-prebid-video-outstream",
                            autoRefreshTime
                        );
                    },
                    GamVideoBanner::destroy
                ),
                new AdType(
                    "Video Interstitial",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-video-interstitial-320-480-original-api");
                        GamVideoInterstitial.create(
                            activity,
                            "/21808260008/prebid-demo-app-original-api-video-interstitial",
                            "imp-prebid-video-interstitial-320-480",
                            autoRefreshTime
                        );
                    },
                    GamVideoInterstitial::destroy
                ),
                new AdType(
                    "Rewarded",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-video-rewarded-320-480-without-end-card");
                        GamRewarded.create(
                            activity,
                            "/21808260008/prebid_oxb_rewarded_video_static",
                            "imp-prebid-video-rewarded-320-480-without-end-card"
                        );
                    },
                    GamRewarded::destroy
                ),
                new AdType(
                    "Native In App",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-banner-native-styles");
                        GamNativeInApp.create(
                            wrapper,
                            "/21808260008/apollo_custom_template_native_ad_unit",
                            "imp-prebid-banner-native-styles",
                            "11934135"
                        );
                    },
                    GamNativeInApp::destroy
                ),
                new AdType(
                    "Native In Banner",
                    (activity, wrapper, autoRefreshTime) -> {
                        PrebidMobile.setStoredAuctionResponse("response-prebid-banner-native-styles");
                        GamNativeInBanner.create(
                            wrapper,
                            "/21808260008/unified_native_ad_unit",
                            "imp-prebid-banner-native-styles",
                            autoRefreshTime
                        );
                    },
                    GamNativeInBanner::destroy
                )
            )
        );

        return result;
    }

}
