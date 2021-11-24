/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.config.MockMediationUtils;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationBannerAdUnitTest {
    private Context mContext;
    private MediationBannerAdUnit mMediationBannerAdUnit;
    @Mock
    private ScreenStateReceiver mMockScreenStateReceiver;
    @Mock
    private BidLoader mMockBidLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        PrebidRenderingSettings.setAccountId("id");
        mMediationBannerAdUnit = new MediationBannerAdUnit(mContext, "config", mock(AdSize.class), new MockMediationUtils());

        WhiteBox.setInternalState(mMediationBannerAdUnit, "mBidLoader", mMockBidLoader);
        WhiteBox.setInternalState(mMediationBannerAdUnit, "mScreenStateReceiver", mMockScreenStateReceiver);

        assertEquals(BannerAdPosition.UNDEFINED.getValue(), mMediationBannerAdUnit.getAdPosition().getValue());
    }

    @After
    public void cleanup() {
        PrebidRenderingSettings.setAccountId(null);
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForBanner() {
        AdSize adSize = new AdSize(1, 2);
        mMediationBannerAdUnit.initAdConfig("config", adSize);
        AdConfiguration adConfiguration = mMediationBannerAdUnit.mAdUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(AdConfiguration.AdUnitIdentifierType.BANNER, adConfiguration.getAdUnitIdentifierType());
        assertTrue(adConfiguration.getAdSizes().contains(adSize));
    }

    @Test
    public void whenSetRefreshInterval_ChangeRefreshIntervalInAdConfig() {
        assertEquals(60000, mMediationBannerAdUnit.mAdUnitConfig.getAutoRefreshDelay());
        mMediationBannerAdUnit.setRefreshInterval(15);
        assertEquals(15000, mMediationBannerAdUnit.mAdUnitConfig.getAutoRefreshDelay());
    }

    @Test
    public void whenDestroy_UnregisterReceiver() {
        mMediationBannerAdUnit.destroy();

        verify(mMockScreenStateReceiver, times(1)).unregister();
    }

    @Test
    public void whenStopRefresh_BidLoaderCancelRefresh() {
        mMediationBannerAdUnit.stopRefresh();

        verify(mMockBidLoader, times(1)).cancelRefresh();
    }

    @Test
    public void whenSetNativeAdConfiguration_ConfigAssignedToAdConfiguration() {
        AdConfiguration mockConfiguration = mock(AdConfiguration.class);
        WhiteBox.setInternalState(mMediationBannerAdUnit, "mAdUnitConfig", mockConfiguration);
        mMediationBannerAdUnit.setNativeAdConfiguration(mock(NativeAdConfiguration.class));
        verify(mockConfiguration).setNativeAdConfiguration(any(NativeAdConfiguration.class));
    }

    @Test
    public void setAdPosition_EqualsGetAdPosition() {
        mMediationBannerAdUnit.setAdPosition(null);
        assertEquals(BannerAdPosition.UNDEFINED, mMediationBannerAdUnit.getAdPosition());

        mMediationBannerAdUnit.setAdPosition(BannerAdPosition.FOOTER);
        assertEquals(BannerAdPosition.FOOTER, mMediationBannerAdUnit.getAdPosition());

        mMediationBannerAdUnit.setAdPosition(BannerAdPosition.HEADER);
        assertEquals(BannerAdPosition.HEADER, mMediationBannerAdUnit.getAdPosition());

        mMediationBannerAdUnit.setAdPosition(BannerAdPosition.SIDEBAR);
        assertEquals(BannerAdPosition.SIDEBAR, mMediationBannerAdUnit.getAdPosition());

        mMediationBannerAdUnit.setAdPosition(BannerAdPosition.UNKNOWN);
        assertEquals(BannerAdPosition.UNKNOWN, mMediationBannerAdUnit.getAdPosition());
    }
}