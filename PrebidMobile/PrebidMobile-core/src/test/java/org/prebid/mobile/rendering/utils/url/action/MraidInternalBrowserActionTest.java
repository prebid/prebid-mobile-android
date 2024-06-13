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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidInternalBrowserActionTest {
    private static final Uri VALID_URI = Uri.parse("http://prebid.com");
    private static final String VALID_URL = VALID_URI.toString();

    private MraidInternalBrowserAction mraidInternalBrowserAction;

    @Mock BaseJSInterface mockBaseJsInterface;
    @Mock Context mockContext;
    @Mock UrlHandler mockUrlHandler;
    @Mock
    MraidVariableContainer mockMraidVariableContainer;
    private Context context;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        context = Robolectric.buildActivity(Activity.class).create().get();
        mraidInternalBrowserAction = new MraidInternalBrowserAction(mockBaseJsInterface, -1);
        when(mockBaseJsInterface.getMraidVariableContainer()).thenReturn(mockMraidVariableContainer);
    }

    @Test
    public void shouldOverrideUrlLoadingWithHttpHttpsScheme_ReturnTrue() {
        Uri httpUri = Uri.parse("http://prebid.com");
        Uri httpsUri = Uri.parse("https://prebid.com");

        assertTrue(mraidInternalBrowserAction.shouldOverrideUrlLoading(httpUri));
        assertTrue(mraidInternalBrowserAction.shouldOverrideUrlLoading(httpsUri));
    }

    @Test
    public void shouldOverrideUrlLoadingWithCustomScheme_ReturnFalse() {
        Uri customUri = Uri.parse("prebid://open");
        Uri deepLinkPlusUri = Uri.parse("deeplink+://open");

        assertFalse(mraidInternalBrowserAction.shouldOverrideUrlLoading(customUri));
        assertFalse(mraidInternalBrowserAction.shouldOverrideUrlLoading(deepLinkPlusUri));
    }

    @Test
    public void performActionWithNullBaseJsInterface_ThrowException() {
        MraidInternalBrowserAction mraidInternalBrowserAction = new MraidInternalBrowserAction(null, -1);

        String expectedMessage = "Action can't be handled. BaseJSInterface is null";
        String actualMessage = "";

        try {
            mraidInternalBrowserAction.performAction(mockContext, mockUrlHandler, VALID_URI);
        }
        catch (ActionNotResolvedException e) {
            actualMessage = e.getMessage();
        }
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void performActionWithValidJsInterface_FollowToOriginalUrl()
    throws ActionNotResolvedException {
        String url = VALID_URI.toString();

        mraidInternalBrowserAction.performAction(mockContext, mockUrlHandler, VALID_URI);

        verify(mockBaseJsInterface).followToOriginalUrl(eq(url), any(RedirectUrlListener.class));
    }

    @Test
    public void handleInternalBrowserActionFollowUrlSuccessAndIsMraid_StartActionViewActivity() {
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        ArgumentCaptor<RedirectUrlListener> callbackCapture = ArgumentCaptor.forClass(RedirectUrlListener.class);
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        String url = Uri.parse("tel://222").toString();

        mraidInternalBrowserAction.handleInternalBrowserAction(mockContext, mockBaseJsInterface, url);

        verify(mockBaseJsInterface).followToOriginalUrl(eq(url), callbackCapture.capture());

        callbackCapture.getValue().onSuccess(url, null);

        verify(mockContext, times(1)).getApplicationContext();
        verify(mockContext, times(1)).startActivity(intentArgumentCaptor.capture());

        Intent intentArgument = intentArgumentCaptor.getValue();

        assertEquals(intentArgument.getAction(), Intent.ACTION_VIEW);
        assertEquals(intentArgument.getFlags(), BuildConfig.DEBUG ? 0 : Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void handleInternalBrowserActionFollowUrlSuccessAndNotMraidAndNotVideoContent_LaunchBrowserActivity() {
        String url = VALID_URI.toString();
        MraidInternalBrowserAction spyMraidInternalBrowserAction = spy(mraidInternalBrowserAction);
        ArgumentCaptor<RedirectUrlListener> callbackCapture = ArgumentCaptor.forClass(RedirectUrlListener.class);

        spyMraidInternalBrowserAction.handleInternalBrowserAction(context, mockBaseJsInterface, url);

        verify(mockBaseJsInterface).followToOriginalUrl(eq(url), callbackCapture.capture());

        callbackCapture.getValue().onSuccess(url, null);

        verify(spyMraidInternalBrowserAction).launchBrowserActivity(any(Activity.class), eq(mockBaseJsInterface), eq(url));
    }

    @Test
    @Config(sdk = 28)
    public void handleInternalBrowserActionFollowUrlSuccessAndIsVideoContent_PlayVideo() {
        String url = "https://video.mp4";
        String extension = "video/mp4";
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("mp4", extension);

        ArgumentCaptor<RedirectUrlListener> callbackCapture = ArgumentCaptor.forClass(RedirectUrlListener.class);
        mraidInternalBrowserAction.handleInternalBrowserAction(context, mockBaseJsInterface, url);

        verify(mockBaseJsInterface).followToOriginalUrl(eq(url), callbackCapture.capture());

        callbackCapture.getValue().onSuccess(url, extension);

        verify(mockBaseJsInterface).playVideo(url);
    }

    @Test
    public void shouldBeTriggeredByUser_ReturnTrue() {
        assertTrue(mraidInternalBrowserAction.shouldBeTriggeredByUserAction());
    }

    @Test
    public void launchBrowserActivityWithNullUrlAndNonEmptyResolveInfoList_StartBrowserActivity() {
        List<ResolveInfo> resolveInfoArrayList = new ArrayList<>();
        resolveInfoArrayList.add(null);
        prepareContextAndInterfaceMocks(false, null, resolveInfoArrayList);

        mraidInternalBrowserAction.launchBrowserActivity(mockContext, mockBaseJsInterface, null);

        verify(mockContext).startActivity(any(Intent.class));
    }

    @Test
    public void launchBrowserActivityWithNullUrlAndEmptyResolveInfoList_NeverStartBrowserActivity() {
        prepareContextAndInterfaceMocks(false, null, new ArrayList<>());

        mraidInternalBrowserAction.launchBrowserActivity(mockContext, mockBaseJsInterface, null);

        verify(mockContext, never()).startActivity(any(Intent.class));
    }

    @Test
    public void launchBrowserActivityWithValidUrlAndNonEmptyResolveInfoList_StartBrowserActivity() {
        String url = VALID_URI.toString();
        List<ResolveInfo> resolveInfos = new ArrayList<>();
        resolveInfos.add(null);
        prepareContextAndInterfaceMocks(true, url, resolveInfos);

        mraidInternalBrowserAction.launchBrowserActivity(mockContext, mockBaseJsInterface, url);

        verify(mockMraidVariableContainer).setUrlForLaunching(url);
        verify(mockContext).startActivity(any(Intent.class));
    }

    @Test
    public void launchBrowserActivityWithValidUrlAndEmptyResolveInfoList_StartBrowserActivity() {
        String demo = VALID_URI.toString();
        prepareContextAndInterfaceMocks(true, demo, new ArrayList<>());

        mraidInternalBrowserAction.launchBrowserActivity(mockContext, mockBaseJsInterface, demo);

        verify(mockMraidVariableContainer).setUrlForLaunching(demo);
        verify(mockContext).startActivity(any(Intent.class));
    }

    private void prepareContextAndInterfaceMocks(boolean isLaunchingWithUrl, String url, List<ResolveInfo> resolveInfoList) {
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockPackageManager.queryIntentActivities(any(Intent.class), eq(PackageManager.MATCH_DEFAULT_ONLY))).thenReturn(resolveInfoList);
        when(mockMraidVariableContainer.isLaunchedWithUrl()).thenReturn(isLaunchingWithUrl);
        when(mockMraidVariableContainer.getUrlForLaunching()).thenReturn(url);
    }
}
