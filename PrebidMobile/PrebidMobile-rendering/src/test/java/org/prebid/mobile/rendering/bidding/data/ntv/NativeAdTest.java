package org.prebid.mobile.rendering.bidding.data.ntv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.apollo.test.utils.ResourceUtils;
import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.listeners.NativeAdListener;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData;
import org.prebid.mobile.rendering.session.manager.NativeOmVerification;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.rendering.views.browser.AdBrowserActivity.EXTRA_URL;

@RunWith(RobolectricTestRunner.class)
public class NativeAdTest {
    private NativeAd mNativeAd;

    private Context mContext;

    @Mock
    private NativeAdListener mMockNativeAdListener;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).get();

        String adm = ResourceUtils.convertResourceToString("native_bid_response_example.json");
        mNativeAd = new NativeAdParser().parse(adm);
        mNativeAd.setNativeAdListener(mMockNativeAdListener);
    }

    @Test
    public void registerView_StartVisibilityTracking_And_ClicksShouldOpenRootLink() {
        View mockAdView = mock(View.class);
        Context spyContext = spy(mContext);
        when(mockAdView.getContext()).thenReturn(spyContext);

        View mockClickView = mock(View.class);
        View mockSecondClickView = mock(View.class);
        NativeAdLink rootNativeAdLink = WhiteBox.getInternalState(mNativeAd, "mNativeAdLink");

        mockClickListener(mockClickView, mockAdView);
        mockClickListener(mockSecondClickView, mockAdView);

        prepareIntentVerifyUrl(spyContext, rootNativeAdLink);

        mNativeAd.registerView(mockAdView, mockClickView, mockSecondClickView);

        assertNotNull(WhiteBox.getInternalState(mNativeAd, "mVisibilityTracker"));
        verify(mockClickView, times(1)).setOnClickListener(any());
        verify(mockSecondClickView, times(1)).setOnClickListener(any());

        verify(spyContext, times(2)).startActivity(any(Intent.class));
        verify(mMockNativeAdListener, times(2)).onAdClicked(eq(mNativeAd));
    }

    @Test
    public void registerView_NativeOmTrackerExists_StartAdSession() throws IOException {
        String adm = ResourceUtils.convertResourceToString("native_bid_response_with_om_example.json");
        mNativeAd = new NativeAdParser().parse(adm);
        mNativeAd.setNativeAdListener(mMockNativeAdListener);

        View mockAdView = mock(View.class);
        Context spyContext = spy(mContext);
        when(mockAdView.getContext()).thenReturn(spyContext);

        NativeAdLink rootNativeAdLink = WhiteBox.getInternalState(mNativeAd, "mNativeAdLink");
        prepareIntentVerifyUrl(spyContext, rootNativeAdLink);

        OmAdSessionManager mockSessionManager = mock(OmAdSessionManager.class);
        WhiteBox.setInternalState(mNativeAd, "mOmAdSessionManager", mockSessionManager);

        mNativeAd.registerView(mockAdView);
        verify(mockSessionManager).initNativeDisplayAdSession(eq(mockAdView), any(NativeOmVerification.class), anyString());
        verify(mockSessionManager).startAdSession();
        verify(mockSessionManager).displayAdLoaded();
    }

    @Test
    public void registerClickView_WithBaseNativeAd_DeeplinkFailed_UseFallbackUrl_HandleClickActionFromBaseNativeAd() {
        View mockClickView = mock(View.class);
        Context spyContext = spy(mContext);
        when(mockClickView.getContext()).thenReturn(spyContext);

        mockClickListener(mockClickView, mockClickView);

        final NativeAdImage nativeAdImage = mNativeAd.getNativeAdImageList().get(1);
        WhiteBox.setInternalState(nativeAdImage.getNativeAdLink(), "mFallback", "https://fallback.com");
        NativeAdLink spyNativeAdLink = spy(nativeAdImage.getNativeAdLink());
        nativeAdImage.setNativeAdLink(spyNativeAdLink);

        mNativeAd.registerClickView(mockClickView, nativeAdImage);

        prepareIntentVerifyUrl(spyContext, nativeAdImage.getNativeAdLink());
        verify(spyNativeAdLink).getFallback();
        verify(mMockNativeAdListener, times(1)).onAdClicked(mNativeAd);
    }

    @Test
    public void registerClickView_WithNativeAdElement_HandleClickActionWithLinkFromNativeAdElement() {
        View mockClickView = mock(View.class);
        Context spyContext = spy(mContext);
        when(mockClickView.getContext()).thenReturn(spyContext);
        mockClickListener(mockClickView, mockClickView);

        final NativeAdTitle nativeAdTitle = mNativeAd.getNativeAdTitleList().get(0);

        prepareIntentVerifyUrl(spyContext, nativeAdTitle.getNativeAdLink());
        mNativeAd.registerClickView(mockClickView, NativeAdElementType.TITLE_VIEW);
        verify(mMockNativeAdListener, times(1)).onAdClicked(eq(mNativeAd));

        final NativeAdData nativeAdData = mNativeAd.getNativeAdDataList(NativeAssetData.DataType.DESC).get(0);
        prepareIntentVerifyUrl(spyContext, nativeAdData.getNativeAdLink());
        mNativeAd.registerClickView(mockClickView, NativeAdElementType.CONTENT_VIEW);
        verify(mMockNativeAdListener, times(2)).onAdClicked(eq(mNativeAd));

        // no link assets. NativeAd's link will be used
        prepareIntentVerifyUrl(spyContext, WhiteBox.getInternalState(mNativeAd, "mNativeAdLink"));
        mNativeAd.registerClickView(mockClickView, NativeAdElementType.ICON_VIEW);
        verify(mMockNativeAdListener, times(3)).onAdClicked(eq(mNativeAd));
        mNativeAd.registerClickView(mockClickView, NativeAdElementType.VIDEO_VIEW);
        verify(mMockNativeAdListener, times(4)).onAdClicked(eq(mNativeAd));
        mNativeAd.registerClickView(mockClickView, NativeAdElementType.MAIN_IMAGE_VIEW);
        verify(mMockNativeAdListener, times(5)).onAdClicked(eq(mNativeAd));
    }

    @Test
    public void visibilityTrackerListenerInvoked_ResultIsImpressionAndValid_FireImpression_NotifyListener() {
        final CreativeVisibilityTracker.VisibilityTrackerListener visibilityTrackerListener = WhiteBox.getInternalState(mNativeAd, "mVisibilityTrackerListener");
        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     new ViewExposure(),
                                                                     true,
                                                                     true);

        visibilityTrackerListener.onVisibilityChanged(result);

        verify(mMockNativeAdListener, times(1)).onAdEvent(mNativeAd,
                                                          NativeEventTracker.EventType.IMPRESSION);
    }

    @Test
    public void visibilityTrackerListenerInvoked_ResultIsOmidAndValid_FireOmImpression_NotifyListener() {
        OmAdSessionManager mockSessionManager = mock(OmAdSessionManager.class);
        WhiteBox.setInternalState(mNativeAd, "mOmAdSessionManager", mockSessionManager);
        final CreativeVisibilityTracker.VisibilityTrackerListener visibilityTrackerListener = WhiteBox.getInternalState(mNativeAd, "mVisibilityTrackerListener");
        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.OMID,
                                                                     new ViewExposure(),
                                                                     true,
                                                                     true);

        visibilityTrackerListener.onVisibilityChanged(result);

        verify(mMockNativeAdListener, times(1)).onAdEvent(mNativeAd,
                                                          NativeEventTracker.EventType.OMID);
        verify(mockSessionManager).registerImpression();
    }

    @Test
    public void visibilityTrackerListenerInvoked_ResultIsInvalidValid_DoNothing() {
        final CreativeVisibilityTracker.VisibilityTrackerListener visibilityTrackerListener = WhiteBox.getInternalState(mNativeAd, "mVisibilityTrackerListener");
        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     new ViewExposure(),
                                                                     false,
                                                                     true);
        final VisibilityTrackerResult secondResult = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                                 new ViewExposure(),
                                                                                 true,
                                                                                 false);
        final VisibilityTrackerResult thirdResult = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                                 new ViewExposure(),
                                                                                 false,
                                                                                 false);

        visibilityTrackerListener.onVisibilityChanged(result);
        visibilityTrackerListener.onVisibilityChanged(secondResult);
        visibilityTrackerListener.onVisibilityChanged(thirdResult);

        verifyZeroInteractions(mMockNativeAdListener);
    }

    @Test
    public void destroy_StopVisibilityTracker() {
        CreativeVisibilityTracker mockCreativeVisibilityTracker = mock(CreativeVisibilityTracker.class);
        WhiteBox.setInternalState(mNativeAd, "mVisibilityTracker", mockCreativeVisibilityTracker);

        mNativeAd.destroy();

        verify(mockCreativeVisibilityTracker, times(1)).stopVisibilityCheck();
    }

    private void mockClickListener(View clickViewToMock, View onClickParam) {
        doAnswer(invocation -> {
            final View.OnClickListener listener = invocation.getArgumentAt(0, View.OnClickListener.class);
            listener.onClick(onClickParam);
            return null;
        }).when(clickViewToMock).setOnClickListener(any());
    }

    private void prepareIntentVerifyUrl(Context spyContext, NativeAdLink nativeAdLink) {
        doAnswer(invocation -> {
            final Intent intent = invocation.getArgumentAt(0, Intent.class);
            final String url = intent.getStringExtra(EXTRA_URL);
            assertEquals(nativeAdLink.getUrl(), url);
            return null;
        }).when(spyContext).startActivity(any());
    }
}