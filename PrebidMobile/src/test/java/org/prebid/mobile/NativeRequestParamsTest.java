package org.prebid.mobile;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest=Config.NONE)
public class NativeRequestParamsTest {

    @Test
    public void testNativeRequestParamsContextType() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertNull(requestParams.getContextType());
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        assertEquals(requestParams.getContextType(), NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        assertNotNull(requestParams.getContextType());
        assertEquals(requestParams.getContextType(), NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        assertEquals(requestParams.getContextType(), NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        assertEquals(requestParams.getContextType(), NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        requestParams.setContextType(NativeAdUnit.CONTEXT_TYPE.CUSTOM);
        assertEquals(requestParams.getContextType(), NativeAdUnit.CONTEXT_TYPE.CUSTOM);
    }

    @Test
    public void testNativeRequestContextSubType() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertNull(requestParams.getContextsubtype());
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        assertNotNull(requestParams.getContextsubtype());
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        requestParams.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
        assertEquals(requestParams.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
    }

    @Test
    public void testNativeRequestPlacementType() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertNull(requestParams.getPlacementType());
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        assertNotNull(requestParams.getPlacementType());
        assertEquals(requestParams.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        assertEquals(requestParams.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        assertEquals(requestParams.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        assertEquals(requestParams.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        requestParams.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CUSTOM);
        assertEquals(requestParams.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.CUSTOM);
    }

    @Test
    public void testNativeRequestPlacementCount() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(requestParams.getPlacementCount(), 1);
        requestParams.setPlacementCount(123);
        assertEquals(requestParams.getPlacementCount(), 123);
    }

    @Test
    public void testNativeRequestSequence() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(requestParams.getSeq(), 0);
        requestParams.setSeq(1);
        assertEquals(requestParams.getSeq(), 1);
    }

    @Test
    public void testNativeRequestAsseturlSupport() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(requestParams.isAUrlSupport(), false);
        requestParams.setAUrlSupport(true);
        assertEquals(requestParams.isAUrlSupport(), true);
    }

    @Test
    public void testNativeRequestDUrlSupport() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(requestParams.isDUrlSupport(), false);
        requestParams.setDUrlSupport(true);
        assertEquals(requestParams.isDUrlSupport(), true);
    }

    @Test
    public void testNativeRequestPrivacy() {
        NativeRequestParams requestParams = new NativeRequestParams();
        assertEquals(requestParams.isPrivacy(), false);
        requestParams.setPrivacy(true);
        assertEquals(requestParams.isPrivacy(), true);
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
        assertEquals(eventTracker1.event, NativeEventTracker.EVENT_TYPE.IMPRESSION);
        assertEquals(eventTracker1.getMethods().size(), 2);
        assertEquals(eventTracker1.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        assertEquals(eventTracker1.getMethods().get(1), NativeEventTracker.EVENT_TRACKING_METHOD.JS);

        NativeEventTracker eventTracker2 = requestParams.getEventTrackers().get(1);
        assertEquals(eventTracker2.event, NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50);
        assertEquals(eventTracker2.getMethods().size(), 2);
        assertEquals(eventTracker2.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM);
        assertEquals(eventTracker2.getMethods().get(1), NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);

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
        assertEquals(requestParams.getAssets().size(), 2);
        assertEquals(((NativeTitleAsset) requestParams.getAssets().get(0)).getLen(), 25);
        assertEquals(((NativeImageAsset) requestParams.getAssets().get(1)).getHMin(), 30);
        assertEquals(((NativeImageAsset) requestParams.getAssets().get(1)).getWMin(), 20);
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