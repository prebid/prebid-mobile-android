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

package org.prebid.mobile.rendering.utils.helpers;

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
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.listeners.OnBrowserActionResultListener;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class ExternalViewerUtilsTest {

    @Mock
    private Context mockContext;
    @Mock
    private OnBrowserActionResultListener mockResultListener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        PrebidMobile.useExternalBrowser = false;
    }

    @Test
    public void whenIsActivityCallableAndContextOrIntentNull_ReturnFalse() {
        assertFalse(ExternalViewerUtils.isActivityCallable(null, null));
        assertFalse(ExternalViewerUtils.isActivityCallable(mockContext, null));
        assertFalse(ExternalViewerUtils.isActivityCallable(null, mock(Intent.class)));
    }

    @Test
    public void whenIsActivityCallableAndQueryIntentActivitiesEmpty_ReturnFalse() {
        PackageManager mockManager = mock(PackageManager.class);
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(Collections.emptyList());
        when(mockContext.getPackageManager()).thenReturn(mockManager);
        assertFalse(ExternalViewerUtils.isActivityCallable(mockContext, mock(Intent.class)));
    }

    @Test
    public void whenIsActivityCallableAndQueryIntentActivitiesNotEmpty_ReturnTrue() {
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mockContext.getPackageManager()).thenReturn(mockManager);
        assertTrue(ExternalViewerUtils.isActivityCallable(mockContext, mock(Intent.class)));
    }

    @Test
    public void whenStartExternalVideoPlayerAndUrlNull_DoNothing() {
        ExternalViewerUtils.startExternalVideoPlayer(mockContext, null);
        verifyNoInteractions(mockContext);
    }

    @Test
    public void whenStartExternalVideoPlayerAndUrlNotNull_StartActivity() {
        ExternalViewerUtils.startExternalVideoPlayer(mockContext, "https://url");
        verify(mockContext).startActivity(any(Intent.class));
    }

    @Test
    public void whenLaunchApplicationUrl_StartActivity() throws ActionNotResolvedException {
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mockContext.getPackageManager()).thenReturn(mockManager);
        ExternalViewerUtils.launchApplicationUrl(mockContext, Uri.parse("test"));
        verify(mockContext).startActivity(any(Intent.class));
    }

    @Test
    public void whenStartBrowserAndUseExternalBrowserFalseAndActivityCallable_NotifyInternalBrowserSuccess() {
        PrebidMobile.useExternalBrowser = false;
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mockContext.getPackageManager()).thenReturn(mockManager);

        ExternalViewerUtils.startBrowser(mockContext, "url", true, mockResultListener);
        verify(mockContext).startActivity(any(Intent.class));
        verify(mockResultListener).onSuccess(OnBrowserActionResultListener.BrowserActionResult.INTERNAL_BROWSER);
    }

    @Test
    public void whenStartBrowserAndUseExternalBrowserFalseAndActivityNotCallable_NotifyExternalBrowserSuccess() {
        PrebidMobile.useExternalBrowser = false;
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mockContext.getPackageManager()).thenReturn(mockManager);

        ExternalViewerUtils.startBrowser(mockContext, "url", true, mockResultListener);
        verify(mockContext).startActivity(any(Intent.class));
        verify(mockResultListener).onSuccess(OnBrowserActionResultListener.BrowserActionResult.INTERNAL_BROWSER);
    }

    @Test
    public void whenStartBrowserAndUseExternalBrowserTrue_NotifyExternalBrowserSuccess() {
        PrebidMobile.useExternalBrowser = true;

        ExternalViewerUtils.startBrowser(mockContext, "https://url.com", true, mockResultListener);
        verify(mockContext).startActivity(any(Intent.class));
        verify(mockResultListener).onSuccess(OnBrowserActionResultListener.BrowserActionResult.EXTERNAL_BROWSER);
    }
}