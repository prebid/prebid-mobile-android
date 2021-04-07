package com.openx.apollo.utils.url.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.networking.tracking.TrackingManager;
import com.openx.apollo.utils.url.ActionNotResolvedException;
import com.openx.apollo.utils.url.UrlHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.openx.apollo.utils.url.action.DeepLinkPlusAction.QUERY_FALLBACK_TRACKING_URL;
import static com.openx.apollo.utils.url.action.DeepLinkPlusAction.QUERY_FALLBACK_URL;
import static com.openx.apollo.utils.url.action.DeepLinkPlusAction.QUERY_PRIMARY_TRACKING_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class DeepLinkPlusActionTest {
    private static final String INVALID_DEEPLINK_EXAMPLE = "deeplink+://example";

    private static final String FULL_DEEPLINK_EXAMPLE = "deeplink+://navigate?primaryUrl=google%3A%2F%2Ftimeline"
                                                        + "&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fclicktracking&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtracking"
                                                        + "&fallbackUrl=http%3A%2F%2Fmobile.twitter.com&fallbackTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtrackingfallback";

    private static final String NO_FALLBACK_DEEPLINK_EXAMPLE = "deeplink+://navigate?primaryUrl=google%3A%2F%2Ftimeline"
                                                               + "&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fclicktracking&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtracking";

    private static final String INVALID_NESTED_DEEPLINK_IN_PRIMARY_URL = "deeplink+://navigate?primaryUrl=deeplink%2B://navigate?primaryUrl=openx.com";

    private static final String INVALID_NESTED_DEEPLINK_IN_FALLBACK_URL = "deeplink+://navigate?primaryUrl=google%3A%2F%2Ftimeline"
                                                                          + "&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fclicktracking&primaryTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtracking"
                                                                          + "&fallbackUrl=deeplink%2B://Fmobile.twitter.com&fallbackTrackingUrl=http%3A%2F%2Fmopub.com%2Fmopubtrackingfallback";

    private DeepLinkPlusAction mDeepLinkPlusAction;

    @Mock
    Context mMockContext;
    @Mock
    UrlHandler mMockUrlHandler;
    @Mock
    TrackingManager mMockTrackingManager;
    @Mock
    PackageManager mMockPackageManager;

    @Before
    public void setup() throws IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        mDeepLinkPlusAction = new DeepLinkPlusAction();

        WhiteBox.field(TrackingManager.class, "sInstance").set(null, mMockTrackingManager);

        when(mMockPackageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(new ArrayList<>());
        when(mMockContext.getPackageManager()).thenReturn(mMockPackageManager);
    }

    @After
    public void clean() throws IllegalAccessException {
        WhiteBox.field(TrackingManager.class, "sInstance").set(null, null);
    }

    @Test
    public void shouldOverrideUrlLoadingHttpHttpsCustomSchemes_ReturnFalse() {
        Uri httpUri = Uri.parse("http://openx.com");
        Uri httpsUri = Uri.parse("https://openx.com");
        Uri customScheme = Uri.parse("openx://open");

        assertFalse(mDeepLinkPlusAction.shouldOverrideUrlLoading(httpUri));
        assertFalse(mDeepLinkPlusAction.shouldOverrideUrlLoading(httpsUri));
        assertFalse(mDeepLinkPlusAction.shouldOverrideUrlLoading(customScheme));
    }

    @Test
    public void shouldOverrideUrlLoadingDeepLinkPlusScheme_ReturnTrue() {
        assertTrue(mDeepLinkPlusAction.shouldOverrideUrlLoading(Uri.parse(INVALID_DEEPLINK_EXAMPLE)));
    }

    @Test
    public void performActionWithNoHost_ThrowException() {
        String expectedMessage = "Deeplink+ URL did not have 'navigate' as the host.";
        String actualMessage = "";

        try {
            mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, Uri.parse("example.com/holiday/openx/"));
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
            mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, Uri.parse("deeplink+://navigate?"));
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
            mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, Uri.parse(INVALID_NESTED_DEEPLINK_IN_PRIMARY_URL));
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
        when(mMockPackageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(resolveInfos);

        Uri validDeepLinkPlusUri = Uri.parse(FULL_DEEPLINK_EXAMPLE);
        List<String> primaryTrackingUrl = validDeepLinkPlusUri.getQueryParameters(QUERY_PRIMARY_TRACKING_URL);

        mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, validDeepLinkPlusUri);

        verify(mMockTrackingManager, times(1)).fireEventTrackingURLs(primaryTrackingUrl);
    }

    @Test
    public void performActionWithValidPrimaryUrlAndTrackingUrlAndActivityIsCallable_TrackEvents()
    throws ActionNotResolvedException {
        ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();
        resolveInfos.add(mock(ResolveInfo.class));
        when(mMockPackageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(resolveInfos);

        Uri validDeepLinkPlusUri = Uri.parse(FULL_DEEPLINK_EXAMPLE);
        List<String> primaryTrackingUrl = validDeepLinkPlusUri.getQueryParameters(QUERY_PRIMARY_TRACKING_URL);

        mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, validDeepLinkPlusUri);
        verify(mMockTrackingManager, times(1)).fireEventTrackingURLs(primaryTrackingUrl);
    }

    @Test
    public void performActionWithInvalidPrimaryUrlAndEmptyFallback_ThrowException() {
        Uri emptyFallbackDeepLink = Uri.parse(NO_FALLBACK_DEEPLINK_EXAMPLE);

        String expectedMessage = "Unable to handle 'primaryUrl' for Deeplink+ and 'fallbackUrl' was missing.";
        String actualMessage = "";

        try {
            mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, emptyFallbackDeepLink);
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
            mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, invalidFallbackDeepLink);
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

        mDeepLinkPlusAction.performAction(mMockContext, mMockUrlHandler, validFallbackDeepLinkUri);

        verify(mMockUrlHandler).handleUrl(mMockContext, fallbackUrl, fallbackTrackingUrls, true);
    }

    @Test
    public void shouldBeTriggeredByUser_ReturnTrue() {
        assertTrue(mDeepLinkPlusAction.shouldBeTriggeredByUserAction());
    }
}
