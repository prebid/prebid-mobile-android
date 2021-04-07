package com.openx.apollo.models;

import com.openx.apollo.networking.tracking.TrackingManager;
import com.openx.apollo.video.OmEventTracker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeModelTest {

    @Test
    public void testRegisterTrackingEvent() throws Exception {
        TrackingManager trackingManager = TrackingManager.getInstance();
        OmEventTracker mockOmEventTracker = mock(OmEventTracker.class);
        AdConfiguration mockConfig = mock(AdConfiguration.class);

        CreativeModel creativeModel = new CreativeModel(trackingManager, mockOmEventTracker, mockConfig);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("www.impression.url");
        creativeModel.registerTrackingEvent(TrackingEvent.Events.IMPRESSION, urls);
        assertTrue(creativeModel.mTrackingURLs.size() == 1);
    }

    @Test
    public void testRegisterTrackingEventNamed() throws Exception {

    }

    @Test
    public void testTrackImpressionEvent() throws Exception {
        TrackingManager trackingManager = TrackingManager.getInstance();
        TrackingManager mockTrackingManager = spy(trackingManager);
        OmEventTracker mockOmEventTracker = mock(OmEventTracker.class);
        AdConfiguration mockConfig = mock(AdConfiguration.class);

        CreativeModel creativeModel = new CreativeModel(mockTrackingManager, mockOmEventTracker, mockConfig);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("www.impression.url");
        creativeModel.registerTrackingEvent(TrackingEvent.Events.IMPRESSION, urls);
        creativeModel.trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockTrackingManager, times(1)).fireEventTrackingImpressionURLs((ArrayList<String>) anyObject());
        verify(mockOmEventTracker, times(1)).trackOmHtmlAdEvent(TrackingEvent.Events.IMPRESSION);
    }

    @Test
    public void testTrackGenericEvent() throws Exception {
        TrackingManager trackingManager = TrackingManager.getInstance();
        TrackingManager mockTrackingManager = spy(trackingManager);
        OmEventTracker mockOmEventTracker = mock(OmEventTracker.class);
        AdConfiguration mockConfig = mock(AdConfiguration.class);

        CreativeModel creativeModel = new CreativeModel(mockTrackingManager, mockOmEventTracker, mockConfig);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("www.default.url");
        creativeModel.registerTrackingEvent(TrackingEvent.Events.DEFAULT, urls);
        creativeModel.trackDisplayAdEvent(TrackingEvent.Events.DEFAULT);
        verify(mockTrackingManager, times(1)).fireEventTrackingURLs((ArrayList<String>) anyObject());
    }
}