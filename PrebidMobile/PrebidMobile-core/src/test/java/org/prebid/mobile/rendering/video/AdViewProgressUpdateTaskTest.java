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

package org.prebid.mobile.rendering.video;

import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdViewProgressUpdateTaskTest {

    private AdViewProgressUpdateTask mAdViewProgressTask;
    private VideoCreative mMockVideoCreative;
    private View mMockCreativeView;
    private final int mDuration = 50;

    @Before
    public void setup() throws AdException {
        mMockVideoCreative = mock(VideoCreative.class);
        mMockCreativeView = mock(VideoCreativeView.class);

        when(mMockVideoCreative.getCreativeView()).thenReturn(mMockCreativeView);

        mAdViewProgressTask = spy(new AdViewProgressUpdateTask(mMockVideoCreative, mDuration));
    }

    @Test
    public void onPostExecuteTest() {
        mAdViewProgressTask.onPostExecute(null);
        verify(mAdViewProgressTask).cancel(true);
    }

    @Test
    public void onProgressUpdateTest() {
        assertFalse(mAdViewProgressTask.getFirstQuartile());
        mAdViewProgressTask.onProgressUpdate((long) 25);
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        assertTrue(mAdViewProgressTask.getFirstQuartile());

        reset(mMockVideoCreative);
        assertFalse(mAdViewProgressTask.getMidpoint());
        mAdViewProgressTask.onProgressUpdate((long) 50);
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        assertTrue(mAdViewProgressTask.getMidpoint());

        reset(mMockVideoCreative);
        assertFalse(mAdViewProgressTask.getThirdQuartile());
        mAdViewProgressTask.onProgressUpdate((long) 75);
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
        assertTrue(mAdViewProgressTask.getThirdQuartile());
    }

    @Test
    public void getCurrentPositionTest() {
        assertEquals(0, mAdViewProgressTask.getCurrentPosition());
    }

    @Test
    public void setVastVideoDurationTest() throws IllegalAccessException, NoSuchFieldException {
        Field vastDurationField = WhiteBox.field(AdViewProgressUpdateTask.class, "mVastVideoDuration");
        assertEquals((long) -1, vastDurationField.get(mAdViewProgressTask));

        mAdViewProgressTask.setVastVideoDuration(10);
        assertEquals((long) 10, vastDurationField.get(mAdViewProgressTask));
    }

    @Test
    public void whenNoQuartilePassed_NoEventsTracked() {
        assertFalse(mAdViewProgressTask.getFirstQuartile());
        assertFalse(mAdViewProgressTask.getMidpoint());
        assertFalse(mAdViewProgressTask.getThirdQuartile());
        mAdViewProgressTask.onProgressUpdate((long) 24);

        assertFalse(mAdViewProgressTask.getFirstQuartile());
        assertFalse(mAdViewProgressTask.getMidpoint());
        assertFalse(mAdViewProgressTask.getThirdQuartile());
        verify(mMockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mMockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mMockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }

    @Test
    public void whenFirstQuartilePassedAndNoEventsTracked_TrackOnlyFirstQuartile() {
        assertFalse(mAdViewProgressTask.getFirstQuartile());
        assertFalse(mAdViewProgressTask.getMidpoint());
        assertFalse(mAdViewProgressTask.getThirdQuartile());

        mAdViewProgressTask.onProgressUpdate((long) 49);

        assertTrue(mAdViewProgressTask.getFirstQuartile());
        assertFalse(mAdViewProgressTask.getMidpoint());
        assertFalse(mAdViewProgressTask.getThirdQuartile());
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mMockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mMockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }

    @Test
    public void whenMidpointPassedAndNoEventsTracked_TrackFirstQuartileAndMidpoint() {
        assertFalse(mAdViewProgressTask.getFirstQuartile());
        assertFalse(mAdViewProgressTask.getMidpoint());
        assertFalse(mAdViewProgressTask.getThirdQuartile());

        mAdViewProgressTask.onProgressUpdate((long) 74);

        assertTrue(mAdViewProgressTask.getFirstQuartile());
        assertTrue(mAdViewProgressTask.getMidpoint());
        assertFalse(mAdViewProgressTask.getThirdQuartile());
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mMockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }

    @Test
    public void whenThirdQuartilePassedAndNoEventsTracked_TrackAllEvents() {
        assertFalse(mAdViewProgressTask.getFirstQuartile());
        assertFalse(mAdViewProgressTask.getMidpoint());
        assertFalse(mAdViewProgressTask.getThirdQuartile());

        mAdViewProgressTask.onProgressUpdate((long) 99);

        assertTrue(mAdViewProgressTask.getFirstQuartile());
        assertTrue(mAdViewProgressTask.getMidpoint());
        assertTrue(mAdViewProgressTask.getThirdQuartile());
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }
}