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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
@Config(sdk = 19)
public class OmEventTrackerTest {
    private OmEventTracker mOmEventTracker;

    private OmAdSessionManager mMockOmAdSessionManager;

    @Before
    public void setup() {
        mMockOmAdSessionManager = mock(OmAdSessionManager.class);
        mOmEventTracker = new OmEventTracker();
        mOmEventTracker.registerActiveAdSession(mMockOmAdSessionManager);
    }

    @Test
    public void trackOmVideoAdEventTest() {
        VideoAdEvent.Event anyVideoEvent = any(VideoAdEvent.Event.class);
        mOmEventTracker.trackOmVideoAdEvent(anyVideoEvent);
        verify(mMockOmAdSessionManager, times(1)).trackAdVideoEvent(anyVideoEvent);
    }

    @Test
    public void trackOmHtmlAdEventTest() {
        mOmEventTracker.trackOmHtmlAdEvent(TrackingEvent.Events.IMPRESSION);
        mOmEventTracker.trackOmHtmlAdEvent(TrackingEvent.Events.CLICK);

        verify(mMockOmAdSessionManager, times(1)).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mMockOmAdSessionManager, times(1)).trackDisplayAdEvent(TrackingEvent.Events.CLICK);
    }

    @Test
    public void trackOmPlayerStateChangeTest() {
        mOmEventTracker.trackOmPlayerStateChange(InternalPlayerState.NORMAL);
        verify(mMockOmAdSessionManager, times(1)).trackPlayerStateChangeEvent(InternalPlayerState.NORMAL);
    }

    @Test
    public void trackVideoAdStartedTest() {
        mOmEventTracker.trackVideoAdStarted(0, 0);
        verify(mMockOmAdSessionManager).videoAdStarted(0, 0);
    }

    @Test
    public void trackNonSkippableVideoLoadedTest() {
        mOmEventTracker.trackNonSkippableStandaloneVideoLoaded(false);
        verify(mMockOmAdSessionManager).nonSkippableStandaloneVideoAdLoaded(false);
    }
}
