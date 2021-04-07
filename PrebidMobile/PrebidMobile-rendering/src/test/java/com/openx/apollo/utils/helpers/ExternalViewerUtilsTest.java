package com.openx.apollo.utils.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.openx.apollo.listeners.OnBrowserActionResultListener;
import com.openx.apollo.sdk.ApolloSettings;
import com.openx.apollo.utils.url.ActionNotResolvedException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class ExternalViewerUtilsTest {

    @Mock
    private Context mMockContext;
    @Mock
    private OnBrowserActionResultListener mMockResultListener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        ApolloSettings.useExternalBrowser = false;
    }

    @Test
    public void whenIsActivityCallableAndContextOrIntentNull_ReturnFalse() {
        assertFalse(ExternalViewerUtils.isActivityCallable(null, null));
        assertFalse(ExternalViewerUtils.isActivityCallable(mMockContext, null));
        assertFalse(ExternalViewerUtils.isActivityCallable(null, mock(Intent.class)));
    }

    @Test
    public void whenIsActivityCallableAndQueryIntentActivitiesEmpty_ReturnFalse() {
        PackageManager mockManager = mock(PackageManager.class);
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(Collections.emptyList());
        when(mMockContext.getPackageManager()).thenReturn(mockManager);
        assertFalse(ExternalViewerUtils.isActivityCallable(mMockContext, mock(Intent.class)));
    }

    @Test
    public void whenIsActivityCallableAndQueryIntentActivitiesNotEmpty_ReturnTrue() {
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mMockContext.getPackageManager()).thenReturn(mockManager);
        assertTrue(ExternalViewerUtils.isActivityCallable(mMockContext, mock(Intent.class)));
    }

    @Test
    public void whenStartExternalVideoPlayerAndUrlNull_DoNothing() {
        ExternalViewerUtils.startExternalVideoPlayer(mMockContext, null);
        verifyZeroInteractions(mMockContext);
    }

    @Test
    public void whenStartExternalVideoPlayerAndUrlNotNull_StartActivity() {
        ExternalViewerUtils.startExternalVideoPlayer(mMockContext, "https://url");
        verify(mMockContext).startActivity(any(Intent.class));
    }

    @Test
    public void whenLaunchApplicationUrl_StartActivity() throws ActionNotResolvedException {
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mMockContext.getPackageManager()).thenReturn(mockManager);
        ExternalViewerUtils.launchApplicationUrl(mMockContext, Uri.parse("test"));
        verify(mMockContext).startActivity(any(Intent.class));
    }

    @Test
    public void whenStartBrowserAndUseExternalBrowserFalseAndActivityCallable_NotifyInternalBrowserSuccess() {
        ApolloSettings.useExternalBrowser = false;
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mMockContext.getPackageManager()).thenReturn(mockManager);

        ExternalViewerUtils.startBrowser(mMockContext, "url", true, mMockResultListener);
        verify(mMockContext).startActivity(any(Intent.class));
        verify(mMockResultListener).onSuccess(OnBrowserActionResultListener.BrowserActionResult.INTERNAL_BROWSER);
    }

    @Test
    public void whenStartBrowserAndUseExternalBrowserFalseAndActivityNotCallable_NotifyExternalBrowserSuccess() {
        ApolloSettings.useExternalBrowser = false;
        PackageManager mockManager = mock(PackageManager.class);
        List<ResolveInfo> mockList = Collections.singletonList(mock(ResolveInfo.class));
        when(mockManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(mockList);
        when(mMockContext.getPackageManager()).thenReturn(mockManager);

        ExternalViewerUtils.startBrowser(mMockContext, "url", true, mMockResultListener);
        verify(mMockContext).startActivity(any(Intent.class));
        verify(mMockResultListener).onSuccess(OnBrowserActionResultListener.BrowserActionResult.INTERNAL_BROWSER);
    }

    @Test
    public void whenStartBrowserAndUseExternalBrowserTrue_NotifyExternalBrowserSuccess() {
        ApolloSettings.useExternalBrowser = true;

        ExternalViewerUtils.startBrowser(mMockContext, "https://url.com", true, mMockResultListener);
        verify(mMockContext).startActivity(any(Intent.class));
        verify(mMockResultListener).onSuccess(OnBrowserActionResultListener.BrowserActionResult.EXTERNAL_BROWSER);
    }
}