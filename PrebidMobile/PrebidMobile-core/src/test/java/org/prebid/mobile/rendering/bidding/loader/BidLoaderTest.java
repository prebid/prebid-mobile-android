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

package org.prebid.mobile.rendering.bidding.loader;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.reflection.sdk.PrebidMobileReflection;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.networking.modelcontrollers.BidRequester;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.utils.helpers.RefreshTimerTask;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidLoaderTest {

    private BidLoader bidLoader;
    @Mock
    private AdUnitConfiguration mockAdConfiguration;
    @Mock
    private BidRequesterListener bidRequesterListener;
    @Mock
    private BidRequester mockRequester;
    @Mock
    private RefreshTimerTask mockTimerTask;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mockAdConfiguration.isAdType(any(AdFormat.class))).thenReturn(true);
        when(mockAdConfiguration.getAutoRefreshDelay()).thenReturn(60000);
        bidLoader = createBidLoader(mockAdConfiguration, bidRequesterListener);
    }

    @After
    public void clean() {

    }

    @Test
    public void whenLoadAndNoContext_NoStartAdRequestCall() {
        bidLoader = createBidLoader(null, bidRequesterListener);
        bidLoader.load();
        verify(mockRequester, never()).startAdRequest();
    }

    @Test
    public void whenLoadAndNoAdConfiguration_NoStartAdRequestCall() {
        PrebidContextHolder.clearContext();

        bidLoader = createBidLoader(mockAdConfiguration, bidRequesterListener);
        bidLoader.load();
        verify(mockRequester, never()).startAdRequest();
    }

    @Test
    public void whenLoadAndNoListener_NoStartAdRequestCall() {
        bidLoader = createBidLoader(mockAdConfiguration, null);
        bidLoader.load();
        verify(mockRequester, never()).startAdRequest();
    }

    @Test
    public void whenFreshLoadAndAdUnitConfigPassed_CallStartAdRequest() {
        PrebidMobileReflection.setFlagsThatSdkIsInitialized();

        bidLoader.load();
        verify(mockRequester).startAdRequest();
    }

    @Test
    public void whenFreshLoadButSdkIsNotInitialized_DoNotStartAdRequest() {
        PrebidMobileReflection.setFlagsThatSdkIsNotInitialized();

        bidLoader.load();
        verify(mockRequester, never()).startAdRequest();
    }

    @Test
    public void whenLoadAndCurrentlyLoading_NoStartAdRequestCall() {
        WhiteBox.setInternalState(bidLoader, "currentlyLoading", new AtomicBoolean(true));
        bidLoader.load();
        verify(mockRequester, never()).startAdRequest();
    }

    @Test
    public void whenDestroy_RequesterAndTimerTaskDestroyed() throws IllegalAccessException {
        WhiteBox.field(BidLoader.class, "bidRequester").set(bidLoader, mockRequester);
        bidLoader.destroy();
        verify(mockRequester).destroy();
        verify(mockTimerTask).cancelRefreshTimer();
        verify(mockTimerTask).destroy();
    }

    @Test
    public void whenCancelRefresh_CancelRefreshTimerTask() {
        bidLoader.cancelRefresh();
        verify(mockTimerTask).cancelRefreshTimer();
    }

    private BidLoader createBidLoader(AdUnitConfiguration adConfiguration, BidRequesterListener requestListener) {
        BidLoader bidLoader = new BidLoader(adConfiguration, requestListener);
        WhiteBox.setInternalState(bidLoader, "bidRequester", mockRequester);
        WhiteBox.setInternalState(bidLoader, "refreshTimerTask", mockTimerTask);
        return bidLoader;
    }
}