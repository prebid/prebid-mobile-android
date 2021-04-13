package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;

import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.nativeads.MoPubNative;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MoPubNativeAdUnitTest {
    private Context mContext;
    private MoPubNativeAdUnit mMoPubNativeAdUnit;
    private MoPubNative mMoPubNative;
    @Mock
    private BidLoader mMockBidLoader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mMoPubNative = new MoPubNative(mContext, "", mock(MoPubNative.MoPubNativeNetworkListener.class));
        PrebidRenderingSettings.setAccountId("id");
        mMoPubNativeAdUnit = new MoPubNativeAdUnit(mContext, "configId", mock(NativeAdConfiguration.class));
        WhiteBox.setInternalState(mMoPubNativeAdUnit, "mBidLoader", mMockBidLoader);
    }

    @After
    public void cleanup() {
        PrebidRenderingSettings.setAccountId(null);
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForNative() {
        mMoPubNativeAdUnit.initAdConfig("config", null);
        AdConfiguration adConfiguration = mMoPubNativeAdUnit.mAdUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(AdConfiguration.AdUnitIdentifierType.NATIVE, adConfiguration.getAdUnitIdentifierType());
    }

    @Test
    public void whenIsMopubViewAndMoPubBannerViewPassed_ReturnTrue() {
        assertTrue(mMoPubNativeAdUnit.isAdObjectSupported(mMoPubNative));
    }

    @Test
    public void whenIsMopubViewAndAnyObjectPassed_ReturnFalse() {
        MoPubInterstitial moPubInterstitial = new MoPubInterstitial((Activity) mContext, "");
        assertFalse(mMoPubNativeAdUnit.isAdObjectSupported(moPubInterstitial));
    }
}