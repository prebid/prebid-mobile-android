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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidInternalBrowserActionTest {
    private static final Uri VALID_URI = Uri.parse("http://prebid.com");
    private static final String VALID_URL = VALID_URI.toString();

    private MraidInternalBrowserAction mMraidInternalBrowserAction;

    @Mock BaseJSInterface mMockBaseJsInterface;
    @Mock Context mMockContext;
    @Mock UrlHandler mMockUrlHandler;
    @Mock
    MraidVariableContainer mMockMraidVariableContainer;
    private Context mContext;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mMraidInternalBrowserAction = new MraidInternalBrowserAction(mMockBaseJsInterface, -1);
        when(mMockBaseJsInterface.getMraidVariableContainer()).thenReturn(mMockMraidVariableContainer);
    }

    @Test
    public void shouldOverrideUrlLoadingWithHttpHttpsScheme_ReturnTrue() {
        Uri httpUri = Uri.parse("http://prebid.com");
        Uri httpsUri = Uri.parse("https://prebid.com");

        assertTrue(mMraidInternalBrowserAction.shouldOverrideUrlLoading(httpUri));
        assertTrue(mMraidInternalBrowserAction.shouldOverrideUrlLoading(httpsUri));
    }

    @Test
    public void shouldOverrideUrlLoadingWithCustomScheme_ReturnFalse() {
        Uri customUri = Uri.parse("prebid://open");
        Uri deepLinkPlusUri = Uri.parse("deeplink+://open");

        assertFalse(mMraidInternalBrowserAction.shouldOverrideUrlLoading(customUri));
        assertFalse(mMraidInternalBrowserAction.shouldOverrideUrlLoading(deepLinkPlusUri));
    }

    @Test
    public void performActionWithNullBaseJsInterface_ThrowException() {
        MraidInternalBrowserAction mraidInternalBrowserAction = new MraidInternalBrowserAction(null, -1);

        String expectedMessage = "Action can't be handled. BaseJSInterface is null";
        String actualMessage = "";

        try {
            mraidInternalBrowserAction.performAction(mMockContext, mMockUrlHandler, VALID_URI);
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

        mMraidInternalBrowserAction.performAction(mMockContext, mMockUrlHandler, VALID_URI);

        verify(mMockBaseJsInterface).followToOriginalUrl(eq(url), any(RedirectUrlListener.class));
    }

    @Test
    public void handleInternalBrowserActionFollowUrlSuccessAndIsMraid_StartActionViewActivity() {
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);

        ArgumentCaptor<RedirectUrlListener> callbackCapture = ArgumentCaptor.forClass(RedirectUrlListener.class);
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        String url = Uri.parse("tel://222").toString();

        mMraidInternalBrowserAction.handleInternalBrowserAction(mMockContext, mMockBaseJsInterface, url);

        verify(mMockBaseJsInterface).followToOriginalUrl(eq(url), callbackCapture.capture());

        callbackCapture.getValue().onSuccess(url, null);

        verify(mMockContext, times(1)).getApplicationContext();
        verify(mMockContext, times(1)).startActivity(intentArgumentCaptor.capture());

        Intent intentArgument = intentArgumentCaptor.getValue();

        assertEquals(intentArgument.getAction(), Intent.ACTION_VIEW);
        assertEquals(intentArgument.getFlags(), Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void handleInternalBrowserActionFollowUrlSuccessAndNotMraidAndNotVideoContent_LaunchBrowserActivity() {
        String url = VALID_URI.toString();
        MraidInternalBrowserAction spyMraidInternalBrowserAction = spy(mMraidInternalBrowserAction);
        ArgumentCaptor<RedirectUrlListener> callbackCapture = ArgumentCaptor.forClass(RedirectUrlListener.class);

        spyMraidInternalBrowserAction.handleInternalBrowserAction(mContext, mMockBaseJsInterface, url);

        verify(mMockBaseJsInterface).followToOriginalUrl(eq(url), callbackCapture.capture());

        callbackCapture.getValue().onSuccess(url, null);

        verify(spyMraidInternalBrowserAction).launchBrowserActivity(any(Activity.class), eq(mMockBaseJsInterface), eq(url));
    }

    @Test
    @Config(sdk = 28)
    public void handleInternalBrowserActionFollowUrlSuccessAndIsVideoContent_PlayVideo() {
        String url = "https://video.mp4";
        String extension = "video/mp4";
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("mp4", extension);

        ArgumentCaptor<RedirectUrlListener> callbackCapture = ArgumentCaptor.forClass(RedirectUrlListener.class);
        mMraidInternalBrowserAction.handleInternalBrowserAction(mContext, mMockBaseJsInterface, url);

        verify(mMockBaseJsInterface).followToOriginalUrl(eq(url), callbackCapture.capture());

        callbackCapture.getValue().onSuccess(url, extension);

        verify(mMockBaseJsInterface).playVideo(url);
    }

    @Test
    public void shouldBeTriggeredByUser_ReturnTrue() {
        assertTrue(mMraidInternalBrowserAction.shouldBeTriggeredByUserAction());
    }

    @Test
    public void launchBrowserActivityWithNullUrlAndNonEmptyResolveInfoList_StartBrowserActivity() {
        List<ResolveInfo> resolveInfoArrayList = new ArrayList<>();
        resolveInfoArrayList.add(null);
        prepareContextAndInterfaceMocks(false, null, resolveInfoArrayList);

        mMraidInternalBrowserAction.launchBrowserActivity(mMockContext, mMockBaseJsInterface, null);

        verify(mMockContext).startActivity(any(Intent.class));
    }

    @Test
    public void launchBrowserActivityWithNullUrlAndEmptyResolveInfoList_NeverStartBrowserActivity() {
        prepareContextAndInterfaceMocks(false, null, new ArrayList<>());

        mMraidInternalBrowserAction.launchBrowserActivity(mMockContext, mMockBaseJsInterface, null);

        verify(mMockContext, never()).startActivity(any(Intent.class));
    }

    @Test
    public void launchBrowserActivityWithValidUrlAndNonEmptyResolveInfoList_StartBrowserActivity() {
        String url = VALID_URI.toString();
        List<ResolveInfo> resolveInfos = new ArrayList<>();
        resolveInfos.add(null);
        prepareContextAndInterfaceMocks(true, url, resolveInfos);

        mMraidInternalBrowserAction.launchBrowserActivity(mMockContext, mMockBaseJsInterface, url);

        verify(mMockMraidVariableContainer).setUrlForLaunching(url);
        verify(mMockContext).startActivity(any(Intent.class));
    }

    @Test
    public void launchBrowserActivityWithValidUrlAndEmptyResolveInfoList_StartBrowserActivity() {
        String demo = VALID_URI.toString();
        prepareContextAndInterfaceMocks(true, demo, new ArrayList<>());

        mMraidInternalBrowserAction.launchBrowserActivity(mMockContext, mMockBaseJsInterface, demo);

        verify(mMockMraidVariableContainer).setUrlForLaunching(demo);
        verify(mMockContext).startActivity(any(Intent.class));
    }

    private void prepareContextAndInterfaceMocks(boolean isLaunchingWithUrl, String url, List<ResolveInfo> resolveInfoList) {
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mMockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockPackageManager.queryIntentActivities(any(Intent.class), eq(PackageManager.MATCH_DEFAULT_ONLY))).thenReturn(resolveInfoList);
        when(mMockMraidVariableContainer.isLaunchedWithUrl()).thenReturn(isLaunchingWithUrl);
        when(mMockMraidVariableContainer.getUrlForLaunching()).thenReturn(url);
    }
}
