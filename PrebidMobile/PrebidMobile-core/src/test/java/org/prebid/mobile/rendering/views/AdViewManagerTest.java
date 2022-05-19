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

package org.prebid.mobile.rendering.views;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.api.rendering.VideoView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.loading.CreativeFactory;
import org.prebid.mobile.rendering.loading.Transaction;
import org.prebid.mobile.rendering.loading.TransactionManager;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.video.*;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdViewManagerTest {
    private Context context;
    private AdViewManager adViewManager;

    @Mock
    private AdViewManagerListener mockAdViewListener;
    @Mock
    private InterstitialView mockAdView;
    @Mock
    private VideoCreative mockVideoCreative;
    @Mock
    private VideoCreativeView mockVideoCreativeView;
    @Mock
    InterstitialManager mockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        context = Robolectric.buildActivity(Activity.class).create().get().getApplicationContext();
        adViewManager = new AdViewManager(context, mockAdViewListener, mockAdView, mockInterstitialManager);
    }

    @Test
    public void testAdViewManager() throws Exception {
        AdViewManagerListener mockAdViewListener = mock(AdViewManagerListener.class);
        InterstitialView mockAdView = mock(InterstitialView.class);

        AdViewManager adViewManager = new AdViewManager(context, mockAdViewListener, mockAdView,
                mockInterstitialManager
        );
        assertNotNull(adViewManager);
    }

    @Test
    public void testAdViewManagerNullAdViewListener() {
        InterstitialView mockAdView = mock(InterstitialView.class);
        AdViewManager adViewManager = null;
        AdException err = null;
        try {
            adViewManager = new AdViewManager(context, null, mockAdView, mockInterstitialManager);
        }
        catch (AdException e) {
            err = e;
        }
        assertNotNull(err);
        assertNull(adViewManager);
        assertEquals(AdException.INTERNAL_ERROR + ": AdViewManagerListener is null", err.getMessage());
    }

    @Test
    public void testAdViewManagerNullParams() {
        InterstitialView mockAdView = mock(InterstitialView.class);
        AdViewManager adViewManager = null;
        AdException err = null;
        try {
            adViewManager = new AdViewManager(context, null, mockAdView, mockInterstitialManager);
        }
        catch (Exception e) {
            err = new AdException(AdException.INTERNAL_ERROR, e.getMessage());
        }
        assertNotNull(err);
        assertNull(adViewManager);
    }

    @Test
    public void showTest() throws Exception {
        AbstractCreative mock = mock(AbstractCreative.class);
        when(mock.getCreativeView()).thenReturn(mock(View.class));

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mock);
        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mockTransactionManager);

        adViewManager.show();
        verify(mockAdViewListener).viewReadyForImmediateDisplay(any(View.class));
    }

    @Test
    public void whenBannerHtmlNotResolved_Return() throws IllegalAccessException, AdException {
        ViewGroup mockBanner = mock(ViewGroup.class);
        adViewManager = new AdViewManager(context, mockAdViewListener, mockBanner, mock(InterstitialManager.class));

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isDisplay()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        adViewManager.show();
        verify(mockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void whenInterstitialHtmlNotResolved_Return()
    throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isDisplay()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        adViewManager.show();
        verify(mockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void whenInterstitialVideoNotResolved_Return()
    throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isDisplay()).thenReturn(false);
        when(mockCreative.isVideo()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        adViewManager.show();
        verify(mockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void whenVideoNotResolved_Return() throws AdException, IllegalAccessException {
        VideoView mockVideoView = mock(VideoView.class);
        adViewManager = new AdViewManager(context, mockAdViewListener, mockVideoView, mock(InterstitialManager.class));

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isVideo()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        adViewManager.show();
        verify(mockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void creativeDidCompleteTest() throws Exception {

        VideoCreative mockVideoCreative = mock(VideoCreative.class);
        when(mockVideoCreative.getCreativeModel())
                .thenReturn(new VideoCreativeModel(mock(TrackingManager.class), mock(OmEventTracker.class), new AdUnitConfiguration()));
        when(mockVideoCreative.isVideo()).thenReturn(true);
        Transaction mockTransaction = mock(Transaction.class);
        ArrayList<CreativeFactory> creativeFactories = new ArrayList<>();
        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentTransaction()).thenReturn(mockTransaction);
        when(mockTransaction.getCreativeFactories()).thenReturn(creativeFactories);

        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mockTransactionManager);
        WhiteBox.field(AdViewManager.class, "adView").set(adViewManager, mockAdView);

        adViewManager.creativeDidComplete(mockVideoCreative);
        verify(mockAdView).closeInterstitialVideo();
        verify(mockAdViewListener, times(1)).adCompleted();

        adViewManager.creativeDidComplete(mockVideoCreative);
        verify(mockAdViewListener, times(2)).adCompleted();
    }

    @Test
    public void creativeWasClickedTest() {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        adViewManager.creativeWasClicked(mockCreative, "test");
        verify(mockAdViewListener).creativeClicked(eq("test"));
    }

    @Test
    public void creativeInterstitialDidCloseTest() throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        TransactionManager mock = mock(TransactionManager.class);
        Transaction transaction = mock(Transaction.class);
        when(mock.getCurrentTransaction()).thenReturn(transaction);
        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mock);

        adViewManager.creativeInterstitialDidClose(mockCreative);
        verify(mockAdViewListener).creativeInterstitialClosed();
    }

    @Test
    public void creativeDidExpandTest() {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        adViewManager.creativeDidExpand(mockCreative);
        verify(mockAdViewListener).creativeExpanded();
    }

    @Test
    public void creativeDidCollapseTest() {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        adViewManager.creativeDidCollapse(mockCreative);
        verify(mockAdViewListener).creativeCollapsed();
    }

    @Test
    public void creativeVolumeStateChanged_NotifyListener() {
        adViewManager.creativeMuted(mockVideoCreative);
        verify(mockAdViewListener).creativeMuted();

        adViewManager.creativeUnMuted(mockVideoCreative);
        verify(mockAdViewListener).creativeMuted();
    }

    @Test
    public void creativePlaybackStateChanged_NotifyListener() {
        adViewManager.creativePaused(mockVideoCreative);
        verify(mockAdViewListener).creativePaused();

        adViewManager.creativeResumed(mockVideoCreative);
        verify(mockAdViewListener).creativeResumed();
    }

    @Test
    public void changeVolumeState_InvokeCreative() throws IllegalAccessException {
        setupCreative();

        adViewManager.mute();
        verify(mockVideoCreative).mute();

        adViewManager.unmute();
        verify(mockVideoCreative).unmute();
    }

    @Test
    public void isNotShowingEndCard_withEndCard_ReturnFalse() throws IllegalAccessException {
        setupCreative();
        when(mockVideoCreative.isDisplay()).thenReturn(true);
        when(mockVideoCreative.isEndCard()).thenReturn(true);

        assertFalse(adViewManager.isNotShowingEndCard());
    }

    @Test
    public void isNotShowingEndCard_noEndCard_ReturnTrue() throws IllegalAccessException {
        setupCreative();
        when(mockVideoCreative.isDisplay()).thenReturn(false);
        when(mockVideoCreative.isEndCard()).thenReturn(true);

        assertTrue(adViewManager.isNotShowingEndCard());
    }

    @Test
    public void hasEndCard_withEndCard_ReturnTrue() throws IllegalAccessException {
        setupCreative();
        when(mockVideoCreative.isDisplay()).thenReturn(true);

        assertTrue(adViewManager.hasEndCard());
    }

    @Test
    public void hasEndCard_noEndCard_ReturnFalse() throws IllegalAccessException {
        setupCreative();
        when(mockVideoCreative.isDisplay()).thenReturn(false);
        when(mockVideoCreative.isEndCard()).thenReturn(true);

        assertFalse(adViewManager.hasEndCard());
    }

    @Test
    public void hideTest() throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        adViewManager.hide();
        verify(mockAdView, never()).removeView(any(View.class));
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);
        adViewManager.hide();
        verify(mockAdView).removeView(any());
    }

    @Test
    public void destroy_Cleanup() throws IllegalAccessException {
        setupCreative();
        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        WhiteBox.field(AdViewManager.class, "transactionManager").set(adViewManager, mockTransactionManager);

        adViewManager.destroy();
        verify(mockTransactionManager).destroy();
        verify(mockVideoCreative).destroy();
    }

    @Test
    public void setAdVisibilityTest() throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);

        adViewManager.setAdVisibility(View.INVISIBLE);
        verify(mockCreative).handleAdWindowNoFocus();

        adViewManager.setAdVisibility(View.VISIBLE);
        verify(mockCreative).handleAdWindowFocus();
    }

    @Test
    public void testGetMediaDuration() throws IllegalAccessException {
        long mediaDuration = adViewManager.getMediaDuration();
        assertEquals(0, mediaDuration);
        VideoCreative videoCreative = mock(VideoCreative.class);
        long expectedValue = 15 * 1000;
        when(videoCreative.getMediaDuration()).thenReturn(expectedValue);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, videoCreative);
        assertEquals(expectedValue, adViewManager.getMediaDuration());
    }

    @Test
    public void testInterstitialClosed() throws IllegalAccessException {
        assertFalse(adViewManager.isInterstitialClosed());

        VideoCreative videoCreative = mock(VideoCreative.class);
        final VideoCreativeModel videoCreativeModel = mock(VideoCreativeModel.class);

        when(videoCreative.getCreativeModel()).thenReturn(videoCreativeModel);
        when(videoCreative.isInterstitialClosed()).thenReturn(true);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, videoCreative);
        assertTrue(adViewManager.isInterstitialClosed());
        when(videoCreativeModel.hasEndCard()).thenReturn(false);
        WhiteBox.field(VideoCreative.class, "model").set(videoCreative, videoCreativeModel);
        when(videoCreative.isInterstitialClosed()).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                videoCreativeModel.trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
                return null;
            }
        });
        adViewManager.isInterstitialClosed();
        verify(videoCreativeModel).trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
    }

    @Test
    public void testIsPlaying() throws IllegalAccessException {
        assertFalse(adViewManager.isPlaying());

        setupCreative();

        assertTrue(adViewManager.isPlaying());
    }

    private void setupCreative() throws IllegalAccessException {
        when(mockVideoCreative.isBuiltInVideo()).thenReturn(true);
        when(mockVideoCreative.isPlaying()).thenReturn(true);
        when(mockVideoCreative.getCreativeView()).thenReturn(mockVideoCreativeView);
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockVideoCreative);
    }

    @Test
    public void testReturnFromVideo() throws IllegalAccessException {
        View mockCallView = mock(View.class);

        setupCreative();

        when(mockVideoCreative.isVideo()).thenReturn(true);
        adViewManager.returnFromVideo(mockCallView);
        verify(mockVideoCreativeView).hideCallToAction();
        verify(mockVideoCreative).updateAdView(mockCallView);
        verify(mockVideoCreativeView).mute();
        verify(mockVideoCreative).onPlayerStateChanged(InternalPlayerState.NORMAL);
    }

    @Test
    public void testCanShowFullScreen() throws IllegalAccessException {
        assertFalse(adViewManager.canShowFullScreen());
        setupCreative();
        assertTrue(adViewManager.canShowFullScreen());
    }

    @Test
    public void testTrackVideoStateChange() throws IllegalAccessException {
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockVideoCreative);
        when(mockVideoCreative.isVideo()).thenReturn(true);

        adViewManager.trackVideoStateChange(InternalPlayerState.NORMAL);

        verify(mockVideoCreative).trackVideoStateChange(eq(InternalPlayerState.NORMAL));
    }

    @Test
    public void testUpdateAdView() throws IllegalAccessException {
        setupCreative();
        View mock = mock(View.class);
        adViewManager.updateAdView(mock);
        verify(mockVideoCreative).updateAdView(mock);
    }

    @Test
    public void whenConditionNotMet_ResumeNotCalled() {
        adViewManager.resume();
        verify(mockVideoCreative, never()).resume();
    }

    @Test
    public void whenConditionNotMet_PauseNotCalled() {
        adViewManager.pause();
        verify(mockVideoCreative, never()).pause();
    }

    @Test
    public void whenResumeCalled_CreativeResumeCalled() throws IllegalAccessException {
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockVideoCreative);
        when(mockVideoCreative.isVideo()).thenReturn(true);
        adViewManager.resume();
        verify(mockVideoCreative).resume();
    }

    @Test
    public void whenResumeCalled_CreativePauseCalled() throws IllegalAccessException {
        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockVideoCreative);
        when(mockVideoCreative.isVideo()).thenReturn(true);
        adViewManager.pause();
        verify(mockVideoCreative).pause();
    }

    @Test
    public void whenFetchedWithException_callFailedToLoad() {
        AdException adException = new AdException(AdException.INTERNAL_ERROR, "Error message");
        adViewManager.onFetchingFailed(adException);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);
        verify(mockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Error message", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void whenFetchedSuccessful_ProcessTransaction() {
        Transaction mockTransaction = mock(Transaction.class);
        CreativeFactory mockFactory = mock(CreativeFactory.class);
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockFactory.getCreative()).thenReturn(mockCreative);
        List<CreativeFactory> creativeFactories = new ArrayList<>();
        creativeFactories.add(mockFactory);

        when(mockTransaction.getTransactionState()).thenReturn("state");
        when(mockTransaction.getCreativeFactories()).thenReturn(creativeFactories);

        adViewManager.onFetchingCompleted(mockTransaction);
        verify(mockCreative).createOmAdSession();
        verify(mockAdViewListener).adLoaded(any(AdDetails.class));
    }
}
