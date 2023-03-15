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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidRequesterTest {

    private Context context;
    private AdUnitConfiguration adConfiguration;
    private AdRequestInput adRequestInput;

    @Mock private ResponseHandler mockResponseHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        context = Robolectric.buildActivity(Activity.class).create().get();
        adConfiguration = new AdUnitConfiguration();
        adRequestInput = new AdRequestInput();
        ManagersResolver.getInstance().prepare(context);
    }

    @Test
    public void whenStartAdRequestAndContextNull_OnErrorWithExceptionCalled() {
        adConfiguration.setConfigId("test");
        BidRequester requester = new BidRequester(null, adConfiguration, adRequestInput, mockResponseHandler);
        requester.startAdRequest();
        verify(mockResponseHandler).onErrorWithException(any(AdException.class), anyLong());
    }

    @Test
    public void whenStartAdRequestAndNoConfigId_OnErrorCalled() {
        adConfiguration.setConfigId(null);
        BidRequester requester = new BidRequester(context, adConfiguration, adRequestInput, mockResponseHandler);
        requester.startAdRequest();
        verify(mockResponseHandler).onError(anyString(), anyLong());
    }

    @Test
    public void whenStartAdRequestAndInitValid_InitAdId() {
        adConfiguration.setConfigId("test");
        BidRequester requester = spy(new BidRequester(context, adConfiguration, adRequestInput, mockResponseHandler));
        requester.startAdRequest();
        verify(requester).makeAdRequest();
    }

}