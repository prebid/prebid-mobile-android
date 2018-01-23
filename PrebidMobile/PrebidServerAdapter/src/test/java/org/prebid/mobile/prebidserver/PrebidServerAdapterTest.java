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

import static org.junit.Assert.assertEquals;
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

    @Test
    public void testRequestTimeOutMillis() {
        // assert default value
        assertEquals(500, Settings.connectionTimeOutMillis);
        // test setter
        Settings.setConnectionTimeOutMillis(1000);
        assertEquals(1000, Settings.getConnectionTimeOutMillis());
    }

    @Test
    public void testGetArrayListForBatchCall() throws Exception {
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        // pass in a single ad unit
        BannerAdUnit adUnit = new BannerAdUnit("B", "0");
        adUnit.addSize(300, 250);
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        ArrayList<ArrayList<AdUnit>> results = adapter.getAdUnitLists(adUnits);
        assertTrue(results.size() == 1);
        assertTrue(results.get(0).size() == 1);
        // pass in 13 ad units
        BannerAdUnit adUnit1 = new BannerAdUnit("B1", "1");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit2 = new BannerAdUnit("B2", "2");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit3 = new BannerAdUnit("B3", "3");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit4 = new BannerAdUnit("B4", "4");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit5 = new BannerAdUnit("B5", "5");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit6 = new BannerAdUnit("B6", "6");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit7 = new BannerAdUnit("B7", "7");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit8 = new BannerAdUnit("B8", "8");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit9 = new BannerAdUnit("B9", "9");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit10 = new BannerAdUnit("B10", "10");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit11 = new BannerAdUnit("B11", "11");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit12 = new BannerAdUnit("B12", "12");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit13 = new BannerAdUnit("B13", "13");
        adUnit.addSize(300, 250);
        adUnits.add(adUnit1);
        adUnits.add(adUnit2);
        adUnits.add(adUnit3);
        adUnits.add(adUnit4);
        adUnits.add(adUnit5);
        adUnits.add(adUnit6);
        adUnits.add(adUnit7);
        adUnits.add(adUnit8);
        adUnits.add(adUnit9);
        adUnits.add(adUnit10);
        adUnits.add(adUnit11);
        adUnits.add(adUnit12);
        adUnits.add(adUnit13);
        results = adapter.getAdUnitLists(adUnits);
        assertTrue(results.size() == 2);
        assertTrue(results.get(0).size() == 10);
        assertTrue(results.get(1).size() == 4);
        BannerAdUnit adUnit14 = new BannerAdUnit("B1", "1");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit15 = new BannerAdUnit("B2", "2");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit16 = new BannerAdUnit("B3", "3");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit17 = new BannerAdUnit("B4", "4");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit18 = new BannerAdUnit("B5", "5");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit19 = new BannerAdUnit("B6", "6");
        adUnit.addSize(300, 250);
        adUnits.add(adUnit14);
        adUnits.add(adUnit15);
        adUnits.add(adUnit16);
        adUnits.add(adUnit17);
        adUnits.add(adUnit18);
        adUnits.add(adUnit19);
        results = adapter.getAdUnitLists(adUnits);
        assertTrue(results.size() == 2);
        assertTrue(results.get(0).size() == 10);
        assertTrue(results.get(1).size() == 10);
    }
}
