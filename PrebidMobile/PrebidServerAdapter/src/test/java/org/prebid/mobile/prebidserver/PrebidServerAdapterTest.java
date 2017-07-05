package org.prebid.mobile.prebidserver;


import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.core.AdUnit;
import org.prebid.mobile.core.BannerAdUnit;
import org.prebid.mobile.core.InterstitialAdUnit;
import org.prebid.mobile.prebidserver.internal.Settings;
import org.prebid.mobile.unittestutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class PrebidServerAdapterTest extends BaseSetup {

    @Test
    public void testPostDataGeneration() {
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", "12345");
        bannerAdUnit.addSize(320, 50);
        adUnits.add(bannerAdUnit);
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("interstitial", "23456");
        adUnits.add(interstitialAdUnit);
        JSONObject postData = adapter.getPostData(activity, adUnits);
        assertTrue(postData.has(Settings.REQUEST_CACHE_MARKUP));
        assertTrue(postData.has(Settings.REQUEST_SORT_BIDS));
        assertTrue(postData.has(Settings.REQUEST_TID));
        assertTrue(postData.has(Settings.REQUEST_AD_UNITS));
        assertTrue(postData.has(Settings.REQUEST_DEVICE));
        assertTrue(postData.has(Settings.REQUEST_APP));
        assertTrue(postData.has(Settings.REQUEST_USER));
    }
}
