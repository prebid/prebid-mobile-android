package com.openx.apollo.models.ntv;

import com.openx.apollo.models.openrtb.bidRequests.Ext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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