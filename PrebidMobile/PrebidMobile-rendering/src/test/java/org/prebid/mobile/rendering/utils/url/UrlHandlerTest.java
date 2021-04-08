package org.prebid.mobile.rendering.utils.url;

import android.content.Context;
import android.net.Uri;

import com.apollo.test.utils.WhiteBox;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.mraid.methods.network.UrlResolutionTask;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkAction;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class UrlHandlerTest {
    private static final String VALID_URL = "http://openx.com";

    @Mock
    Context mMockContext;
    @Mock
    TrackingManager mMockTrackingManager;
    @Mock
    UrlResolutionTask.UrlResolutionListener mMockUrlResolutionListener;
    @Mock
    UrlHandler.UrlHandlerResultListener mMockUrlHandlerResultListener;
    @Mock
    DeepLinkAction mMockDeepLinkAction;

    private UrlHandler mUrlHandler;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mUrlHandler = createUrlHandler(mMockDeepLinkAction);
        WhiteBox.field(TrackingManager.class, "sInstance").set(null, mMockTrackingManager);
    }

    @After
    public void cleanup() throws IllegalAccessException {
        WhiteBox.field(TrackingManager.class, "sInstance").set(null, null);
    }

    @Test
    public void whenHandleResolvedUrlWithEmptyString_NotifyFailure() {
        boolean result = createUrlHandler(null)
            .handleResolvedUrl(mMockContext, " ", null, true);

        verify(mMockUrlHandlerResultListener, times(1)).onFailure(" ");
        assertFalse(result);
    }

    @Test
    public void whenHandleResolvedUrlWithEmptyActions_NotifyFailure() {
        boolean result = createUrlHandler(null)
            .handleResolvedUrl(mMockContext, VALID_URL, null, true);

        verify(mMockUrlHandlerResultListener, times(1)).onFailure(VALID_URL);
        assertFalse(result);
    }

    @Test
    public void whenHandleResolvedUrlWithMatchingAction_HandleActionAndNotifySuccess()
    throws ActionNotResolvedException {
        String url = VALID_URL;
        Uri destinationUri = Uri.parse(url);
        List<String> trackingUrlList = new ArrayList<>();
        trackingUrlList.add("http://exampleTrackingUrl");

        when(mMockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);
        when(mMockDeepLinkAction.shouldOverrideUrlLoading(destinationUri)).thenReturn(true);

        boolean result = mUrlHandler.handleResolvedUrl(mMockContext, url, trackingUrlList, true);

        verify(mMockDeepLinkAction, times(1)).performAction(mMockContext, mUrlHandler, destinationUri);
        verify(mMockTrackingManager, times(1)).fireEventTrackingURLs(trackingUrlList);
        verify(mMockUrlHandlerResultListener, times(1)).onSuccess(url, mMockDeepLinkAction);
        assertTrue(result);
    }

    @Test
    public void whenHandleResolvedUrlWithInvalidExpectedInteractionCondition_NotifySuccess() {
        when(mMockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);
        when(mMockDeepLinkAction.shouldOverrideUrlLoading(any())).thenReturn(true);

        boolean result = mUrlHandler.handleResolvedUrl(mMockContext, VALID_URL, null, false);

        assertFalse(result);
        verify(mMockUrlHandlerResultListener, times(1)).onFailure(VALID_URL);
    }

    @Test(expected = ActionNotResolvedException.class)
    public void whenHandleActionWithInvalidInteractionCondition_ThrowException()
    throws ActionNotResolvedException {
        Uri destinationUri = Uri.parse(VALID_URL);
        when(mMockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);

        mUrlHandler.handleAction(mMockContext, destinationUri, mMockDeepLinkAction, false);
    }

    @Test
    public void whenHandleActionWithValidInteractionCondition_PerformAction()
    throws ActionNotResolvedException {
        Uri destinationUri = Uri.parse(VALID_URL);
        when(mMockDeepLinkAction.shouldBeTriggeredByUserAction()).thenReturn(true);

        mUrlHandler.handleAction(mMockContext, destinationUri, mMockDeepLinkAction, true);

        verify(mMockDeepLinkAction, times(1)).performAction(mMockContext, mUrlHandler, destinationUri);
    }

    @Test
    public void whenHandleUrlWithEmptyString_NotifyFailure() {
        String url = "  ";

        mUrlHandler.handleUrl(mMockContext, url, null, false);

        verify(mMockUrlHandlerResultListener, times(1)).onFailure(url);
    }

    @Test
    public void whenHandleUrlWithValidString_PerformUrlResolutionRequest() {
        UrlHandler spyUrlHandler = spy(createUrlHandler(mMockDeepLinkAction));
        spyUrlHandler.handleUrl(mMockContext, VALID_URL, null, false);

        verify(spyUrlHandler, times(1)).performUrlResolutionRequest(eq(VALID_URL), any(UrlResolutionTask.UrlResolutionListener.class));
    }

    private UrlHandler createUrlHandler(DeepLinkAction deepLinkAction) {
        UrlHandler.Builder builder = new UrlHandler.Builder();

        if (deepLinkAction != null) {
            builder.withDeepLinkAction(deepLinkAction);
        }

        builder.withResultListener(mMockUrlHandlerResultListener);
        return builder.build();
    }
}
