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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeVisibilityTrackerTest {

    private final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);

    private Activity activity;
    private CreativeVisibilityTracker creativeVisibilityTracker;
    private Handler visibilityHandler;

    private VisibilityChecker spyVisibilityChecker;
    @Mock private View mockView;
    @Mock private VisibilityTrackerListener mockVisibilityTrackerListener;
    @Mock private Window mockWindow;
    @Mock private View mockDecorView;
    @Mock private ViewTreeObserver mockViewTreeObserver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.buildActivity(Activity.class).create().get();

        creativeVisibilityTracker = new CreativeVisibilityTracker(mockView, visibilityTrackerOption);
        creativeVisibilityTracker.setVisibilityTrackerListener(mockVisibilityTrackerListener);

        visibilityHandler = (Handler) getFieldValue("visibilityHandler");

        spyVisibilityChecker = spy(new VisibilityChecker(visibilityTrackerOption));
        List<VisibilityChecker> visibilityCheckerList = new ArrayList<>();
        visibilityCheckerList.add(spyVisibilityChecker);
        WhiteBox.setInternalState(creativeVisibilityTracker, "visibilityCheckerList", visibilityCheckerList);

        // We need this to ensure that our SystemClock starts
        ShadowSystemClock.currentTimeMillis();
    }

    @Test
    public void startVisibilityCheck_shouldSetOnPreDrawListenerForDecorView() throws Exception {
        Activity spyActivity = spy(activity);

        when(spyActivity.getWindow()).thenReturn(mockWindow);
        when(mockWindow.getDecorView()).thenReturn(mockDecorView);
        when(mockDecorView.findViewById(anyInt())).thenReturn(mockDecorView);
        when(mockDecorView.getViewTreeObserver()).thenReturn(mockViewTreeObserver);
        when(mockViewTreeObserver.isAlive()).thenReturn(true);

        creativeVisibilityTracker = new CreativeVisibilityTracker(mockView, visibilityTrackerOption);
        creativeVisibilityTracker.startVisibilityCheck(spyActivity);
        ViewTreeObserver.OnPreDrawListener expectedListener = (ViewTreeObserver.OnPreDrawListener) getFieldValue(
                "onPreDrawListener");
        assertNotNull(expectedListener);
        verify(mockViewTreeObserver).addOnPreDrawListener(eq(expectedListener));
        assertEquals(
                mockViewTreeObserver,
                ((WeakReference<ViewTreeObserver>) getFieldValue("weakViewTreeObserver")).get()
        );
    }

    @Test
    public void startVisibilityCheckWithNonAliveViewTreeObserver_shouldNotSetOnPreDrawListenerForDecorView()
    throws Exception {
        Activity mockActivity = mock(Activity.class);

        when(mockActivity.getWindow()).thenReturn(mockWindow);
        when(mockWindow.getDecorView()).thenReturn(mockDecorView);
        when(mockDecorView.getViewTreeObserver()).thenReturn(mockViewTreeObserver);
        when(mockViewTreeObserver.isAlive()).thenReturn(false);

        creativeVisibilityTracker = new CreativeVisibilityTracker(mockView, visibilityTrackerOption);
        creativeVisibilityTracker.startVisibilityCheck(mockActivity);
        verify(mockViewTreeObserver, never()).addOnPreDrawListener(any(ViewTreeObserver.OnPreDrawListener.class));
        assertNull(((WeakReference<ViewTreeObserver>) getFieldValue("weakViewTreeObserver")).get());
    }

    @Test
    public void startVisibilityCheckWithApplicationContext_shouldNotSetOnPreDrawListener()
    throws IllegalAccessException {
        creativeVisibilityTracker = new CreativeVisibilityTracker(mockView, visibilityTrackerOption);
        creativeVisibilityTracker.startVisibilityCheck(activity.getApplicationContext());

        assertNull(((WeakReference<ViewTreeObserver>) getFieldValue("weakViewTreeObserver")).get());
    }

    @Test
    public void startVisibilityCheckWithViewTreeObserverNotSet_shouldSetViewTreeObserver()
    throws IllegalAccessException {
        View mockRootView = mock(View.class);

        when(mockView.getContext()).thenReturn(activity.getApplicationContext());
        when(mockView.getRootView()).thenReturn(mockRootView);
        when(mockRootView.getViewTreeObserver()).thenReturn(mockViewTreeObserver);
        when(mockViewTreeObserver.isAlive()).thenReturn(true);

        creativeVisibilityTracker = new CreativeVisibilityTracker(mockView, visibilityTrackerOption);
        creativeVisibilityTracker.startVisibilityCheck(activity.getApplicationContext());
        assertEquals(
                mockViewTreeObserver,
                ((WeakReference<ViewTreeObserver>) getFieldValue("weakViewTreeObserver")).get()
        );
    }

    @Test
    public void destroy_shouldRemoveListenerFromDecorView() throws Exception {
        Activity spyActivity = spy(activity);

        when(spyActivity.getWindow()).thenReturn(mockWindow);
        when(mockWindow.getDecorView()).thenReturn(mockDecorView);
        when(mockDecorView.findViewById(anyInt())).thenReturn(mockDecorView);
        when(mockDecorView.getViewTreeObserver()).thenReturn(mockViewTreeObserver);
        when(mockViewTreeObserver.isAlive()).thenReturn(true);

        creativeVisibilityTracker = new CreativeVisibilityTracker(mockView, visibilityTrackerOption);
        creativeVisibilityTracker.startVisibilityCheck(spyActivity);
        creativeVisibilityTracker.stopVisibilityCheck();

        assertFalse(visibilityHandler.hasMessages(0));
        assertFalse((Boolean) getFieldValue("isVisibilityScheduled"));
        verify(mockViewTreeObserver).removeOnPreDrawListener(any(ViewTreeObserver.OnPreDrawListener.class));
        assertNull(((WeakReference<ViewTreeObserver>) getFieldValue("weakViewTreeObserver")).get());
        assertNull(getFieldValue("visibilityTrackerListener"));
    }

    // VisibilityRunnable Tests
    @Test
    public void visibilityRunnable_runWithViewVisibleForAtLeastMinDuration_CallOnVisibilityChanged_ImpTrackerFiredTrue_IsVisibilityScheduledFalse()
    throws Exception {
        ViewExposure viewExposure = new ViewExposure(100, new Rect(0, 0, 100, 100), null);
        when(spyVisibilityChecker.isVisible(any(View.class))).thenReturn(true);
        when(spyVisibilityChecker.checkViewExposure(any(View.class))).thenReturn(viewExposure);
        when(spyVisibilityChecker.hasBeenVisible()).thenReturn(true);
        when(spyVisibilityChecker.hasRequiredTimeElapsed()).thenReturn(true);

        creativeVisibilityTracker.visibilityRunnable.run();

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                viewExposure,
                true,
                true
        );
        verify(mockVisibilityTrackerListener).onVisibilityChanged(result);
        final VisibilityTrackerOption visibilityTrackerOption = spyVisibilityChecker.getVisibilityTrackerOption();
        assertTrue(visibilityTrackerOption.isImpressionTracked());
        assertFalse((Boolean) getFieldValue("isVisibilityScheduled"));
    }

    @Test
    public void visibilityRunnable_runWithViewNotVisible_CallOnVisibilityChangedWithFalseValues_ImpTrackerFiredFalse_IsVisibilityScheduledTrue()
    throws Exception {
        when(mockView.getVisibility()).thenReturn(View.INVISIBLE);
        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                null,
                false,
                false
        );
        when(spyVisibilityChecker.checkViewExposure(any(View.class))).thenReturn(null);

        creativeVisibilityTracker.visibilityRunnable.run();

        verify(mockVisibilityTrackerListener).onVisibilityChanged(result);
        final VisibilityTrackerOption visibilityTrackerOption = spyVisibilityChecker.getVisibilityTrackerOption();
        assertFalse(visibilityTrackerOption.isImpressionTracked());
        assertTrue((Boolean) getFieldValue("isVisibilityScheduled"));
    }

    @Test
    public void visibilityRunnable_runWitViewVisibleForLessThanMinDuration_ShouldNotCallOnVisibilityChanged_ImpTrackerFiredFalse_IsVisibilityScheduledTrue()
    throws Exception {
        ViewExposure viewExposure = new ViewExposure(5.0f, null, null);
        when(spyVisibilityChecker.isVisible(any(View.class))).thenReturn(true);
        when(spyVisibilityChecker.hasBeenVisible()).thenReturn(false);
        when(spyVisibilityChecker.hasRequiredTimeElapsed()).thenReturn(false);
        when(spyVisibilityChecker.checkViewExposure(any(View.class))).thenReturn(viewExposure);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                viewExposure,
                true,
                false
        );

        creativeVisibilityTracker.visibilityRunnable.run();

        verify(mockVisibilityTrackerListener).onVisibilityChanged(result);
        final VisibilityTrackerOption visibilityTrackerOption = spyVisibilityChecker.getVisibilityTrackerOption();
        assertFalse(visibilityTrackerOption.isImpressionTracked());
        assertTrue((Boolean) getFieldValue("isVisibilityScheduled"));
    }

    // VisibilityChecker Tests
    @Test
    public void hasRequiredTimeElapsedWithStartTimeNotSet_ReturnFalse() {
        assertFalse(spyVisibilityChecker.hasRequiredTimeElapsed());
    }

    @Test
    public void hasRequiredTimeElapsedWithStartTimeSet_And_ElapsedTimeGreaterThanMinTimeViewed_ReturnTrue() {
        spyVisibilityChecker = new VisibilityChecker(visibilityTrackerOption);
        spyVisibilityChecker.setStartTimeMillis();

        // minVisibleMillis is 0 ms as defined by constant MIN_VISIBLE_MILLIS
        assertTrue(spyVisibilityChecker.hasRequiredTimeElapsed());
    }

    @Test
    public void hasRequiredTimeElapsedWithStartTimeSet_And_ElapsedTimeLessThanMinTimeViewed_ReturnFalse() {
        spyVisibilityChecker = new VisibilityChecker(new VisibilityTrackerOption(NativeEventTracker.EventType.VIEWABLE_MRC50));
        spyVisibilityChecker.setStartTimeMillis();

        // minVisibleMillis is 1 sec, should return false since we are checking immediately before 1 sec elapses
        assertFalse(spyVisibilityChecker.hasRequiredTimeElapsed());
    }

    @Test
    public void isVisibleWhenViewIsNull_ReturnFalse() {
        assertNull(spyVisibilityChecker.checkViewExposure(null));
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

        CreativeVisibilityTracker creativeVisibilityTracker = new CreativeVisibilityTracker(
                mockView,
                visibilityTrackerOptionSet
        );
        creativeVisibilityTracker.setVisibilityTrackerListener(mockVisibilityTrackerListener);
        mockVisibilityChecker(creativeVisibilityTracker, viewExposure, visibilityTrackerOptionSet);

        creativeVisibilityTracker.visibilityRunnable.run();

        // 0 sec visibility duration
        assertTrue(trackerOptionImpression.isImpressionTracked());
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));

        // 1 sec visibility duration
        assertTrue(trackerOptionMrc50.isImpressionTracked());
        assertTrue(trackerOptionMrc100.isImpressionTracked());
        // 2 sec visibility duration
        assertFalse(trackerOptionViewableVideo50.isImpressionTracked());

        VisibilityTrackerResult impressionResult = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                viewExposure,
                true,
                true
        );
        VisibilityTrackerResult mrc50Result = new VisibilityTrackerResult(NativeEventTracker.EventType.VIEWABLE_MRC50,
                viewExposure,
                true,
                true
        );
        VisibilityTrackerResult mrc100Result = new VisibilityTrackerResult(NativeEventTracker.EventType.VIEWABLE_MRC100,
                viewExposure,
                true,
                true
        );
        VisibilityTrackerResult videoResult = new VisibilityTrackerResult(NativeEventTracker.EventType.VIEWABLE_VIDEO50,
                viewExposure,
                true,
                true
        );

        verify(mockVisibilityTrackerListener, times(1)).onVisibilityChanged(eq(impressionResult));
        verify(mockVisibilityTrackerListener, times(1)).onVisibilityChanged(eq(mrc50Result));
        verify(mockVisibilityTrackerListener, times(1)).onVisibilityChanged(eq(mrc100Result));
        verify(mockVisibilityTrackerListener, times(0)).onVisibilityChanged(eq(videoResult));
    }

    private Object getFieldValue(String fieldName) {
        return WhiteBox.getInternalState(creativeVisibilityTracker, fieldName);
    }

    private void mockView() {
        final ViewParent mockParent = mock(ViewParent.class);
        when(mockView.getWidth()).thenReturn(200);
        when(mockView.getHeight()).thenReturn(300);
        when(mockView.isShown()).thenReturn(true);
        when(mockView.hasWindowFocus()).thenReturn(true);
        when(mockParent.getParent()).thenReturn(mockParent);
        when(mockView.getParent()).thenReturn(mockParent);
        when(mockView.getContext()).thenReturn(activity);
        doAnswer(invocation -> {
            final Rect clipRect = invocation.getArgument(0);
            clipRect.right = 200;
            clipRect.bottom = 300;
            return true;
        }).when(mockView).getGlobalVisibleRect(any(Rect.class));
    }

    private void mockVisibilityChecker(CreativeVisibilityTracker visibilityTracker, ViewExposure desiredViewExposure, Set<VisibilityTrackerOption> optionSet) {
        List<VisibilityChecker> visibilityCheckerList = new ArrayList<>();
        for (VisibilityTrackerOption trackerOption : optionSet) {
            VisibilityChecker visibilityChecker = spy(new VisibilityChecker(trackerOption));
            when(visibilityChecker.checkViewExposure(any(View.class))).thenReturn(desiredViewExposure);
            visibilityCheckerList.add(visibilityChecker);
        }
        WhiteBox.setInternalState(visibilityTracker, "visibilityCheckerList", visibilityCheckerList);
    }
}