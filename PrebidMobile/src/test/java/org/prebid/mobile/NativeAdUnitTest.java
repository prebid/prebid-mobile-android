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
public class NativeAdUnitTest {
    static final String PBS_CONFIG_ID_NATIVE_APPNEXUS = "1f85e687-b45f-4649-a4d5-65f74f2ede8e";

    @Test
    public void testNativeAdUnitCreation() throws Exception {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertEquals(PBS_CONFIG_ID_NATIVE_APPNEXUS, FieldUtils.readField(nativeUnit, "configId", true));
        assertEquals(AdType.NATIVE, FieldUtils.readField(nativeUnit, "adType", true));
    }

    @Test
    public void testNativeAdContextType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.params.getContextType());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        assertNotNull(nativeUnit.params.getContextType());
        assertEquals(nativeUnit.params.getContextType(), NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        assertEquals(nativeUnit.params.getContextType(), NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        assertEquals(nativeUnit.params.getContextType(), NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.CUSTOM);
        assertEquals(nativeUnit.params.getContextType(), NativeAdUnit.CONTEXT_TYPE.CUSTOM);
    }

    @Test
    public void testNativeAdContextSubType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.params.getContextsubtype());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        assertNotNull(nativeUnit.params.getContextsubtype());
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
        assertEquals(nativeUnit.params.getContextsubtype(), NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
    }

    @Test
    public void testNativeAdPlacementType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.params.getPlacementType());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        assertNotNull(nativeUnit.params.getPlacementType());
        assertEquals(nativeUnit.params.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        assertEquals(nativeUnit.params.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        assertEquals(nativeUnit.params.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        assertEquals(nativeUnit.params.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CUSTOM);
        assertEquals(nativeUnit.params.getPlacementType(), NativeAdUnit.PLACEMENTTYPE.CUSTOM);
    }

    @Test
    public void testNativeAdPlacementCount() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(nativeUnit.params.getPlacementCount(), 1);
        nativeUnit.setPlacementCount(123);
        assertEquals(nativeUnit.params.getPlacementCount(), 123);
    }

    @Test
    public void testNativeAdSequence() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(nativeUnit.params.getSeq(), 0);
        nativeUnit.setSeq(1);
        assertEquals(nativeUnit.params.getSeq(), 1);
    }

    @Test
    public void testNativeAdAsseturlSupport() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(nativeUnit.params.isAUrlSupport(), false);
        nativeUnit.setAUrlSupport(true);
        assertEquals(nativeUnit.params.isAUrlSupport(), true);
    }

    @Test
    public void testNativeAdDUrlSupport() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(nativeUnit.params.isDUrlSupport(), false);
        nativeUnit.setDUrlSupport(true);
        assertEquals(nativeUnit.params.isDUrlSupport(), true);
    }

    @Test
    public void testNativeAdPrivacy() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(nativeUnit.params.isPrivacy(), false);
        nativeUnit.setPrivacy(true);
        assertEquals(nativeUnit.params.isPrivacy(), true);
    }

    @Test
    public void testNativeAdExt() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNull(nativeUnit.params.getExt());
        JSONObject ext = new JSONObject();
        try {
            ext.put("key", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        nativeUnit.setExt(ext);
        String value = "";
        try {
            JSONObject data = (JSONObject) nativeUnit.params.getExt();
            value = data.getString("key");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value", value);
    }

    @Test
    public void testNativeAdEventTrackers() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(nativeUnit.params.getEventTrackers().size(), 0);
        try {
            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods1 = new ArrayList<>();
            methods1.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            methods1.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
            NativeEventTracker eventTracker1 = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods1);

            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods2 = new ArrayList<>();
            methods2.add(NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM);
            methods2.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
            NativeEventTracker eventTracker2 = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50, methods2);

            nativeUnit.addEventTracker(eventTracker1);
            nativeUnit.addEventTracker(eventTracker2);
        } catch (Exception e) {

        }
        assertEquals(nativeUnit.params.getEventTrackers().size(), 2);

        NativeEventTracker eventTracker1 = nativeUnit.params.getEventTrackers().get(0);
        assertEquals(eventTracker1.event, NativeEventTracker.EVENT_TYPE.IMPRESSION);
        assertEquals(eventTracker1.getMethods().size(), 2);
        assertEquals(eventTracker1.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        assertEquals(eventTracker1.getMethods().get(1), NativeEventTracker.EVENT_TRACKING_METHOD.JS);

        NativeEventTracker eventTracker2 = nativeUnit.params.getEventTrackers().get(1);
        assertEquals(eventTracker2.event, NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50);
        assertEquals(eventTracker2.getMethods().size(), 2);
        assertEquals(eventTracker2.getMethods().get(0), NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM);
        assertEquals(eventTracker2.getMethods().get(1), NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);

    }

    @Test
    public void testNativeAdAssets() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertTrue(nativeUnit.params.getAssets().isEmpty());

        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(25);
        NativeImageAsset image = new NativeImageAsset();
        image.setWMin(20);
        image.setHMin(30);
        nativeUnit.params.addAsset(title);
        nativeUnit.params.addAsset(image);

        assertNotNull(nativeUnit.params.getAssets());
        assertEquals(nativeUnit.params.getAssets().size(), 2);
        assertEquals(((NativeTitleAsset) nativeUnit.params.getAssets().get(0)).getLen(), 25);
        assertEquals(((NativeImageAsset) nativeUnit.params.getAssets().get(1)).getHMin(), 30);
        assertEquals(((NativeImageAsset) nativeUnit.params.getAssets().get(1)).getWMin(), 20);
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