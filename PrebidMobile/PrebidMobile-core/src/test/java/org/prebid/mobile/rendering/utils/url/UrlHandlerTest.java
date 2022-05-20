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

package org.prebid.mobile.rendering.utils.url;

import android.content.Context;
import android.net.Uri;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.mraid.methods.network.UrlResolutionTask;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkAction;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class UrlHandlerTest {
    private static final String VALID_URL = "http://prebid.com";

    @Mock
    Context mockContext;
    @Mock
    TrackingManager mockTrackingManager;
    @Mock
    UrlResolutionTask.UrlResolutionListener mockUrlResolutionListener;
    @Mock
    UrlHandler.UrlHandlerResultListener mockUrlHandlerResultListener;
    @Mock
    DeepLinkAction mockDeepLinkAction;

    private UrlHandler urlHandler;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        urlHandler = createUrlHandler(mockDeepLinkAction);
        WhiteBox.field(TrackingManager.class, "sInstance").set(null, mockTrackingManager);
    }

    @After
    public void cleanup() throws IllegalAccessException {
        WhiteBox.field(TrackingManager.class, "sInstance").set(null, null);
    }

    @Test
    public void whenHandleResolvedUrlWithEmptyString_NotifyFailure() {
        boolean result = createUrlHandler(null)
            .handleResolvedUrl(mockContext, " ", null, true);

        verify(mockUrlHandlerResultListener, times(1)).onFailure(" ");
        assertFalse(result);
    }

    @Test
    public void whenHandleResolvedUrlWithEmptyActions_NotifyFailure() {
        boolean result = createUrlHandler(null)
            .handleResolvedUrl(mockContext, VALID_URL, null, true);

        verify(mockUrlHandlerResultListener, times(1)).onFailure(VALID_URL);
        assertFalse(result);
    }

    @Test
    public void whenHandleResolvedUrlWithMatchingAction_HandleActionAndNotifySuccess()
    throws ActionNotResolvedException {
        String url = VALID_URL;
        Uri destinationUri = Uri.parse(url);
        List<String> trackingUrlList = new ArrayList<>();
        trackingUrlList.add("http://exampleTrackingUrl");

        when(mockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);
        when(mockDeepLinkAction.shouldOverrideUrlLoading(destinationUri)).thenReturn(true);

        boolean result = urlHandler.handleResolvedUrl(mockContext, url, trackingUrlList, true);

        verify(mockDeepLinkAction, times(1)).performAction(mockContext, urlHandler, destinationUri);
        verify(mockTrackingManager, times(1)).fireEventTrackingURLs(trackingUrlList);
        verify(mockUrlHandlerResultListener, times(1)).onSuccess(url, mockDeepLinkAction);
        assertTrue(result);
    }

    @Test
    public void whenHandleResolvedUrlWithInvalidExpectedInteractionCondition_NotifySuccess() {
        when(mockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);
        when(mockDeepLinkAction.shouldOverrideUrlLoading(any())).thenReturn(true);

        boolean result = urlHandler.handleResolvedUrl(mockContext, VALID_URL, null, false);

        assertFalse(result);
        verify(mockUrlHandlerResultListener, times(1)).onFailure(VALID_URL);
    }

    @Test(expected = ActionNotResolvedException.class)
    public void whenHandleActionWithInvalidInteractionCondition_ThrowException()
    throws ActionNotResolvedException {
        Uri destinationUri = Uri.parse(VALID_URL);
        when(mockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);

        urlHandler.handleAction(mockContext, destinationUri, mockDeepLinkAction, false);
    }

    @Test
    public void whenHandleActionWithValidInteractionCondition_PerformAction()
    throws ActionNotResolvedException {
        Uri destinationUri = Uri.parse(VALID_URL);
        when(mockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);

        urlHandler.handleAction(mockContext, destinationUri, mockDeepLinkAction, true);

        verify(mockDeepLinkAction, times(1)).performAction(mockContext, urlHandler, destinationUri);
    }

    @Test
    public void whenHandleUrlWithEmptyString_NotifyFailure() {
        String url = "  ";

        urlHandler.handleUrl(mockContext, url, null, false);

        verify(mockUrlHandlerResultListener, times(1)).onFailure(url);
    }

    @Test
    public void whenHandleUrlWithValidString_PerformUrlResolutionRequest() {
        UrlHandler spyUrlHandler = spy(createUrlHandler(mockDeepLinkAction));
        spyUrlHandler.handleUrl(mockContext, VALID_URL, null, false);

        verify(spyUrlHandler, times(1)).performUrlResolutionRequest(eq(VALID_URL), any(UrlResolutionTask.UrlResolutionListener.class));
    }

    private UrlHandler createUrlHandler(DeepLinkAction deepLinkAction) {
        UrlHandler.Builder builder = new UrlHandler.Builder();

        if (deepLinkAction != null) {
            builder.withDeepLinkAction(deepLinkAction);
        }

        builder.withResultListener(mockUrlHandlerResultListener);
        return builder.build();
    }
}
