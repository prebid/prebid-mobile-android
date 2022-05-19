package org.prebid.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
            assertEquals(NativeEventTracker.EVENT_TYPE.IMPRESSION,eventTracker.getEvent());
            assertEquals(1,eventTracker.getEvent().getID());
            eventTracker.event = NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50;
            assertEquals(NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50,eventTracker.getEvent());
            assertEquals(2,eventTracker.getEvent().getID());
            eventTracker.event = NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC100;
            assertEquals(NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC100,eventTracker.getEvent());
            assertEquals(3,eventTracker.getEvent().getID());
            eventTracker.event = NativeEventTracker.EVENT_TYPE.VIEWABLE_VIDEO50;
            assertEquals(NativeEventTracker.EVENT_TYPE.VIEWABLE_VIDEO50,eventTracker.getEvent());
            assertEquals(4,eventTracker.getEvent().getID());
            eventTracker.event = NativeEventTracker.EVENT_TYPE.CUSTOM;
            assertEquals(NativeEventTracker.EVENT_TYPE.CUSTOM,eventTracker.getEvent());
            NativeEventTracker.EVENT_TYPE.CUSTOM.setID(500);
            assertEquals(500,eventTracker.getEvent().getID());
            NativeEventTracker.EVENT_TYPE.CUSTOM.setID(600);
            assertEquals(600, eventTracker.getEvent().getID());
            NativeEventTracker.EVENT_TYPE.CUSTOM.setID(1);
            assertEquals(600, eventTracker.getEvent().getID());
            assertFalse("Invalid CustomId", 1 == eventTracker.getEvent().getID());

        } catch (Exception e) {

        }

    }

    @Test
    public void testNativeEventTrackingMethods() {
        try {
            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            NativeEventTracker eventTracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            assertEquals(1,eventTracker.getMethods().size());
            assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE,eventTracker.getMethods().get(0));
            assertEquals(1,eventTracker.getMethods().get(0).getID());
            methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
            eventTracker.methods = methods;
            assertEquals(1,eventTracker.getMethods().size());
            assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.JS,eventTracker.getMethods().get(0));
            assertEquals(2,eventTracker.getMethods().get(0).getID());
            methods = new ArrayList<>();
            methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM);
            eventTracker.methods = methods;
            assertEquals(1,eventTracker.getMethods().size());
            assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM,eventTracker.getMethods().get(0));
            NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(500);
            assertEquals(500,eventTracker.getMethods().get(0).getID());
            NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(600);
            assertEquals(600, eventTracker.getMethods().get(0).getID());
            NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(1);
            assertEquals(600, eventTracker.getMethods().get(0).getID());
            assertFalse("Invalid CustomId", 1 == eventTracker.getMethods().get(0).getID());

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

    @After
    public void tearDown() throws Exception {
        NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(500);
    }
}