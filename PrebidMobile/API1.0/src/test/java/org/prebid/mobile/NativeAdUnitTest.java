package org.prebid.mobile;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.prebid.mobile.NativeAdUnit.ASSETS;
import static org.prebid.mobile.NativeAdUnit.EVENT_TRACKERS;
import static org.prebid.mobile.NativeAdUnit.LENGTH;
import static org.prebid.mobile.NativeAdUnit.REQUIRED;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class NativeAdUnitTest {

    @Test
    public void testNativeAdUnitCreation() throws Exception {
        NativeAdUnit adUnit = new NativeAdUnit("12345");
        assertEquals("12345", FieldUtils.readField(adUnit, "configId", true));
        assertEquals(AdType.NATIVE, FieldUtils.readField(adUnit, "adType", true));
    }

    @Test
    public void testConstants() {
        assertEquals("ver", NativeAdUnit.VERSION);
        assertEquals("1.2", NativeAdUnit.SUPPORTED_VERSION);
        assertEquals("context", NativeAdUnit.CONTEXT);
        assertEquals("contextsubtype", NativeAdUnit.CONTEXT_SUB_TYPE);
        assertEquals("plcmttype", NativeAdUnit.PLACEMENT_TYPE);
        assertEquals("plcmtcnt", NativeAdUnit.PLACEMENT_COUNT);
        assertEquals("seq", NativeAdUnit.SEQ);
        assertEquals("assets", NativeAdUnit.ASSETS);
        assertEquals("aurlsupport", NativeAdUnit.A_URL_SUPPORT);
        assertEquals("durlsupport", NativeAdUnit.D_URL_SUPPORT);
        assertEquals("eventtrackers", EVENT_TRACKERS);
        assertEquals("privacy", NativeAdUnit.PRIVACY);
        assertEquals("event", NativeAdUnit.EVENT);
        assertEquals("methods", NativeAdUnit.METHODS);
        assertEquals("len", NativeAdUnit.LENGTH);
        assertEquals("required", NativeAdUnit.REQUIRED);
        assertEquals("assetExt", NativeAdUnit.ASSETS_EXT);
        assertEquals("wmin", NativeAdUnit.WIDTH_MIN);
        assertEquals("hmin", NativeAdUnit.HEIGHT_MIN);
        assertEquals("W", NativeAdUnit.WIDTH);
        assertEquals("h", NativeAdUnit.HEIGHT);
        assertEquals("type", NativeAdUnit.TYPE);
        assertEquals("mimes", NativeAdUnit.MIMES);
        assertEquals("title", NativeAdUnit.TITLE);
        assertEquals("img", NativeAdUnit.IMAGE);
        assertEquals("data", NativeAdUnit.DATA);
        assertEquals("native", NativeAdUnit.NATIVE);
        assertEquals("request", NativeAdUnit.REQUEST);
    }

    @Test
    public void testSetContextType() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        assertEquals(null, adUnit.requestConfig.get("context"));
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        assertEquals(3, adUnit.requestConfig.get("context"));
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        assertEquals(1, adUnit.requestConfig.get("context"));
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        assertEquals(2, adUnit.requestConfig.get("context"));
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(501);
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.CUSTOM);
        assertEquals(501, adUnit.requestConfig.get("context"));
    }

    @Test
    public void testSetContextSubType() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        assertEquals(null, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        assertEquals(10, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        assertEquals(11, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        assertEquals(12, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        assertEquals(13, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        assertEquals(14, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        assertEquals(15, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        assertEquals(20, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        assertEquals(21, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        assertEquals(22, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        assertEquals(30, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        assertEquals(31, adUnit.requestConfig.get("contextsubtype"));
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        assertEquals(32, adUnit.requestConfig.get("contextsubtype"));
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(501);
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
        assertEquals(501, adUnit.requestConfig.get("contextsubtype"));
    }

    @Test
    public void testSetPlacementType() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        assertEquals(null, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        assertEquals(1, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
        NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED.setID(501);
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        assertEquals(1, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        assertEquals(2, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        assertEquals(3, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        assertEquals(4, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CUSTOM);
        assertEquals(500, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(501);
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CUSTOM);
        assertEquals(501, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_TYPE));
    }

    @Test
    public void testSetPlacementCount() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        assertEquals(null, adUnit.requestConfig.get(NativeAdUnit.PLACEMENT_COUNT));
        adUnit.setPlacementCount(30);
    }

    @Test
    public void testSetSeq() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        adUnit.setSeq(1);
        assertEquals(1, adUnit.requestConfig.get(NativeAdUnit.SEQ));
    }

    @Test
    public void testSetAUrlSupport() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        adUnit.setAUrlSupport(true);
        assertEquals(1, adUnit.requestConfig.get(NativeAdUnit.A_URL_SUPPORT));
        adUnit.setAUrlSupport(false);
        assertEquals(0, adUnit.requestConfig.get(NativeAdUnit.A_URL_SUPPORT));
    }

    @Test
    public void testSetDUrlSupport() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        adUnit.setDUrlSupport(true);
        assertEquals(1, adUnit.requestConfig.get(NativeAdUnit.D_URL_SUPPORT));
        adUnit.setDUrlSupport(false);
        assertEquals(0, adUnit.requestConfig.get(NativeAdUnit.D_URL_SUPPORT));
    }

    @Test
    public void testSetPrivacy() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        adUnit.setPrivacy(true);
        assertEquals(1, adUnit.requestConfig.get(NativeAdUnit.PRIVACY));
        adUnit.setPrivacy(false);
        assertEquals(0, adUnit.requestConfig.get(NativeAdUnit.PRIVACY));
    }

    @Test
    public void testSetExt() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        adUnit.setExt(123);
        assertEquals(null, adUnit.requestConfig.get(NativeAdUnit.EXT));
        JSONObject ext = new JSONObject();
        try {
            ext.put("key", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adUnit.setExt(ext);
        String value = "";

        try {
            value = ((JSONObject) adUnit.requestConfig.get(NativeAdUnit.EXT)).getString("key");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals("value", value);
    }

    @Test
    public void testAddEventTrackerNullMethods() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        Exception expected = null;
        try {
            adUnit.addEventTracker(NativeAdUnit.EVENT_TYPE.IMPRESSION, null, null);
        } catch (Exception e) {
            expected = e;
        }
        assertNotNull(expected);
        assertEquals("Methods are required", expected.getMessage());
        Exception expected2 = null;
        try {
            adUnit.addEventTracker(NativeAdUnit.EVENT_TYPE.IMPRESSION, new ArrayList<NativeAdUnit.EVENT_TRACKING_METHOD>(), null);
        } catch (Exception e) {
            expected2 = e;
        }
        assertNotNull(expected2);
        assertEquals("Methods are required", expected2.getMessage());
    }

    @Test
    public void testAddEventTracker() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        try {
            ArrayList<NativeAdUnit.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
            methods.add(NativeAdUnit.EVENT_TRACKING_METHOD.IMAGE);
            adUnit.addEventTracker(NativeAdUnit.EVENT_TYPE.IMPRESSION, methods, null);
            ArrayList<NativeAdUnit.EVENT_TRACKING_METHOD> methods1 = new ArrayList<>();
            methods1.add(NativeAdUnit.EVENT_TRACKING_METHOD.JS);
            adUnit.addEventTracker(NativeAdUnit.EVENT_TYPE.VIEWABLE_MRC50, methods1, null);
        } catch (Exception e) {

        }
        assertNotNull(adUnit.requestConfig.get(EVENT_TRACKERS));
        JSONArray trackers = (JSONArray) adUnit.requestConfig.get(EVENT_TRACKERS);
        assertEquals(2, trackers.length());
        JSONObject tracker = null;
        int event = 0;
        int medthod = 0;
        try {
            tracker = (JSONObject) trackers.get(0);
            event = (int) tracker.get(NativeAdUnit.EVENT);
            medthod = (int) tracker.getJSONArray(NativeAdUnit.METHODS).get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(1, event);
        assertEquals(1, medthod);
        try {
            tracker = (JSONObject) trackers.get(1);
            event = (int) tracker.get(NativeAdUnit.EVENT);
            medthod = (int) tracker.getJSONArray(NativeAdUnit.METHODS).get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(2, event);
        assertEquals(2, medthod);
    }

    @Test
    public void testAddTitle() {
        NativeAdUnit adUnit = new NativeAdUnit("1234");
        adUnit.addTitle(90, true, null, null);
        HashMap<String, Object> params = ((HashMap<NativeAdUnit.NATIVE_REQUEST_ASSET, HashMap<String, Object>>) adUnit.requestConfig.get(ASSETS)).get(NativeAdUnit.NATIVE_REQUEST_ASSET.TITLE);
        assertEquals(2, params.size());
        assertEquals(90, params.get(LENGTH));
        assertEquals(true, params.get(REQUIRED));
    }


}