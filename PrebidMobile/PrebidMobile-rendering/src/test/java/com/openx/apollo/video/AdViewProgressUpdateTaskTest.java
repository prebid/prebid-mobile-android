package com.openx.apollo.video;

import android.view.View;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.errors.AdException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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