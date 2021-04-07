package com.openx.apollo.video;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.errors.AdException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoCreativeViewTest {

    private VideoCreativeView mVideoCreativeView;
    private VideoCreative mMockCreative;

    @Before
    public void setUp() throws AdException {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mMockCreative = Mockito.mock(VideoCreative.class);

        mVideoCreativeView = new VideoCreativeView(context, mMockCreative);
    }

    @Test
    public void startTest() throws IllegalAccessException {
        VideoPlayerView mockPlugPlayView = mock(ExoPlayerView.class);
        WhiteBox.field(VideoCreativeView.class, "mExoPlayerView").set(mVideoCreativeView, mockPlugPlayView);

        mVideoCreativeView.start(anyInt());
        verify(mockPlugPlayView).start(anyInt());
    }

    @Test
    public void destroyTest() throws IllegalAccessException {
        VideoPlayerView mockPlugPlayView = mock(ExoPlayerView.class);
        View mockLytCallToActionOverlay = mock(View.class);
        WhiteBox.field(VideoCreativeView.class, "mExoPlayerView").set(mVideoCreativeView, mockPlugPlayView);
        mVideoCreativeView.addView(mockLytCallToActionOverlay);

        mVideoCreativeView.destroy();
        verify(mockPlugPlayView).destroy();
    }

    @Test
    public void getCallToActionOverlayViewTest() {

    }
}