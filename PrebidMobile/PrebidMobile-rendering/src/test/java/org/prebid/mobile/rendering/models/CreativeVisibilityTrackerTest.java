package org.prebid.mobile.rendering.models;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker.VisibilityTrackerListener;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSystemClock;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeVisibilityTrackerTest {

    private final VisibilityTrackerOption mVisibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);

    private Activity mActivity;
    private CreativeVisibilityTracker mCreativeVisibilityTracker;
    private Handler mVisibilityHandler;

    private VisibilityChecker mSpyVisibilityChecker;
    @Mock
    private View mMockView;
    @Mock
    private VisibilityTrackerListener mMockVisibilityTrackerListener;
    @Mock
    private Window mMockWindow;
    @Mock
    private View mMockDecorView;
    @Mock
    private ViewTreeObserver mMockViewTreeObserver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mActivity = Robolectric.buildActivity(Activity.class).create().get();

        mCreativeVisibilityTracker = new CreativeVisibilityTracker(mMockView, mVisibilityTrackerOption);
        mCreativeVisibilityTracker.setVisibilityTrackerListener(mMockVisibilityTrackerListener);

        mVisibilityHandler = (Handler) getFieldValue("mVisibilityHandler");

        mSpyVisibilityChecker = spy(new VisibilityChecker(mVisibilityTrackerOption));
        List<VisibilityChecker> visibilityCheckerList = new ArrayList<>();
        visibilityCheckerList.add(mSpyVisibilityChecker);
        WhiteBox.setInternalState(mCreativeVisibilityTracker, "mVisibilityCheckerList", visibilityCheckerList);

        // We need this to ensure that our SystemClock starts
        ShadowSystemClock.currentTimeMillis();
    }

    @Test
    public void startVisibilityCheck_shouldSetOnPreDrawListenerForDecorView() throws Exception {
        Activity spyActivity = spy(mActivity);

        when(spyActivity.getWindow()).thenReturn(mMockWindow);
        when(mMockWindow.getDecorView()).thenReturn(mMockDecorView);
        when(mMockDecorView.findViewById(anyInt())).thenReturn(mMockDecorView);
        when(mMockDecorView.getViewTreeObserver()).thenReturn(mMockViewTreeObserver);
        when(mMockViewTreeObserver.isAlive()).thenReturn(true);

        mCreativeVisibilityTracker = new CreativeVisibilityTracker(mMockView, mVisibilityTrackerOption);
        mCreativeVisibilityTracker.startVisibilityCheck(spyActivity);
        ViewTreeObserver.OnPreDrawListener expectedListener = (ViewTreeObserver.OnPreDrawListener) getFieldValue("mOnPreDrawListener");
        assertNotNull(expectedListener);
        verify(mMockViewTreeObserver).addOnPreDrawListener(eq(expectedListener));
        assertEquals(mMockViewTreeObserver, ((WeakReference<ViewTreeObserver>) getFieldValue("mWeakViewTreeObserver")).get());
    }

    @Test
    public void startVisibilityCheckWithNonAliveViewTreeObserver_shouldNotSetOnPreDrawListenerForDecorView()
    throws Exception {
        Activity mockActivity = mock(Activity.class);

        when(mockActivity.getWindow()).thenReturn(mMockWindow);
        when(mMockWindow.getDecorView()).thenReturn(mMockDecorView);
        when(mMockDecorView.getViewTreeObserver()).thenReturn(mMockViewTreeObserver);
        when(mMockViewTreeObserver.isAlive()).thenReturn(false);

        mCreativeVisibilityTracker = new CreativeVisibilityTracker(mMockView, mVisibilityTrackerOption);
        mCreativeVisibilityTracker.startVisibilityCheck(mockActivity);
        verify(mMockViewTreeObserver, never()).addOnPreDrawListener(any(ViewTreeObserver.OnPreDrawListener.class));
        assertNull(((WeakReference<ViewTreeObserver>) getFieldValue("mWeakViewTreeObserver")).get());
    }

    @Test
    public void startVisibilityCheckWithApplicationContext_shouldNotSetOnPreDrawListener()
    throws IllegalAccessException {
        mCreativeVisibilityTracker = new CreativeVisibilityTracker(mMockView, mVisibilityTrackerOption);
        mCreativeVisibilityTracker.startVisibilityCheck(mActivity.getApplicationContext());

        assertNull(((WeakReference<ViewTreeObserver>) getFieldValue("mWeakViewTreeObserver")).get());
    }

    @Test
    public void startVisibilityCheckWithViewTreeObserverNotSet_shouldSetViewTreeObserver()
    throws IllegalAccessException {
        View mockRootView = mock(View.class);

        when(mMockView.getContext()).thenReturn(mActivity.getApplicationContext());
        when(mMockView.getRootView()).thenReturn(mockRootView);
        when(mockRootView.getViewTreeObserver()).thenReturn(mMockViewTreeObserver);
        when(mMockViewTreeObserver.isAlive()).thenReturn(true);

        mCreativeVisibilityTracker = new CreativeVisibilityTracker(mMockView, mVisibilityTrackerOption);
        mCreativeVisibilityTracker.startVisibilityCheck(mActivity.getApplicationContext());
        assertEquals(mMockViewTreeObserver, ((WeakReference<ViewTreeObserver>) getFieldValue("mWeakViewTreeObserver")).get());
    }

    @Test
    public void destroy_shouldRemoveListenerFromDecorView() throws Exception {
        Activity spyActivity = spy(mActivity);

        when(spyActivity.getWindow()).thenReturn(mMockWindow);
        when(mMockWindow.getDecorView()).thenReturn(mMockDecorView);
        when(mMockDecorView.findViewById(anyInt())).thenReturn(mMockDecorView);
        when(mMockDecorView.getViewTreeObserver()).thenReturn(mMockViewTreeObserver);
        when(mMockViewTreeObserver.isAlive()).thenReturn(true);

        mCreativeVisibilityTracker = new CreativeVisibilityTracker(mMockView, mVisibilityTrackerOption);
        mCreativeVisibilityTracker.startVisibilityCheck(spyActivity);
        mCreativeVisibilityTracker.stopVisibilityCheck();

        assertFalse(mVisibilityHandler.hasMessages(0));
        assertFalse((Boolean) getFieldValue("mIsVisibilityScheduled"));
        verify(mMockViewTreeObserver).removeOnPreDrawListener(any(ViewTreeObserver.OnPreDrawListener.class));
        assertNull(((WeakReference<ViewTreeObserver>) getFieldValue("mWeakViewTreeObserver")).get());
        assertNull(getFieldValue("mVisibilityTrackerListener"));
    }

    // VisibilityRunnable Tests
    @Test
    public void visibilityRunnable_runWithViewVisibleForAtLeastMinDuration_CallOnVisibilityChanged_ImpTrackerFiredTrue_IsVisibilityScheduledFalse()
    throws Exception {
        ViewExposure viewExposure = new ViewExposure(100, new Rect(0, 0, 100, 100), null);
        when(mSpyVisibilityChecker.isVisible(any(View.class))).thenReturn(true);
        when(mSpyVisibilityChecker.checkViewExposure(any(View.class))).thenReturn(viewExposure);
        when(mSpyVisibilityChecker.hasBeenVisible()).thenReturn(true);
        when(mSpyVisibilityChecker.hasRequiredTimeElapsed()).thenReturn(true);

        mCreativeVisibilityTracker.mVisibilityRunnable.run();

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     viewExposure, true, true);
        verify(mMockVisibilityTrackerListener).onVisibilityChanged(result);
        final VisibilityTrackerOption visibilityTrackerOption = mSpyVisibilityChecker.getVisibilityTrackerOption();
        assertTrue(visibilityTrackerOption.isImpressionTracked());
        assertFalse((Boolean) getFieldValue("mIsVisibilityScheduled"));
    }

    @Test
    public void visibilityRunnable_runWithViewNotVisible_CallOnVisibilityChangedWithFalseValues_ImpTrackerFiredFalse_IsVisibilityScheduledTrue()
    throws Exception {
        when(mMockView.getVisibility()).thenReturn(View.INVISIBLE);
        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     null, false, false);
        when(mSpyVisibilityChecker.checkViewExposure(any(View.class))).thenReturn(null);

        mCreativeVisibilityTracker.mVisibilityRunnable.run();

        verify(mMockVisibilityTrackerListener).onVisibilityChanged(result);
        final VisibilityTrackerOption visibilityTrackerOption = mSpyVisibilityChecker.getVisibilityTrackerOption();
        assertFalse(visibilityTrackerOption.isImpressionTracked());
        assertTrue((Boolean) getFieldValue("mIsVisibilityScheduled"));
    }

    @Test
    public void visibilityRunnable_runWitViewVisibleForLessThanMinDuration_ShouldNotCallOnVisibilityChanged_ImpTrackerFiredFalse_IsVisibilityScheduledTrue()
    throws Exception {
        ViewExposure viewExposure = new ViewExposure(5.0f, null, null);
        when(mSpyVisibilityChecker.isVisible(any(View.class))).thenReturn(true);
        when(mSpyVisibilityChecker.hasBeenVisible()).thenReturn(false);
        when(mSpyVisibilityChecker.hasRequiredTimeElapsed()).thenReturn(false);
        when(mSpyVisibilityChecker.checkViewExposure(any(View.class))).thenReturn(viewExposure);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     viewExposure, true, false);

        mCreativeVisibilityTracker.mVisibilityRunnable.run();

        verify(mMockVisibilityTrackerListener).onVisibilityChanged(result);
        final VisibilityTrackerOption visibilityTrackerOption = mSpyVisibilityChecker.getVisibilityTrackerOption();
        assertFalse(visibilityTrackerOption.isImpressionTracked());
        assertTrue((Boolean) getFieldValue("mIsVisibilityScheduled"));
    }

    // VisibilityChecker Tests
    @Test
    public void hasRequiredTimeElapsedWithStartTimeNotSet_ReturnFalse() {
        assertFalse(mSpyVisibilityChecker.hasRequiredTimeElapsed());
    }

    @Test
    public void hasRequiredTimeElapsedWithStartTimeSet_And_ElapsedTimeGreaterThanMinTimeViewed_ReturnTrue() {
        mSpyVisibilityChecker = new VisibilityChecker(mVisibilityTrackerOption);
        mSpyVisibilityChecker.setStartTimeMillis();

        // minVisibleMillis is 0 ms as defined by constant MIN_VISIBLE_MILLIS
        assertTrue(mSpyVisibilityChecker.hasRequiredTimeElapsed());
    }

    @Test
    public void hasRequiredTimeElapsedWithStartTimeSet_And_ElapsedTimeLessThanMinTimeViewed_ReturnFalse() {
        mSpyVisibilityChecker = new VisibilityChecker(new VisibilityTrackerOption(NativeEventTracker.EventType.VIEWABLE_MRC50));
        mSpyVisibilityChecker.setStartTimeMillis();

        // minVisibleMillis is 1 sec, should return false since we are checking immediately before 1 sec elapses
        assertFalse(mSpyVisibilityChecker.hasRequiredTimeElapsed());
    }

    @Test
    public void isVisibleWhenViewIsNull_ReturnFalse() {
        assertNull(mSpyVisibilityChecker.checkViewExposure(null));
    }

    @Test
    public void visibilityRunnable_runWithMultipleTrackingOptions_CallOnVisibilityChangedIndependently_NoTrackForVideo() {
        mockView();

        VisibilityTrackerOption trackerOptionImpression = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        VisibilityTrackerOption trackerOptionMrc50 = new VisibilityTrackerOption(NativeEventTracker.EventType.VIEWABLE_MRC50);
        VisibilityTrackerOption trackerOptionMrc100 = new VisibilityTrackerOption(NativeEventTracker.EventType.VIEWABLE_MRC100);
        VisibilityTrackerOption trackerOptionViewableVideo50 = new VisibilityTrackerOption(NativeEventTracker.EventType.VIEWABLE_VIDEO50);
        final ViewExposure viewExposure = new ViewExposure(1.0f, new Rect(), new ArrayList<>());

        Set<VisibilityTrackerOption> visibilityTrackerOptionSet = new HashSet<>();
        visibilityTrackerOptionSet.add(trackerOptionImpression);
        visibilityTrackerOptionSet.add(trackerOptionMrc50);
        visibilityTrackerOptionSet.add(trackerOptionMrc100);
        visibilityTrackerOptionSet.add(trackerOptionViewableVideo50);

        CreativeVisibilityTracker creativeVisibilityTracker = new CreativeVisibilityTracker(mMockView, visibilityTrackerOptionSet);
        creativeVisibilityTracker.setVisibilityTrackerListener(mMockVisibilityTrackerListener);
        mockVisibilityChecker(creativeVisibilityTracker, viewExposure, visibilityTrackerOptionSet);

        creativeVisibilityTracker.mVisibilityRunnable.run();

        // 0 sec visibility duration
        assertTrue(trackerOptionImpression.isImpressionTracked());
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));

        // 1 sec visibility duration
        assertTrue(trackerOptionMrc50.isImpressionTracked());
        assertTrue(trackerOptionMrc100.isImpressionTracked());
        // 2 sec visibility duration
        assertFalse(trackerOptionViewableVideo50.isImpressionTracked());

        VisibilityTrackerResult impressionResult = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                               viewExposure, true, true);
        VisibilityTrackerResult mrc50Result = new VisibilityTrackerResult(NativeEventTracker.EventType.VIEWABLE_MRC50,
                                                                          viewExposure, true, true);
        VisibilityTrackerResult mrc100Result = new VisibilityTrackerResult(NativeEventTracker.EventType.VIEWABLE_MRC100,
                                                                           viewExposure, true, true);
        VisibilityTrackerResult videoResult = new VisibilityTrackerResult(NativeEventTracker.EventType.VIEWABLE_VIDEO50,
                                                                          viewExposure, true, true);

        verify(mMockVisibilityTrackerListener, times(1)).onVisibilityChanged(eq(impressionResult));
        verify(mMockVisibilityTrackerListener, times(1)).onVisibilityChanged(eq(mrc50Result));
        verify(mMockVisibilityTrackerListener, times(1)).onVisibilityChanged(eq(mrc100Result));
        verify(mMockVisibilityTrackerListener, times(0)).onVisibilityChanged(eq(videoResult));
    }

    private Object getFieldValue(String fieldName) {
        return WhiteBox.getInternalState(mCreativeVisibilityTracker, fieldName);
    }

    private void mockView() {
        final ViewParent mockParent = mock(ViewParent.class);
        when(mMockView.getWidth()).thenReturn(200);
        when(mMockView.getHeight()).thenReturn(300);
        when(mMockView.isShown()).thenReturn(true);
        when(mMockView.hasWindowFocus()).thenReturn(true);
        when(mockParent.getParent()).thenReturn(mockParent);
        when(mMockView.getParent()).thenReturn(mockParent);
        when(mMockView.getContext()).thenReturn(mActivity);
        doAnswer(invocation -> {
            final Rect clipRect = invocation.getArgument(0);
            clipRect.right = 200;
            clipRect.bottom = 300;
            return true;
        }).when(mMockView).getGlobalVisibleRect(any(Rect.class));
    }

    private void mockVisibilityChecker(CreativeVisibilityTracker visibilityTracker, ViewExposure desiredViewExposure, Set<VisibilityTrackerOption> optionSet) {
        List<VisibilityChecker> visibilityCheckerList = new ArrayList<>();
        for (VisibilityTrackerOption trackerOption : optionSet) {
            VisibilityChecker visibilityChecker = spy(new VisibilityChecker(trackerOption));
            when(visibilityChecker.checkViewExposure(any(View.class))).thenReturn(desiredViewExposure);
            visibilityCheckerList.add(visibilityChecker);
        }
        WhiteBox.setInternalState(visibilityTracker, "mVisibilityCheckerList", visibilityCheckerList);
    }
}