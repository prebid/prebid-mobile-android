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