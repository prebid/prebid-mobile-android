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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.video.OmEventTracker;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeModelTest {

    @Test
    public void testRegisterTrackingEvent() throws Exception {
        TrackingManager trackingManager = TrackingManager.getInstance();
        OmEventTracker mockOmEventTracker = mock(OmEventTracker.class);
        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);

        CreativeModel creativeModel = new CreativeModel(trackingManager, mockOmEventTracker, mockConfig);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("www.impression.url");
        creativeModel.registerTrackingEvent(TrackingEvent.Events.IMPRESSION, urls);
        assertTrue(creativeModel.trackingURLs.size() == 1);
    }

    @Test
    public void testRegisterTrackingEventNamed() throws Exception {

    }

    @Test
    public void testTrackImpressionEvent() throws Exception {
        TrackingManager trackingManager = TrackingManager.getInstance();
        TrackingManager mockTrackingManager = spy(trackingManager);
        OmEventTracker mockOmEventTracker = mock(OmEventTracker.class);
        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);

        CreativeModel creativeModel = new CreativeModel(mockTrackingManager, mockOmEventTracker, mockConfig);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("www.impression.url");
        creativeModel.registerTrackingEvent(TrackingEvent.Events.IMPRESSION, urls);
        creativeModel.trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockTrackingManager, times(1)).fireEventTrackingImpressionURLs(any());
        verify(mockOmEventTracker, times(1)).trackOmHtmlAdEvent(TrackingEvent.Events.IMPRESSION);
    }

    @Test
    public void testTrackGenericEvent() throws Exception {
        TrackingManager trackingManager = TrackingManager.getInstance();
        TrackingManager mockTrackingManager = spy(trackingManager);
        OmEventTracker mockOmEventTracker = mock(OmEventTracker.class);
        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);

        CreativeModel creativeModel = new CreativeModel(mockTrackingManager, mockOmEventTracker, mockConfig);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("www.default.url");
        creativeModel.registerTrackingEvent(TrackingEvent.Events.DEFAULT, urls);
        creativeModel.trackDisplayAdEvent(TrackingEvent.Events.DEFAULT);
        verify(mockTrackingManager, times(1)).fireEventTrackingURLs(any());
    }
}