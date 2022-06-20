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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoCreativeViewTest {

    private VideoCreativeView videoCreativeView;
    private VideoCreative mockCreative;

    @Before
    public void setUp() throws AdException {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mockCreative = Mockito.mock(VideoCreative.class);

        videoCreativeView = new VideoCreativeView(context, mockCreative);
    }

    @Test
    public void startTest() throws IllegalAccessException {
        VideoPlayerView mockPlugPlayView = mock(ExoPlayerView.class);
        WhiteBox.field(VideoCreativeView.class, "exoPlayerView").set(videoCreativeView, mockPlugPlayView);

        videoCreativeView.start(anyInt());
        verify(mockPlugPlayView).start(anyFloat());
    }

    @Test
    public void destroyTest() throws IllegalAccessException {
        VideoPlayerView mockPlugPlayView = mock(ExoPlayerView.class);
        View mockLytCallToActionOverlay = mock(View.class);
        WhiteBox.field(VideoCreativeView.class, "exoPlayerView").set(videoCreativeView, mockPlugPlayView);
        videoCreativeView.addView(mockLytCallToActionOverlay);

        videoCreativeView.destroy();
        verify(mockPlugPlayView).destroy();
    }

    @Test
    public void getCallToActionOverlayViewTest() {

    }
}