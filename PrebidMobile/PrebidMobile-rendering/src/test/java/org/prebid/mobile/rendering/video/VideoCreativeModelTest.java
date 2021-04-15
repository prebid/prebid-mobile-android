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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
@Config(sdk = 19)
public class VideoCreativeModelTest {

    private VideoCreativeModel mVideoCreativeModel;

    private OmEventTracker mMockOmEventTracker;
    private TrackingManager mMockTrackingManager;

    @Before
    public void setup() {
        AdConfiguration adConfiguration = mock(AdConfiguration.class);
        mMockOmEventTracker = mock(OmEventTracker.class);

        mMockTrackingManager = mock(TrackingManager.class);

        mVideoCreativeModel = new VideoCreativeModel(mMockTrackingManager,
                                                     mMockOmEventTracker,
                                                     adConfiguration);
    }

    @Test
    public void trackVideoEventTest() {
        mVideoCreativeModel.getVideoEventUrls().put(VideoAdEvent.Event.AD_COLLAPSE, new ArrayList<String>());

        mVideoCreativeModel.trackVideoEvent(VideoAdEvent.Event.AD_COLLAPSE);
        verify(mMockTrackingManager).fireEventTrackingURLs(any(ArrayList.class));
        verify(mMockOmEventTracker).trackOmVideoAdEvent(VideoAdEvent.Event.AD_COLLAPSE);

        mVideoCreativeModel.trackNonSkippableStandaloneVideoLoaded(false);
        verify(mMockOmEventTracker).trackNonSkippableStandaloneVideoLoaded(false);

        mVideoCreativeModel.trackPlayerStateChange(InternalPlayerState.FULLSCREEN);
        verify(mMockOmEventTracker).trackOmPlayerStateChange(InternalPlayerState.FULLSCREEN);

        mVideoCreativeModel.trackVideoAdStarted(0, 0);
        verify(mMockOmEventTracker).trackVideoAdStarted(0, 0);
    }

    @Test
    public void registerVideoEventTest() {
        assertEquals(0, mVideoCreativeModel.getVideoEventUrls().size());
        mVideoCreativeModel.registerVideoEvent(VideoAdEvent.Event.AD_COLLAPSE, new ArrayList<String>());
        assertEquals(1, mVideoCreativeModel.getVideoEventUrls().size());
    }
}