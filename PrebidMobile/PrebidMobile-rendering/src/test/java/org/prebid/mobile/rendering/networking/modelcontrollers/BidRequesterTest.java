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

package org.prebid.mobile.rendering.networking.modelcontrollers;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidRequesterTest {

    private Context mContext;
    private AdConfiguration mAdConfiguration;
    private AdRequestInput mAdRequestInput;

    @Mock
    private ResponseHandler mMockResponseHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mAdConfiguration = new AdConfiguration();
        mAdRequestInput = new AdRequestInput();
        ManagersResolver.getInstance().prepare(mContext);
    }

    @Test
    public void whenStartAdRequestAndContextNull_OnErrorWithExceptionCalled() {
        mAdConfiguration.setConfigId("test");
        BidRequester requester = new BidRequester(null, mAdConfiguration, mAdRequestInput, mMockResponseHandler);
        requester.startAdRequest();
        verify(mMockResponseHandler).onErrorWithException(any(AdException.class), anyLong());
    }

    @Test
    public void whenStartAdRequestAndNoConfigId_OnErrorCalled() {
        mAdConfiguration.setConfigId(null);
        BidRequester requester = new BidRequester(mContext, mAdConfiguration, mAdRequestInput, mMockResponseHandler);
        requester.startAdRequest();
        verify(mMockResponseHandler).onError(anyString(), anyLong());
    }

    @Test
    public void whenStartAdRequestAndInitValid_InitAdId() {
        mAdConfiguration.setConfigId("test");
        BidRequester requester = spy(new BidRequester(mContext, mAdConfiguration, mAdRequestInput, mMockResponseHandler));
        requester.startAdRequest();
        verify(requester).makeAdRequest();
    }

    @Test
    public void whenFetchAdIdFailedOrSucceed_MakeRequest() {
        BidRequester mockRequester = mock(BidRequester.class);
        Requester.AdIdInitListener adIdInitListener = new Requester.AdIdInitListener(mockRequester);

        // Fetch successful
        adIdInitListener.adIdFetchCompletion();
        verify(mockRequester, times(1)).makeAdRequest();

        // Fetch failure
        adIdInitListener.adIdFetchFailure();
        verify(mockRequester, times(2)).makeAdRequest();
    }
}