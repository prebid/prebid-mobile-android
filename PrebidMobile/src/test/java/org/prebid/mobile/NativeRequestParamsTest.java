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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class NativeRequestParamsTest {
    @After
    public void tearDown() throws Exception {
        NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(500);
    }

    @Test
    public void testNativeRequestParamsContextType() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertNotNull(requestParams);
        assertNull(requestParams.getContextType());
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        assertNotNull(requestParams.getContextType());
        assertEquals(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC, requestParams.getContextType());
        assertEquals(1, requestParams.getContextType().getID());
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC, requestParams.getContextType());
        assertEquals(2, requestParams.getContextType().getID());
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.PRODUCT, requestParams.getContextType());
        assertEquals(3, requestParams.getContextType().getID());
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.CUSTOM);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.CUSTOM, requestParams.getContextType());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(500);
        assertEquals(500, requestParams.getContextType().getID());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(600);
        assertEquals(600, requestParams.getContextType().getID());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(1);
        assertEquals(600, requestParams.getContextType().getID());
        assertFalse("Invalid CustomId", 1 == requestParams.getContextType().getID());
    }

    @Test
    public void testNativeRequestContextSubType() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertNull(requestParams.getContextsubtype());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        assertNotNull(requestParams.getContextsubtype());
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.GENERAL, requestParams.getContextsubtype());
        assertEquals(10, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL, requestParams.getContextsubtype());
        assertEquals(11, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.VIDEO, requestParams.getContextsubtype());
        assertEquals(12, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.AUDIO, requestParams.getContextsubtype());
        assertEquals(13, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.IMAGE, requestParams.getContextsubtype());
        assertEquals(14, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED, requestParams.getContextsubtype());
        assertEquals(15, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL, requestParams.getContextsubtype());
        assertEquals(20, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.EMAIL, requestParams.getContextsubtype());
        assertEquals(21, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM, requestParams.getContextsubtype());
        assertEquals(22, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.SELLING, requestParams.getContextsubtype());
        assertEquals(30, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE, requestParams.getContextsubtype());
        assertEquals(31, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES, requestParams.getContextsubtype());
        assertEquals(32, requestParams.getContextsubtype().getID());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM, requestParams.getContextsubtype());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(500);
        assertEquals(500, requestParams.getContextsubtype().getID());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(600);
        assertEquals(600, requestParams.getContextsubtype().getID());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(10);
        assertEquals(600, requestParams.getContextsubtype().getID());
        assertFalse("Invalid CustomId", 1 == requestParams.getContextsubtype().getID());
    }

    @Test
    public void testNativeRequestPlacementType() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertNull(requestParams.getPlacementType());
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        assertNotNull(requestParams.getPlacementType());
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED, requestParams.getPlacementType());
        assertEquals(1, requestParams.getPlacementType().getID());
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT, requestParams.getPlacementType());
        assertEquals(2, requestParams.getPlacementType().getID());
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT, requestParams.getPlacementType());
        assertEquals(3, requestParams.getPlacementType().getID());
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET, requestParams.getPlacementType());
        assertEquals(4, requestParams.getPlacementType().getID());
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CUSTOM);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CUSTOM, requestParams.getPlacementType());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(500);
        assertEquals(500, requestParams.getPlacementType().getID());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(600);
        assertEquals(600, requestParams.getPlacementType().getID());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(1);
        assertEquals(600, requestParams.getPlacementType().getID());
        assertFalse("Invalid CustomId", 1 == requestParams.getPlacementType().getID());
    }

    @Test
    public void testNativeRequestPlacementCount() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(1, requestParams.getPlacementCount());
        requestParams.setPlacementCount(123);
        assertEquals(123, requestParams.getPlacementCount());
    }

    @Test
    public void testNativeRequestSequence() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(0, requestParams.getSeq());
        requestParams.setSeq(1);
        assertEquals(1, requestParams.getSeq());
    }

    @Test
    public void testNativeRequestAsseturlSupport() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(false, requestParams.isAUrlSupport());
        requestParams.setAUrlSupport(true);
        assertEquals(true, requestParams.isAUrlSupport());
    }

    @Test
    public void testNativeRequestDUrlSupport() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(false, requestParams.isDUrlSupport());
        requestParams.setDUrlSupport(true);
        assertEquals(true, requestParams.isDUrlSupport());
    }

    @Test
    public void testNativeRequestPrivacy() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(false, requestParams.isPrivacy());
        requestParams.setPrivacy(true);
        assertEquals(true, requestParams.isPrivacy());
    }

    @Test
    public void testNativeRequestExt() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertNull(requestParams.getExt());
        JSONObject ext = new JSONObject();
        try {
            ext.put("key", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestParams.setExt(ext);
        String value = "";
        try {
            JSONObject data = (JSONObject) requestParams.getExt();
            value = data.getString("key");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value", value);
    }

    @Test
    public void testNativeRequestEventTrackers() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(requestParams.getEventTrackers().size(), 0);
        try {
            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods1 = new ArrayList<>();
            methods1.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            methods1.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
            NativeEventTracker eventTracker1 = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods1);

            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods2 = new ArrayList<>();
            methods2.add(NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM);
            methods2.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            NativeEventTracker eventTracker2 = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50, methods2);

            requestParams.addEventTracker(eventTracker1);
            requestParams.addEventTracker(eventTracker2);
        } catch (Exception e) {

        }
        assertEquals(requestParams.getEventTrackers().size(), 2);

        NativeEventTracker eventTracker1 = requestParams.getEventTrackers().get(0);
        assertEquals(NativeEventTracker.EVENT_TYPE.IMPRESSION, eventTracker1.event);
        assertEquals(1, eventTracker1.event.getID());
        assertEquals(2, eventTracker1.getMethods().size());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE, eventTracker1.getMethods().get(0));
        assertEquals(1, eventTracker1.getMethods().get(0).getID());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.JS, eventTracker1.getMethods().get(1));
        assertEquals(2, eventTracker1.getMethods().get(1).getID());
        NativeEventTracker eventTracker2 = requestParams.getEventTrackers().get(1);
        assertEquals(NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50, eventTracker2.event);
        assertEquals(2, eventTracker2.event.getID());
        assertEquals(2, eventTracker2.getMethods().size());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM, eventTracker2.getMethods().get(0));
        NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(500);
        assertEquals(500, eventTracker2.getMethods().get(0).getID());
        NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(600);
        assertEquals(600, eventTracker2.getMethods().get(0).getID());
        NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(1);
        assertEquals(600, eventTracker2.getMethods().get(0).getID());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE, eventTracker2.getMethods().get(1));
        assertEquals(1, eventTracker2.getMethods().get(1).getID());

    }

    @Test
    public void testNativeRequestAssets() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertTrue(requestParams.getAssets().isEmpty());

        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(25);
        NativeImageAsset image = new NativeImageAsset();
        image.setWMin(20);
        image.setHMin(30);
        requestParams.addAsset(title);
        requestParams.addAsset(image);

        assertNotNull(requestParams.getAssets());
        assertEquals(2, requestParams.getAssets().size());
        assertEquals(25, ((NativeTitleAsset) requestParams.getAssets().get(0)).getLen());
        assertEquals(30, ((NativeImageAsset) requestParams.getAssets().get(1)).getHMin());
        assertEquals(20, ((NativeImageAsset) requestParams.getAssets().get(1)).getWMin());
    }

    @Test
    public void testConstants() {
        assertEquals("ver", NativeRequestParams.VERSION);
        assertEquals("1.2", NativeRequestParams.SUPPORTED_VERSION);
        assertEquals("context", NativeRequestParams.CONTEXT);
        assertEquals("contextsubtype", NativeRequestParams.CONTEXT_SUB_TYPE);
        assertEquals("plcmttype", NativeRequestParams.PLACEMENT_TYPE);
        assertEquals("plcmtcnt", NativeRequestParams.PLACEMENT_COUNT);
        assertEquals("seq", NativeRequestParams.SEQ);
        assertEquals("assets", NativeRequestParams.ASSETS);
        assertEquals("aurlsupport", NativeRequestParams.A_URL_SUPPORT);
        assertEquals("durlsupport", NativeRequestParams.D_URL_SUPPORT);
        assertEquals("eventtrackers", NativeRequestParams.EVENT_TRACKERS);
        assertEquals("privacy", NativeRequestParams.PRIVACY);
        assertEquals("event", NativeRequestParams.EVENT);
        assertEquals("methods", NativeRequestParams.METHODS);
        assertEquals("len", NativeRequestParams.LENGTH);
        assertEquals("required", NativeRequestParams.REQUIRED);
        assertEquals("assetExt", NativeRequestParams.ASSETS_EXT);
        assertEquals("wmin", NativeRequestParams.WIDTH_MIN);
        assertEquals("hmin", NativeRequestParams.HEIGHT_MIN);
        assertEquals("W", NativeRequestParams.WIDTH);
        assertEquals("h", NativeRequestParams.HEIGHT);
        assertEquals("type", NativeRequestParams.TYPE);
        assertEquals("mimes", NativeRequestParams.MIMES);
        assertEquals("title", NativeRequestParams.TITLE);
        assertEquals("img", NativeRequestParams.IMAGE);
        assertEquals("data", NativeRequestParams.DATA);
        assertEquals("native", NativeRequestParams.NATIVE);
        assertEquals("request", NativeRequestParams.REQUEST);
    }

}