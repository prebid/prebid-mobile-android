package org.prebid.mobile.core;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.core.mockobjects.MockServer;
import org.prebid.mobile.unittestutils.BaseSetup;
import org.prebid.mobile.unittestutils.TestConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class PrebidInitTest extends BaseSetup {

    @Before
    public void setUp() {
        super.setup();
        setTestServer(MockServer.class.getName());
    }

    private void setTestServer(String serverName) {
        try {
            Field prebidServerField = Prebid.class.getDeclaredField("PREBID_SERVER");
            prebidServerField.setAccessible(true);
            prebidServerField.set(null, serverName);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    // Check if the ad units passed in are stored correctly in the BidManager after init.
    @Test
    public void testNormalInit() throws Exception {
        //Create a list of adUnits test
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        BannerAdUnit adUnit1 = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit1.addSize(320, 50);
        BannerAdUnit adUnit2 = new BannerAdUnit(TestConstants.bannerAdUnit2, TestConstants.configID1);
        adUnit2.addSize(320, 200);
        InterstitialAdUnit adUnit3 = new InterstitialAdUnit(TestConstants.interstitialAdUnit, TestConstants.configID3);
        adUnits.add(adUnit1);
        adUnits.add(adUnit2);
        adUnits.add(adUnit3);
        // Init
        Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId, Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        // Assertion
        assertEquals(adUnit1, BidManager.getAdUnitByCode(TestConstants.bannerAdUnit1));
        assertEquals(adUnit2, BidManager.getAdUnitByCode(TestConstants.bannerAdUnit2));
        assertEquals(adUnit3, BidManager.getAdUnitByCode(TestConstants.interstitialAdUnit));
    }

    // Exceptions Testing
    @Test
    public void testInitWithNullContext() {
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        BannerAdUnit adUnit1 = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit1.addSize(320, 50);
        try {
            Prebid.init(null, adUnits, TestConstants.accountId, Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        } catch (Exception e) {
            Assert.assertEquals(PrebidException.PrebidError.NULL_CONTEXT.getDetailMessage(), e.getMessage());
        }
    }

    @Test
    public void testInitWithEmptyAdUnits() {
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        try {
            Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId, Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        } catch (Exception e) {
            Assert.assertEquals(PrebidException.PrebidError.EMPTY_ADUNITS.getDetailMessage(), e.getMessage());
        }
    }

    @Test
    public void testInitBannerNoSizeException() {
        BannerAdUnit adUnit = new BannerAdUnit("NoSize", TestConstants.configID1);
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        try {
            Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId, Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        } catch (Exception e) {
            Assert.assertEquals(PrebidException.PrebidError.BANNER_AD_UNIT_NO_SIZE.getDetailMessage(), e.getMessage());
        }
    }

    @Test
    public void testUnableToInitDemandAdapterException() {
        setTestServer("random value");
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit.addSize(320, 50);
        adUnits.add(adUnit);
        try {
            Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId, Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        } catch (Exception e) {
            Assert.assertEquals(PrebidException.PrebidError.UNABLE_TO_INITIALIZE_DEMAND_SOURCE.getDetailMessage(), e.getMessage());
        }
    }

    @Test
    public void testUnableToInitNullHostException() {
        setTestServer("random value");
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit.addSize(320, 50);
        adUnits.add(adUnit);
        try {
            Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId, Prebid.AdServer.DFP, null);
        } catch (Exception e) {
            Assert.assertEquals(PrebidException.PrebidError.NULL_HOST.getDetailMessage(), e.getMessage());
        }
    }
}
