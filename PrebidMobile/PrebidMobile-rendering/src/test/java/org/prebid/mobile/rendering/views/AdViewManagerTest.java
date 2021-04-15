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
import org.prebid.mobile.rendering.bidding.display.InterstitialView;
import org.prebid.mobile.rendering.bidding.display.VideoView;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.loading.CreativeFactory;
import org.prebid.mobile.rendering.loading.Transaction;
import org.prebid.mobile.rendering.loading.TransactionManager;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.video.OmEventTracker;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdViewManagerTest {
    private Context mContext;
    private AdViewManager mAdViewManager;

    @Mock
    private AdViewManagerListener mMockAdViewListener;
    @Mock
    private InterstitialView mockAdView;
    @Mock
    private VideoCreative mMockVideoCreative;
    @Mock
    private VideoCreativeView mMockVideoCreativeView;
    @Mock
    InterstitialManager mMockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get().getApplicationContext();
        mAdViewManager = new AdViewManager(mContext, mMockAdViewListener, mockAdView, mMockInterstitialManager);
    }

    @Test
    public void testAdViewManager() throws Exception {
        AdViewManagerListener mockAdViewListener = mock(AdViewManagerListener.class);
        InterstitialView mockAdView = mock(InterstitialView.class);

        AdViewManager adViewManager = new AdViewManager(mContext, mockAdViewListener, mockAdView, mMockInterstitialManager);
        assertNotNull(adViewManager);
    }

    @Test
    public void testAdViewManagerNullAdViewListener() {
        InterstitialView mockAdView = mock(InterstitialView.class);
        AdViewManager adViewManager = null;
        AdException err = null;
        try {
            adViewManager = new AdViewManager(mContext, null, mockAdView, mMockInterstitialManager);
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
            adViewManager = new AdViewManager(mContext, null, mockAdView, mMockInterstitialManager);
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
        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mockTransactionManager);

        mAdViewManager.show();
        verify(mMockAdViewListener).viewReadyForImmediateDisplay(any(View.class));
    }

    @Test
    public void whenBannerHtmlNotResolved_Return() throws IllegalAccessException, AdException {
        ViewGroup mockBanner = mock(ViewGroup.class);
        mAdViewManager = new AdViewManager(mContext, mMockAdViewListener, mockBanner, mock(InterstitialManager.class));

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isDisplay()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        mAdViewManager.show();
        verify(mMockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mMockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void whenInterstitialHtmlNotResolved_Return()
    throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isDisplay()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        mAdViewManager.show();
        verify(mMockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mMockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void whenInterstitialVideoNotResolved_Return()
    throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isDisplay()).thenReturn(false);
        when(mockCreative.isVideo()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        mAdViewManager.show();
        verify(mMockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mMockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void whenVideoNotResolved_Return() throws AdException, IllegalAccessException {
        VideoView mockVideoView = mock(VideoView.class);
        mAdViewManager = new AdViewManager(mContext, mMockAdViewListener, mockVideoView, mock(InterstitialManager.class));

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        when(mockCreative.isVideo()).thenReturn(true);
        when(mockCreative.isResolved()).thenReturn(false);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mockCreative);

        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentCreative()).thenReturn(mockCreative);
        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mockTransactionManager);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);

        mAdViewManager.show();
        verify(mMockAdViewListener, never()).viewReadyForImmediateDisplay(any(View.class));
        verify(mMockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
        assertEquals("SDK internal error: Creative has not been resolved yet", ((AdException) exceptionCaptor.getValue()).getMessage());
    }

    @Test
    public void creativeDidCompleteTest() throws Exception {

        VideoCreative mockVideoCreative = mock(VideoCreative.class);
        when(mockVideoCreative.getCreativeModel())
            .thenReturn(new VideoCreativeModel(mock(TrackingManager.class), mock(OmEventTracker.class), new AdConfiguration()));
        when(mockVideoCreative.isVideo()).thenReturn(true);
        Transaction mockTransaction = mock(Transaction.class);
        ArrayList<CreativeFactory> creativeFactories = new ArrayList<>();
        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        when(mockTransactionManager.getCurrentTransaction()).thenReturn(mockTransaction);
        when(mockTransaction.getCreativeFactories()).thenReturn(creativeFactories);

        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mockTransactionManager);
        WhiteBox.field(AdViewManager.class, "mAdView").set(mAdViewManager, mockAdView);

        mAdViewManager.creativeDidComplete(mockVideoCreative);
        verify(mockAdView).closeInterstitialVideo();
        verify(mMockAdViewListener, times(1)).adCompleted();

        mAdViewManager.creativeDidComplete(mockVideoCreative);
        verify(mMockAdViewListener, times(2)).adCompleted();
    }

    @Test
    public void creativeWasClickedTest() {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        mAdViewManager.creativeWasClicked(mockCreative, "test");
        verify(mMockAdViewListener).creativeClicked(eq("test"));
    }

    @Test
    public void creativeInterstitialDidCloseTest() throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        TransactionManager mock = mock(TransactionManager.class);
        Transaction transaction = mock(Transaction.class);
        when(mock.getCurrentTransaction()).thenReturn(transaction);
        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mock);

        mAdViewManager.creativeInterstitialDidClose(mockCreative);
        verify(mMockAdViewListener).creativeInterstitialClosed();
    }

    @Test
    public void creativeDidExpandTest() {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        mAdViewManager.creativeDidExpand(mockCreative);
        verify(mMockAdViewListener).creativeExpanded();
    }

    @Test
    public void creativeDidCollapseTest() {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        mAdViewManager.creativeDidCollapse(mockCreative);
        verify(mMockAdViewListener).creativeCollapsed();
    }

    @Test
    public void creativeVolumeStateChanged_NotifyListener() {
        mAdViewManager.creativeMuted(mMockVideoCreative);
        verify(mMockAdViewListener).creativeMuted();

        mAdViewManager.creativeUnMuted(mMockVideoCreative);
        verify(mMockAdViewListener).creativeMuted();
    }

    @Test
    public void creativePlaybackStateChanged_NotifyListener() {
        mAdViewManager.creativePaused(mMockVideoCreative);
        verify(mMockAdViewListener).creativePaused();

        mAdViewManager.creativeResumed(mMockVideoCreative);
        verify(mMockAdViewListener).creativeResumed();
    }

    @Test
    public void changeVolumeState_InvokeCreative() throws IllegalAccessException {
        setupCreative();

        mAdViewManager.mute();
        verify(mMockVideoCreative).mute();

        mAdViewManager.unmute();
        verify(mMockVideoCreative).unmute();
    }

    @Test
    public void isNotShowingEndCard_withEndCard_ReturnFalse() throws IllegalAccessException {
        setupCreative();
        when(mMockVideoCreative.isDisplay()).thenReturn(true);
        when(mMockVideoCreative.isEndCard()).thenReturn(true);

        assertFalse(mAdViewManager.isNotShowingEndCard());
    }

    @Test
    public void isNotShowingEndCard_noEndCard_ReturnTrue() throws IllegalAccessException {
        setupCreative();
        when(mMockVideoCreative.isDisplay()).thenReturn(false);
        when(mMockVideoCreative.isEndCard()).thenReturn(true);

        assertTrue(mAdViewManager.isNotShowingEndCard());
    }

    @Test
    public void hasEndCard_withEndCard_ReturnTrue() throws IllegalAccessException {
        setupCreative();
        when(mMockVideoCreative.isDisplay()).thenReturn(true);

        assertTrue(mAdViewManager.hasEndCard());
    }

    @Test
    public void hasEndCard_noEndCard_ReturnFalse() throws IllegalAccessException {
        setupCreative();
        when(mMockVideoCreative.isDisplay()).thenReturn(false);
        when(mMockVideoCreative.isEndCard()).thenReturn(true);

        assertFalse(mAdViewManager.hasEndCard());
    }

    @Test
    public void hideTest() throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);

        mAdViewManager.hide();
        verify(mockAdView, never()).removeView(any(View.class));
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mockCreative);
        mAdViewManager.hide();
        verify(mockAdView).removeView(any());
    }

    @Test
    public void destroy_Cleanup() throws IllegalAccessException {
        setupCreative();
        TransactionManager mockTransactionManager = mock(TransactionManager.class);
        WhiteBox.field(AdViewManager.class, "mTransactionManager").set(mAdViewManager, mockTransactionManager);

        mAdViewManager.destroy();
        verify(mockTransactionManager).destroy();
        verify(mMockVideoCreative).destroy();
    }

    @Test
    public void setAdVisibilityTest() throws IllegalAccessException {
        AbstractCreative mockCreative = mock(AbstractCreative.class);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mockCreative);

        mAdViewManager.setAdVisibility(View.INVISIBLE);
        verify(mockCreative).handleAdWindowNoFocus();

        mAdViewManager.setAdVisibility(View.VISIBLE);
        verify(mockCreative).handleAdWindowFocus();
    }

    @Test
    public void testGetMediaDuration() throws IllegalAccessException {
        long mediaDuration = mAdViewManager.getMediaDuration();
        assertEquals(0, mediaDuration);
        VideoCreative videoCreative = mock(VideoCreative.class);
        long expectedValue = 15 * 1000;
        when(videoCreative.getMediaDuration()).thenReturn(expectedValue);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, videoCreative);
        assertEquals(expectedValue, mAdViewManager.getMediaDuration());
    }

    @Test
    public void testInterstitialClosed() throws IllegalAccessException {
        assertFalse(mAdViewManager.isInterstitialClosed());

        VideoCreative videoCreative = mock(VideoCreative.class);
        final VideoCreativeModel videoCreativeModel = mock(VideoCreativeModel.class);

        when(videoCreative.getCreativeModel()).thenReturn(videoCreativeModel);
        when(videoCreative.isInterstitialClosed()).thenReturn(true);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, videoCreative);
        assertTrue(mAdViewManager.isInterstitialClosed());
        when(videoCreativeModel.hasEndCard()).thenReturn(false);
        WhiteBox.field(VideoCreative.class, "mModel").set(videoCreative, videoCreativeModel);
        when(videoCreative.isInterstitialClosed()).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                videoCreativeModel.trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
                return null;
            }
        });
        mAdViewManager.isInterstitialClosed();
        verify(videoCreativeModel).trackVideoEvent(VideoAdEvent.Event.AD_CLOSE);
    }

    @Test
    public void testIsPlaying() throws IllegalAccessException {
        assertFalse(mAdViewManager.isPlaying());

        setupCreative();

        assertTrue(mAdViewManager.isPlaying());
    }

    private void setupCreative() throws IllegalAccessException {
        when(mMockVideoCreative.isBuiltInVideo()).thenReturn(true);
        when(mMockVideoCreative.isPlaying()).thenReturn(true);
        when(mMockVideoCreative.getCreativeView()).thenReturn(mMockVideoCreativeView);
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mMockVideoCreative);
    }

    @Test
    public void testReturnFromVideo() throws IllegalAccessException {
        View mockCallView = mock(View.class);

        setupCreative();

        when(mMockVideoCreative.isVideo()).thenReturn(true);
        mAdViewManager.returnFromVideo(mockCallView);
        verify(mMockVideoCreativeView).hideCallToAction();
        verify(mMockVideoCreative).updateAdView(mockCallView);
        verify(mMockVideoCreativeView).mute();
        verify(mMockVideoCreative).onPlayerStateChanged(InternalPlayerState.NORMAL);
    }

    @Test
    public void testCanShowFullScreen() throws IllegalAccessException {
        assertFalse(mAdViewManager.canShowFullScreen());
        setupCreative();
        assertTrue(mAdViewManager.canShowFullScreen());
    }

    @Test
    public void testTrackVideoStateChange() throws IllegalAccessException {
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mMockVideoCreative);
        when(mMockVideoCreative.isVideo()).thenReturn(true);

        mAdViewManager.trackVideoStateChange(InternalPlayerState.NORMAL);

        verify(mMockVideoCreative).trackVideoStateChange(eq(InternalPlayerState.NORMAL));
    }

    @Test
    public void testUpdateAdView() throws IllegalAccessException {
        setupCreative();
        View mock = mock(View.class);
        mAdViewManager.updateAdView(mock);
        verify(mMockVideoCreative).updateAdView(mock);
    }

    @Test
    public void whenConditionNotMet_ResumeNotCalled() {
        mAdViewManager.resume();
        verify(mMockVideoCreative, never()).resume();
    }

    @Test
    public void whenConditionNotMet_PauseNotCalled() {
        mAdViewManager.pause();
        verify(mMockVideoCreative, never()).pause();
    }

    @Test
    public void whenResumeCalled_CreativeResumeCalled() throws IllegalAccessException {
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mMockVideoCreative);
        when(mMockVideoCreative.isVideo()).thenReturn(true);
        mAdViewManager.resume();
        verify(mMockVideoCreative).resume();
    }

    @Test
    public void whenResumeCalled_CreativePauseCalled() throws IllegalAccessException {
        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(mAdViewManager, mMockVideoCreative);
        when(mMockVideoCreative.isVideo()).thenReturn(true);
        mAdViewManager.pause();
        verify(mMockVideoCreative).pause();
    }

    @Test
    public void whenFetchedWithException_callFailedToLoad() {
        AdException adException = new AdException(AdException.INTERNAL_ERROR, "Error message");
        mAdViewManager.onFetchingFailed(adException);

        ArgumentCaptor exceptionCaptor = ArgumentCaptor.forClass(AdException.class);
        verify(mMockAdViewListener).failedToLoad((AdException) exceptionCaptor.capture());
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

        mAdViewManager.onFetchingCompleted(mockTransaction);
        verify(mockCreative).createOmAdSession();
        verify(mMockAdViewListener).adLoaded(any(AdDetails.class));
    }
}
