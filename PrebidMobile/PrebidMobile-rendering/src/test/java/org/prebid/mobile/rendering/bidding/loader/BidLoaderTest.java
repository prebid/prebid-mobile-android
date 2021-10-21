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

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetImage;
import org.prebid.mobile.rendering.networking.modelcontrollers.BidRequester;
import org.prebid.mobile.rendering.utils.helpers.RefreshTimerTask;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidLoaderTest {

    private BidLoader mBidLoader;
    private Context mContext;
    @Mock
    private AdConfiguration mMockAdConfiguration;
    @Mock
    private BidRequesterListener mBidRequesterListener;
    @Mock
    private BidRequester mMockRequester;
    @Mock
    private RefreshTimerTask mMockTimerTask;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        when(mMockAdConfiguration.isAdType(any(AdConfiguration.AdUnitIdentifierType.class))).thenReturn(true);
        when(mMockAdConfiguration.getAutoRefreshDelay()).thenReturn(60000);
        mBidLoader = createBidLoader(mContext, mMockAdConfiguration, mBidRequesterListener);
    }

    @Test
    public void whenLoadAndNoContext_NoStartAdRequestCall() {
        mBidLoader = createBidLoader(mContext, null, mBidRequesterListener);
        mBidLoader.load();
        verify(mMockRequester, never()).startAdRequest();
    }

    @Test
    public void whenLoadAndNoAdConfiguration_NoStartAdRequestCall() {
        mBidLoader = createBidLoader(null, mMockAdConfiguration, mBidRequesterListener);
        mBidLoader.load();
        verify(mMockRequester, never()).startAdRequest();
    }

    @Test
    public void whenLoadAndNoListener_NoStartAdRequestCall() {
        mBidLoader = createBidLoader(mContext, mMockAdConfiguration, null);
        mBidLoader.load();
        verify(mMockRequester, never()).startAdRequest();
    }

    @Test
    public void whenFreshLoadAndAdUnitConfigPassed_CallStartAdRequest() {
        mBidLoader.load();
        verify(mMockRequester).startAdRequest();
    }

    @Test
    public void whenLoadAndCurrentlyLoading_NoStartAdRequestCall() {
        WhiteBox.setInternalState(mBidLoader, "mCurrentlyLoading", new AtomicBoolean(true));
        mBidLoader.load();
        verify(mMockRequester, never()).startAdRequest();
    }

    @Test
    public void whenDestroy_RequesterAndTimerTaskDestroyed() throws IllegalAccessException {
        WhiteBox.field(BidLoader.class, "mBidRequester").set(mBidLoader, mMockRequester);
        mBidLoader.destroy();
        verify(mMockRequester).destroy();
        verify(mMockTimerTask).cancelRefreshTimer();
        verify(mMockTimerTask).destroy();
    }

    @Test
    public void whenCancelRefresh_CancelRefreshTimerTask(){
        mBidLoader.cancelRefresh();
        verify(mMockTimerTask).cancelRefreshTimer();
    }

    private BidLoader createBidLoader(Context context, AdConfiguration adConfiguration, BidRequesterListener requestListener) {
        BidLoader bidLoader = new BidLoader(context, adConfiguration, requestListener);
        WhiteBox.setInternalState(bidLoader, "mBidRequester", mMockRequester);
        WhiteBox.setInternalState(bidLoader, "mRefreshTimerTask", mMockTimerTask);
        return bidLoader;
    }

    private NativeAdConfiguration getNativeAdConfiguration() {
        NativeAdConfiguration nativeConfiguration = new NativeAdConfiguration();

        NativeAssetData nativeAssetData = new NativeAssetData();
        nativeAssetData.setLen(100);
        nativeAssetData.setType(NativeAssetData.DataType.SPONSORED);
        nativeAssetData.setRequired(true);

        NativeAssetImage nativeAssetImage = new NativeAssetImage();
        nativeAssetImage.setW(100);
        nativeAssetImage.setH(200);
        nativeAssetImage.setType(NativeAssetImage.ImageType.ICON);
        nativeAssetImage.setRequired(true);

        nativeConfiguration.getAssets().add(nativeAssetData);
        nativeConfiguration.getAssets().add(nativeAssetImage);

        return nativeConfiguration;
    }
}