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

package org.prebid.mobile.api.mediation;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.config.MockMediationUtils;
import org.prebid.mobile.rendering.bidding.display.MediationBannerView;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationBannerAdUnitTest {

    private Context context;
    private MediationBannerAdUnit mediationBannerAdUnit;
    @Mock
    private ScreenStateReceiver mockScreenStateReceiver;
    @Mock
    private BidLoader mockBidLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        context = Robolectric.buildActivity(Activity.class).create().get();
        PrebidMobile.setPrebidServerAccountId("id");
        mediationBannerAdUnit = new MediationBannerAdUnit(context, "config", mock(AdSize.class), new MockMediationUtils());

        WhiteBox.setInternalState(mediationBannerAdUnit, "bidLoader", mockBidLoader);
        WhiteBox.setInternalState(mediationBannerAdUnit, "screenStateReceiver", mockScreenStateReceiver);

        assertEquals(AdPosition.UNDEFINED.getValue(), mediationBannerAdUnit.getAdPosition().getValue());
    }

    @After
    public void cleanup() {
        PrebidMobile.setPrebidServerAccountId(null);
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForBanner() {
        AdSize adSize = new AdSize(1, 2);
        mediationBannerAdUnit.initAdConfig("config", adSize);
        AdUnitConfiguration adConfiguration = mediationBannerAdUnit.adUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.BANNER), adConfiguration.getAdFormats());
        assertTrue(adConfiguration.getSizes().contains(adSize));
    }

    @Test
    public void whenSetRefreshInterval_ChangeRefreshIntervalInAdConfig() {
        assertEquals(30_000, mediationBannerAdUnit.adUnitConfig.getAutoRefreshDelay());
        mediationBannerAdUnit.setRefreshInterval(15);
        assertEquals(30_000, mediationBannerAdUnit.adUnitConfig.getAutoRefreshDelay());
    }

    @Test
    public void whenDestroy_UnregisterReceiver() {
        mediationBannerAdUnit.destroy();

        verify(mockScreenStateReceiver, times(1)).unregister();
    }

    @Test
    public void whenStopRefresh_BidLoaderCancelRefresh() {
        mediationBannerAdUnit.stopRefresh();

        verify(mockBidLoader, times(1)).cancelRefresh();
    }

    @Test
    public void whenResumeRefresh_BidLoaderSetupsNewTimer() {
        mediationBannerAdUnit.resumeRefresh();

        verify(mockBidLoader, times(1)).setupRefreshTimer();
    }

    @Test
    public void adUnitFormatsDefaultToBanner() {
        assertEquals(EnumSet.of(AdUnitFormat.BANNER), mediationBannerAdUnit.getAdUnitFormats());
        assertEquals(EnumSet.of(AdFormat.BANNER), mediationBannerAdUnit.adUnitConfig.getAdFormats());
    }

    @Test
    public void setAdUnitFormats_video_requestsVideoOnly() {
        mediationBannerAdUnit.setAdUnitFormats(EnumSet.of(AdUnitFormat.VIDEO));

        assertEquals(EnumSet.of(AdUnitFormat.VIDEO), mediationBannerAdUnit.getAdUnitFormats());
        assertEquals(EnumSet.of(AdFormat.VAST), mediationBannerAdUnit.adUnitConfig.getAdFormats());
    }

    @Test
    public void setAdUnitFormats_multiformat_requestsBannerAndVideo() {
        mediationBannerAdUnit.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO));

        assertEquals(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), mediationBannerAdUnit.getAdUnitFormats());
        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.VAST), mediationBannerAdUnit.adUnitConfig.getAdFormats());
    }

    @Test
    public void setAdUnitFormats_nullOrEmpty_keepsCurrentValue() {
        mediationBannerAdUnit.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO));

        mediationBannerAdUnit.setAdUnitFormats(null);
        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.VAST), mediationBannerAdUnit.adUnitConfig.getAdFormats());

        mediationBannerAdUnit.setAdUnitFormats(EnumSet.noneOf(AdUnitFormat.class));
        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.VAST), mediationBannerAdUnit.adUnitConfig.getAdFormats());
    }

    //region ================= Auto refresh vs. video creatives
    @Test
    public void canPerformRefreshWhileVideoIsPlaying_SkipTheTick() throws Exception {
        givenMediatedAdViewWithPrebidVideo(true);

        assertFalse(getBidRefreshListener().canPerformRefresh());
    }

    @Test
    public void canPerformRefreshAfterVideoFinished_AllowTheTick() throws Exception {
        givenMediatedAdViewWithPrebidVideo(false);
        when(mockScreenStateReceiver.isScreenOn()).thenReturn(true);

        assertTrue(getBidRefreshListener().canPerformRefresh());
    }

    @Test
    public void canPerformRefreshAfterAdFailed_AllowTheTickEvenIfVideoReportsPlaying() throws Exception {
        givenMediatedAdViewWithPrebidVideo(true);
        WhiteBox.field(MediationBannerAdUnit.class, "adFailed").set(mediationBannerAdUnit, true);

        assertTrue(getBidRefreshListener().canPerformRefresh());
    }

    @Test
    public void isVideoPlayingWithoutPrebidViewInTheHierarchy_False() {
        // Another demand partner won in the mediation SDK, so there is no Prebid creative to guard.
        FrameLayout mediatedAdView = new FrameLayout(context);
        mediatedAdView.addView(new FrameLayout(context));
        givenMediationDelegateReturning(mediatedAdView);

        assertFalse(mediationBannerAdUnit.isVideoPlaying());
    }

    @Test
    public void isVideoPlayingWithoutAdView_False() {
        givenMediationDelegateReturning(null);

        assertFalse(mediationBannerAdUnit.isVideoPlaying());
    }

    @Test
    public void isVideoPlayingWithDefaultDelegate_False() {
        // A custom delegate that does not implement getAdView() opts out of the gate.
        WhiteBox.setInternalState(mediationBannerAdUnit, "mediationDelegate", new MockMediationUtils());

        assertFalse(mediationBannerAdUnit.isVideoPlaying());
    }

    /**
     * Mirrors what the adapters build: the mediation SDK's ad view wraps the
     * {@link MediationBannerView} the Prebid adapter rendered into.
     */
    private void givenMediatedAdViewWithPrebidVideo(boolean isVideoPlaying) {
        MediationBannerView prebidView = mock(MediationBannerView.class);
        when(prebidView.isVideoPlaying()).thenReturn(isVideoPlaying);

        FrameLayout mediatedAdView = new FrameLayout(context);
        FrameLayout intermediateContainer = new FrameLayout(context);
        mediatedAdView.addView(intermediateContainer);
        intermediateContainer.addView(prebidView);

        givenMediationDelegateReturning(mediatedAdView);
    }

    private void givenMediationDelegateReturning(@Nullable View adView) {
        PrebidMediationDelegate delegate = mock(PrebidMediationDelegate.class);
        when(delegate.getAdView()).thenReturn(adView);
        when(delegate.canPerformRefresh()).thenReturn(true);

        WhiteBox.setInternalState(mediationBannerAdUnit, "mediationDelegate", delegate);
    }

    private BidLoader.BidRefreshListener getBidRefreshListener() throws Exception {
        // initBidLoader() builds a BidLoader and registers the refresh gate on it.
        WhiteBox.method(MediationBannerAdUnit.class, "initBidLoader").invoke(mediationBannerAdUnit);

        final BidLoader bidLoader = (BidLoader) WhiteBox
                .field(MediationBaseAdUnit.class, "bidLoader")
                .get(mediationBannerAdUnit);
        return (BidLoader.BidRefreshListener) WhiteBox
                .field(BidLoader.class, "bidRefreshListener")
                .get(bidLoader);
    }
    //endregion ============== Auto refresh vs. video creatives

    @Test
    public void setAdPosition_EqualsGetAdPosition() {
        mediationBannerAdUnit.setAdPosition(null);
        assertEquals(AdPosition.UNDEFINED, mediationBannerAdUnit.getAdPosition());

        mediationBannerAdUnit.setAdPosition(AdPosition.FOOTER);
        assertEquals(AdPosition.FOOTER, mediationBannerAdUnit.getAdPosition());

        mediationBannerAdUnit.setAdPosition(AdPosition.HEADER);
        assertEquals(AdPosition.HEADER, mediationBannerAdUnit.getAdPosition());

        mediationBannerAdUnit.setAdPosition(AdPosition.SIDEBAR);
        assertEquals(AdPosition.SIDEBAR, mediationBannerAdUnit.getAdPosition());

        mediationBannerAdUnit.setAdPosition(AdPosition.UNKNOWN);
        assertEquals(AdPosition.UNKNOWN, mediationBannerAdUnit.getAdPosition());
    }

}