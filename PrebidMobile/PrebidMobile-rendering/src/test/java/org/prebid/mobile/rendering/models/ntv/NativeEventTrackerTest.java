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

package org.prebid.mobile.rendering.models.ntv;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class NativeEventTrackerTest {

    @Before
    public void setUp() {
        NativeEventTracker.EventType.CUSTOM.setId(500);
        NativeEventTracker.EventTrackingMethod.CUSTOM.setId(500);
    }

    @Test
    public void whenInit_EventTypeAndMethodsAreSet() {
        ArrayList<NativeEventTracker.EventTrackingMethod> eventTrackingMethods = new ArrayList<>();
        eventTrackingMethods.add(NativeEventTracker.EventTrackingMethod.IMAGE);

        NativeEventTracker nativeEventTracker = new NativeEventTracker(NativeEventTracker.EventType.IMPRESSION, eventTrackingMethods);
        assertEquals(NativeEventTracker.EventType.IMPRESSION, nativeEventTracker.getEventType());
        assertEquals(eventTrackingMethods, nativeEventTracker.getEventTrackingMethods());
        assertEquals(NativeEventTracker.EventTrackingMethod.IMAGE.getId(), nativeEventTracker.getEventTrackingMethods().get(0).getId());
    }

    @Test
    public void whenSetExt_ExtWasSet() {
        Ext ext = new Ext();
        ext.put("test", "test");

        ArrayList<NativeEventTracker.EventTrackingMethod> eventTrackingMethods = new ArrayList<>();
        eventTrackingMethods.add(NativeEventTracker.EventTrackingMethod.IMAGE);

        NativeEventTracker nativeEventTracker = new NativeEventTracker(NativeEventTracker.EventType.IMPRESSION, eventTrackingMethods);
        nativeEventTracker.setExt(ext);
        assertEquals(ext.getJsonObject().toString(), nativeEventTracker.getExt().getJsonObject().toString());
    }

    @Test
    public void whenEventTypeSetIdAndTypeNotCustom_IdWasNotChanged() {
        NativeEventTracker.EventType eventType = NativeEventTracker.EventType.IMPRESSION;
        assertEquals(1, eventType.getId());
        eventType.setId(501);
        assertEquals(1, eventType.getId());
    }

    @Test
    public void whenEventTypeSetIdAndTypeCustomAndInExistingValue_IdWasNotChanged() {
        NativeEventTracker.EventType eventType = NativeEventTracker.EventType.CUSTOM;
        assertEquals(500, eventType.getId());
        eventType.setId(1);
        assertEquals(500, eventType.getId());
    }

    @Test
    public void whenEventTypeSetIdAndTypeCustomAndNotInExistingValue_IdWasChanged() {
        NativeEventTracker.EventType eventType = NativeEventTracker.EventType.CUSTOM;
        assertEquals(500, eventType.getId());
        eventType.setId(501);
        assertEquals(501, eventType.getId());
    }

    @Test
    public void whenEventTrackingMethodSetIdAndTypeNotCustom_IdWasNotChanged() {
        NativeEventTracker.EventTrackingMethod eventTrackingMethod = NativeEventTracker.EventTrackingMethod.IMAGE;
        assertEquals(1, eventTrackingMethod.getId());
        eventTrackingMethod.setId(501);
        assertEquals(1, eventTrackingMethod.getId());
    }

    @Test
    public void whenEventTrackingMethodSetIdAndTypeCustomAndInExistingValue_IdWasNotChanged() {
        NativeEventTracker.EventTrackingMethod eventTrackingMethod = NativeEventTracker.EventTrackingMethod.CUSTOM;
        assertEquals(500, eventTrackingMethod.getId());
        eventTrackingMethod.setId(1);
        assertEquals(500, eventTrackingMethod.getId());
    }

    @Test
    public void whenEventTrackingMethodSetIdAndTypeCustomAndNotInExistingValue_IdWasChanged() {
        NativeEventTracker.EventTrackingMethod eventTrackingMethod = NativeEventTracker.EventTrackingMethod.CUSTOM;
        assertEquals(500, eventTrackingMethod.getId());
        eventTrackingMethod.setId(501);
        assertEquals(501, eventTrackingMethod.getId());
    }
}