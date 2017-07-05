package org.prebid.mobile.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.unittestutils.TestConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class AdUnitTest {
    //region AdUnit general test
    @Test
    public void testAuctionIDGeneration() {
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        String id = adUnit.getAuctionId();
        assertNotNull(id);
        adUnit.generateNewAuctionId();
        assertNotSame(id, adUnit.getAuctionId());
    }
    //endregion

    //region BannerAdUnit tests
    @Test
    public void testBannerAdCreation() {
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        assertEquals(AdType.BANNER, adUnit.getAdType());
        assertEquals(TestConstants.bannerAdUnit1, adUnit.getCode());
        assertEquals(TestConstants.configID1, adUnit.getConfigId());
    }


    @Test
    public void testBannerAddSize() {
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit.addSize(320, 50);
        ArrayList<AdSize> sizes = adUnit.getSizes();
        assertNotNull(sizes);
        assertEquals(1, sizes.size());
        AdSize size = sizes.get(0);
        assertEquals(320, size.getWidth());
        assertEquals(50, size.getHeight());
    }

    @Test
    public void testEquals() {
        BannerAdUnit adUnit1 = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        BannerAdUnit adUnit2 = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        assertEquals(adUnit1, adUnit2);
    }
    //endregion

    //region InterstitialAdUnit tests
    @Test
    public void testInterstitialAdType() {
        InterstitialAdUnit adUnit = new InterstitialAdUnit(TestConstants.interstitialAdUnit, TestConstants.configID2);
        assertEquals(AdType.INTERSTITIAL, adUnit.getAdType());
        assertEquals(TestConstants.interstitialAdUnit, adUnit.getCode());
        assertEquals(TestConstants.configID2, adUnit.getConfigId());
    }
    //endregion
}
