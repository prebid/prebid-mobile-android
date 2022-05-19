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
import org.prebid.mobile.rendering.models.TrackingEvent;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
@Config(sdk = 19)
public class OmEventTrackerTest {
    private OmEventTracker omEventTracker;

    private OmAdSessionManager mockOmAdSessionManager;

    @Before
    public void setup() {
        mockOmAdSessionManager = mock(OmAdSessionManager.class);
        omEventTracker = new OmEventTracker();
        omEventTracker.registerActiveAdSession(mockOmAdSessionManager);
    }

    @Test
    public void trackOmVideoAdEventTest() {
        VideoAdEvent.Event anyVideoEvent = any(VideoAdEvent.Event.class);
        omEventTracker.trackOmVideoAdEvent(anyVideoEvent);
        verify(mockOmAdSessionManager, times(1)).trackAdVideoEvent(anyVideoEvent);
    }

    @Test
    public void trackOmHtmlAdEventTest() {
        omEventTracker.trackOmHtmlAdEvent(TrackingEvent.Events.IMPRESSION);
        omEventTracker.trackOmHtmlAdEvent(TrackingEvent.Events.CLICK);

        verify(mockOmAdSessionManager, times(1)).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockOmAdSessionManager, times(1)).trackDisplayAdEvent(TrackingEvent.Events.CLICK);
    }

    @Test
    public void trackOmPlayerStateChangeTest() {
        omEventTracker.trackOmPlayerStateChange(InternalPlayerState.NORMAL);
        verify(mockOmAdSessionManager, times(1)).trackPlayerStateChangeEvent(InternalPlayerState.NORMAL);
    }

    @Test
    public void trackVideoAdStartedTest() {
        omEventTracker.trackVideoAdStarted(0, 0);
        verify(mockOmAdSessionManager).videoAdStarted(0, 0);
    }

    @Test
    public void trackNonSkippableVideoLoadedTest() {
        omEventTracker.trackNonSkippableStandaloneVideoLoaded(false);
        verify(mockOmAdSessionManager).nonSkippableStandaloneVideoAdLoaded(false);
    }
}
