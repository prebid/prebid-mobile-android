package org.prebid.mobile.javademo.testcases;

import com.google.common.collect.Lists;

import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiDisplayBanner300x250;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiDisplayBanner320x50;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiDisplayInterstitial;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiNativeInApp;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiNativeStyles;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiVideoBanner;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiVideoInStream;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiVideoInterstitial;
import org.prebid.mobile.javademo.activities.ads.gam.original.GamOriginalApiVideoRewarded;

import java.util.ArrayList;

public class TestCaseRepository {

    public static TestCase lastTestCase;

    public static ArrayList<TestCase> getList() {
        ArrayList<TestCase> result = Lists.newArrayList(
            new TestCase(
                R.string.gam_original_display_banner_320x50,
                AdFormat.DISPLAY_BANNER,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiDisplayBanner320x50.class
            ),
            new TestCase(
                R.string.gam_original_display_banner_300x250,
                AdFormat.DISPLAY_BANNER,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiDisplayBanner300x250.class
            ),
            new TestCase(
                R.string.gam_original_video_banner,
                AdFormat.VIDEO_BANNER,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiVideoBanner.class
            ),
            new TestCase(
                R.string.gam_original_display_interstitial,
                AdFormat.DISPLAY_INTERSTITIAL,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiDisplayInterstitial.class
            ),
            new TestCase(
                R.string.gam_original_video_interstitial,
                AdFormat.VIDEO_INTERSTITIAL,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiVideoInterstitial.class
            ),
            new TestCase(
                R.string.gam_original_video_rewarded,
                AdFormat.VIDEO_REWARDED,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiVideoRewarded.class
            ),
            new TestCase(
                R.string.gam_original_native_in_app,
                AdFormat.NATIVE,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiNativeInApp.class
            ),
            new TestCase(
                R.string.gam_original_native_styles,
                AdFormat.NATIVE,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiNativeStyles.class
            ),
            new TestCase(
                R.string.gam_original_in_stream,
                AdFormat.IN_STREAM_VIDEO,
                IntegrationKind.GAM_ORIGINAL,
                GamOriginalApiVideoInStream.class
            )
        );
        return result;
    }

}
