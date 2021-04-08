package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;

import com.apollo.test.utils.WhiteBox;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MoPubBannerAdUnitTest {
    private Context mContext;
    private MoPubBannerAdUnit mMopubBannerAdUnit;
    private MoPubView mMopubView;
    @Mock
    private ScreenStateReceiver mMockScreenStateReceiver;
    @Mock
    private BidLoader mMockBidLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mMopubView = new MoPubView(mContext);
        PrebidRenderingSettings.setAccountId("id");
        mMopubBannerAdUnit = new MoPubBannerAdUnit(mContext, "config", mock(AdSize.class));

        WhiteBox.setInternalState(mMopubBannerAdUnit, "mBidLoader", mMockBidLoader);
        WhiteBox.setInternalState(mMopubBannerAdUnit, "mScreenStateReceiver", mMockScreenStateReceiver);

        assertEquals(BannerAdPosition.UNDEFINED.getValue(), mMopubBannerAdUnit.getAdPosition().getValue());
    }

    @After
    public void cleanup() {
        PrebidRenderingSettings.setAccountId(null);
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForBanner() {
        AdSize adSize = new AdSize(1, 2);
        mMopubBannerAdUnit.initAdConfig("config", adSize);
        AdConfiguration adConfiguration = mMopubBannerAdUnit.mAdUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(AdConfiguration.AdUnitIdentifierType.BANNER, adConfiguration.getAdUnitIdentifierType());
        assertTrue(adConfiguration.getAdSizes().contains(adSize));
    }

    @Test
    public void whenIsMopubViewAndMoPubBannerViewPassed_ReturnTrue() {
        assertTrue(mMopubBannerAdUnit.isAdObjectSupported(mMopubView));
    }

    @Test
    public void whenIsMopubViewAndAnyObjectPassed_ReturnFalse() {
        MoPubInterstitial moPubInterstitial = new MoPubInterstitial((Activity) mContext, "");
        assertFalse(mMopubBannerAdUnit.isAdObjectSupported(moPubInterstitial));
    }

    @Test
    public void whenSetRefreshInterval_ChangeRefreshIntervalInAdConfig() {
        assertEquals(60000, mMopubBannerAdUnit.mAdUnitConfig.getAutoRefreshDelay());
        mMopubBannerAdUnit.setRefreshInterval(15);
        assertEquals(15000, mMopubBannerAdUnit.mAdUnitConfig.getAutoRefreshDelay());
    }

    @Test
    public void whenDestroy_UnregisterReceiver() {
        mMopubBannerAdUnit.destroy();

        verify(mMockScreenStateReceiver, times(1)).unregister();
    }

    @Test
    public void whenStopRefresh_BidLoaderCancelRefresh() {
        mMopubBannerAdUnit.stopRefresh();

        verify(mMockBidLoader, times(1)).cancelRefresh();
    }

    @Test
    public void whenSetNativeAdConfiguration_ConfigAssignedToAdConfiguration() {
        AdConfiguration mockConfiguration = mock(AdConfiguration.class);
        WhiteBox.setInternalState(mMopubBannerAdUnit, "mAdUnitConfig", mockConfiguration);
        mMopubBannerAdUnit.setNativeAdConfiguration(mock(NativeAdConfiguration.class));
        verify(mockConfiguration).setNativeAdConfiguration(any(NativeAdConfiguration.class));
    }

    @Test
    public void setAdPosition_EqualsGetAdPosition() {
        mMopubBannerAdUnit.setAdPosition(null);
        assertEquals(BannerAdPosition.UNDEFINED, mMopubBannerAdUnit.getAdPosition());

        mMopubBannerAdUnit.setAdPosition(BannerAdPosition.FOOTER);
        assertEquals(BannerAdPosition.FOOTER, mMopubBannerAdUnit.getAdPosition());

        mMopubBannerAdUnit.setAdPosition(BannerAdPosition.HEADER);
        assertEquals(BannerAdPosition.HEADER, mMopubBannerAdUnit.getAdPosition());

        mMopubBannerAdUnit.setAdPosition(BannerAdPosition.SIDEBAR);
        assertEquals(BannerAdPosition.SIDEBAR, mMopubBannerAdUnit.getAdPosition());

        mMopubBannerAdUnit.setAdPosition(BannerAdPosition.UNKNOWN);
        assertEquals(BannerAdPosition.UNKNOWN, mMopubBannerAdUnit.getAdPosition());
    }
}