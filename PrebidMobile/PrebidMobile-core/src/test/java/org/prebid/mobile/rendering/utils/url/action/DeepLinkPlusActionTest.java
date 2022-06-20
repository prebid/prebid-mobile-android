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

package org.prebid.mobile.rendering.utils.url.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.rendering.utils.url.action.DeepLinkPlusAction.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class DeepLinkPlusActionTest {
    private static final String INVALID_DEEPLINK_EXAMPLE = "deeplink+://example";

    private static final String FULL_DEEPLINK_EXAMPLE = "deeplink+://navigate?primaryUrl=google%3A%2F%2Ftimeline"
                                                        + "&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fclicktracking&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtracking"
                                                        + "&fallbackUrl=http%3A%2F%2Fmobile.twitter.com&fallbackTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtrackingfallback";

    private static final String NO_FALLBACK_DEEPLINK_EXAMPLE = "deeplink+://navigate?primaryUrl=google%3A%2F%2Ftimeline"
                                                               + "&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fclicktracking&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtracking";

    private static final String INVALID_NESTED_DEEPLINK_IN_PRIMARY_URL = "deeplink+://navigate?primaryUrl=deeplink%2B://navigate?primaryUrl=prebid.com";

    private static final String INVALID_NESTED_DEEPLINK_IN_FALLBACK_URL = "deeplink+://navigate?primaryUrl=google%3A%2F%2Ftimeline"
                                                                          + "&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fclicktracking&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtracking"
                                                                          + "&fallbackUrl=deeplink%2B://Fmobile.twitter.com&fallbackTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtrackingfallback";

    private DeepLinkPlusAction deepLinkPlusAction;

    @Mock
    Context mockContext;
    @Mock
    UrlHandler mockUrlHandler;
    @Mock
    TrackingManager mockTrackingManager;
    @Mock
    PackageManager mockPackageManager;

    @Before
    public void setup() throws IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        deepLinkPlusAction = new DeepLinkPlusAction();

        WhiteBox.field(TrackingManager.class, "sInstance").set(null, mockTrackingManager);

        when(mockPackageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(new ArrayList<>());
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
    }

    @After
    public void clean() throws IllegalAccessException {
        WhiteBox.field(TrackingManager.class, "sInstance").set(null, null);
    }

    @Test
    public void shouldOverrideUrlLoadingHttpHttpsCustomSchemes_ReturnFalse() {
        Uri httpUri = Uri.parse("http://prebid.com");
        Uri httpsUri = Uri.parse("https://prebid.com");
        Uri customScheme = Uri.parse("prebid://open");

        assertFalse(deepLinkPlusAction.shouldOverrideUrlLoading(httpUri));
        assertFalse(deepLinkPlusAction.shouldOverrideUrlLoading(httpsUri));
        assertFalse(deepLinkPlusAction.shouldOverrideUrlLoading(customScheme));
    }

    @Test
    public void shouldOverrideUrlLoadingDeepLinkPlusScheme_ReturnTrue() {
        assertTrue(deepLinkPlusAction.shouldOverrideUrlLoading(Uri.parse(INVALID_DEEPLINK_EXAMPLE)));
    }

    @Test
    public void performActionWithNoHost_ThrowException() {
        String expectedMessage = "Deeplink+ URL did not have 'navigate' as the host.";
        String actualMessage = "";

        try {
            deepLinkPlusAction.performAction(mockContext, mockUrlHandler, Uri.parse("example.com/holiday/prebid/"));
        }
        catch (ActionNotResolvedException e) {
            actualMessage = e.getMessage();
        }
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void performActionWithNoPrimaryUrl_ThrowException() {
        String expectedMessage = "Deeplink+ did not have 'primaryUrl' query param.";
        String actualMessage = "";

        try {
            deepLinkPlusAction.performAction(mockContext, mockUrlHandler, Uri.parse("deeplink+://navigate?"));
        }
        catch (ActionNotResolvedException e) {
            actualMessage = e.getMessage();
        }
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void performActionWithNestedDeepLinkInPrimaryUrl_ThrowException() {
        String expectedMessage = "Deeplink+ had another Deeplink+ as the 'primaryUrl'.";
        String actualMessage = "";

        try {
            deepLinkPlusAction.performAction(mockContext, mockUrlHandler, Uri.parse(INVALID_NESTED_DEEPLINK_IN_PRIMARY_URL));
        }
        catch (ActionNotResolvedException e) {
            actualMessage = e.getMessage();
        }
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void performActionWithValidPrimaryUrlAndTrackingUrlAndActivityIsNotCallable_TrackUrls()
    throws ActionNotResolvedException {
        ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();
        resolveInfos.add(mock(ResolveInfo.class));
        when(mockPackageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(resolveInfos);

        Uri validDeepLinkPlusUri = Uri.parse(FULL_DEEPLINK_EXAMPLE);
        List<String> primaryTrackingUrl = validDeepLinkPlusUri.getQueryParameters(QUERY_PRIMARY_TRACKING_URL);

        deepLinkPlusAction.performAction(mockContext, mockUrlHandler, validDeepLinkPlusUri);

        verify(mockTrackingManager, times(1)).fireEventTrackingURLs(primaryTrackingUrl);
    }

    @Test
    public void performActionWithValidPrimaryUrlAndTrackingUrlAndActivityIsCallable_TrackEvents()
    throws ActionNotResolvedException {
        ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();
        resolveInfos.add(mock(ResolveInfo.class));
        when(mockPackageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(resolveInfos);

        Uri validDeepLinkPlusUri = Uri.parse(FULL_DEEPLINK_EXAMPLE);
        List<String> primaryTrackingUrl = validDeepLinkPlusUri.getQueryParameters(QUERY_PRIMARY_TRACKING_URL);

        deepLinkPlusAction.performAction(mockContext, mockUrlHandler, validDeepLinkPlusUri);
        verify(mockTrackingManager, times(1)).fireEventTrackingURLs(primaryTrackingUrl);
    }

    @Test
    public void performActionWithInvalidPrimaryUrlAndEmptyFallback_ThrowException() {
        Uri emptyFallbackDeepLink = Uri.parse(NO_FALLBACK_DEEPLINK_EXAMPLE);

        String expectedMessage = "Unable to handle 'primaryUrl' for Deeplink+ and 'fallbackUrl' was missing.";
        String actualMessage = "";

        try {
            deepLinkPlusAction.performAction(mockContext, mockUrlHandler, emptyFallbackDeepLink);
        }
        catch (ActionNotResolvedException e) {
            actualMessage = e.getMessage();
        }
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void performActionWithInvalidPrimaryUrlAndFallbackDeepLinkPlusUrl_ThrowException() {
        Uri invalidFallbackDeepLink = Uri.parse(INVALID_NESTED_DEEPLINK_IN_FALLBACK_URL);

        String expectedMessage = "Deeplink+ URL had another Deeplink+ URL as the 'fallbackUrl'.";
        String actualMessage = "";

        try {
            deepLinkPlusAction.performAction(mockContext, mockUrlHandler, invalidFallbackDeepLink);
        }
        catch (ActionNotResolvedException e) {
            actualMessage = e.getMessage();
        }
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void performActionWithInvalidPrimaryUrlAndValidFallback_ExecuteUrlHandler()
    throws ActionNotResolvedException {
        Uri validFallbackDeepLinkUri = Uri.parse(FULL_DEEPLINK_EXAMPLE);
        String fallbackUrl = validFallbackDeepLinkUri.getQueryParameter(QUERY_FALLBACK_URL);
        List<String> fallbackTrackingUrls = validFallbackDeepLinkUri.getQueryParameters(QUERY_FALLBACK_TRACKING_URL);

        deepLinkPlusAction.performAction(mockContext, mockUrlHandler, validFallbackDeepLinkUri);

        verify(mockUrlHandler).handleUrl(mockContext, fallbackUrl, fallbackTrackingUrls, true);
    }

    @Test
    public void shouldBeTriggeredByUser_ReturnTrue() {
        assertTrue(deepLinkPlusAction.shouldBeTriggeredByUserAction());
    }
}
