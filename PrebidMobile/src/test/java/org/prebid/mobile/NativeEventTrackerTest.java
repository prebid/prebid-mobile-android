package org.prebid.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest=Config.NONE)
public class NativeEventTrackerTest {

    @Test
    public void testNativeEventType() {
        try {
            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
            NativeEventTracker eventTracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            assertEquals(eventTracker.getEvent(), NativeEventTracker.EVENT_TYPE.IMPRESSION);
            eventTracker.event = NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50;
            assertEquals(eventTracker.getEvent(), NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50);
            eventTracker.event = NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC100;
            assertEquals(eventTracker.getEvent(), NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC100);
            eventTracker.event = NativeEventTracker.EVENT_TYPE.VIEWABLE_VIDEO50;
            assertEquals(eventTracker.getEvent(), NativeEventTracker.EVENT_TYPE.VIEWABLE_VIDEO50);
            eventTracker.event = NativeEventTracker.EVENT_TYPE.CUSTOM;
            assertEquals(eventTracker.getEvent(), NativeEventTracker.EVENT_TYPE.CUSTOM);

        } catch (Exception e) {

        }

    }

    @Test
    public void testNativeEventTrackingMethods() {
        try {
            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            NativeEventTracker eventTracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            assertEquals(eventTracker.getMethods().size(), 1);
            assertEquals(eventTracker.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
            eventTracker.methods = methods;
            assertEquals(eventTracker.getMethods().size(), 1);
            assertEquals(eventTracker.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.JS);
            methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM);
            eventTracker.methods = methods;
            assertEquals(eventTracker.getMethods().size(), 1);
            assertEquals(eventTracker.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM);

        } catch (Exception e) {

        }
    }

    @Test
    public void testNativeEventExtObject() {

        try {
            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            NativeEventTracker eventTracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            JSONObject ext = new JSONObject();
            try {
                ext.put("key", "value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            eventTracker.setExt(ext);
            String value = "";
            try {
                JSONObject data = (JSONObject) eventTracker.getExtObject();
                value = data.getString("key");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            assertEquals("value", value);

        } catch (Exception e) {

        }
    }

}