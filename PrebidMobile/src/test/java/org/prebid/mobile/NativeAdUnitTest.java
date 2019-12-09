package org.prebid.mobile;

import org.apache.commons.lang3.reflect.FieldUtils;
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
import static junit.framework.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class NativeAdUnitTest {
    static final String PBS_CONFIG_ID_NATIVE_APPNEXUS = "1f85e687-b45f-4649-a4d5-65f74f2ede8e";

    @After
    public void tearDown() throws Exception {
        NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(500);
    }

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
        assertEquals(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC, nativeUnit.params.getContextType());
        assertEquals(1, nativeUnit.params.getContextType().getID());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC, nativeUnit.params.getContextType());
        assertEquals(2, nativeUnit.params.getContextType().getID());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.PRODUCT, nativeUnit.params.getContextType());
        assertEquals(3, nativeUnit.params.getContextType().getID());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.CUSTOM);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.CUSTOM, nativeUnit.params.getContextType());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(500);
        assertEquals(500, nativeUnit.params.getContextType().getID());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(600);
        assertEquals(600, nativeUnit.params.getContextType().getID());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(1);
        assertEquals(600, nativeUnit.params.getContextType().getID());
        assertFalse("Invalid CustomId", 1 == nativeUnit.params.getContextType().getID());
    }

    @Test
    public void testNativeAdContextSubType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.params.getContextsubtype());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        assertNotNull(nativeUnit.params.getContextsubtype());
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.GENERAL, nativeUnit.params.getContextsubtype());
        assertEquals(10, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL, nativeUnit.params.getContextsubtype());
        assertEquals(11, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.VIDEO, nativeUnit.params.getContextsubtype());
        assertEquals(12, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.AUDIO, nativeUnit.params.getContextsubtype());
        assertEquals(13, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.IMAGE, nativeUnit.params.getContextsubtype());
        assertEquals(14, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED, nativeUnit.params.getContextsubtype());
        assertEquals(15, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL, nativeUnit.params.getContextsubtype());
        assertEquals(20, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.EMAIL, nativeUnit.params.getContextsubtype());
        assertEquals(21, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM, nativeUnit.params.getContextsubtype());
        assertEquals(22, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.SELLING, nativeUnit.params.getContextsubtype());
        assertEquals(30, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE, nativeUnit.params.getContextsubtype());
        assertEquals(31, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES, nativeUnit.params.getContextsubtype());
        assertEquals(32, nativeUnit.params.getContextsubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM, nativeUnit.params.getContextsubtype());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(500);
        assertEquals(500, nativeUnit.params.getContextsubtype().getID());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(600);
        assertEquals(600, nativeUnit.params.getContextsubtype().getID());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(10);
        assertEquals(600, nativeUnit.params.getContextsubtype().getID());
        assertFalse("Invalid CustomId", 1 == nativeUnit.params.getContextsubtype().getID());

    }

    @Test
    public void testNativeAdPlacementType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.params.getPlacementType());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        assertNotNull(nativeUnit.params.getPlacementType());
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED, nativeUnit.params.getPlacementType());
        assertEquals(1, nativeUnit.params.getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT, nativeUnit.params.getPlacementType());
        assertEquals(2, nativeUnit.params.getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT, nativeUnit.params.getPlacementType());
        assertEquals(3, nativeUnit.params.getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET, nativeUnit.params.getPlacementType());
        assertEquals(4, nativeUnit.params.getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CUSTOM);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CUSTOM, nativeUnit.params.getPlacementType());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(500);
        assertEquals(500, nativeUnit.params.getPlacementType().getID());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(600);
        assertEquals(600, nativeUnit.params.getPlacementType().getID());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(1);
        assertEquals(600, nativeUnit.params.getPlacementType().getID());
        assertFalse("Invalid CustomId", 1 == nativeUnit.params.getPlacementType().getID());
    }

    @Test
    public void testNativeAdPlacementCount() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(1, nativeUnit.params.getPlacementCount());
        nativeUnit.setPlacementCount(123);
        assertEquals(123, nativeUnit.params.getPlacementCount());
    }

    @Test
    public void testNativeAdSequence() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(0, nativeUnit.params.getSeq());
        nativeUnit.setSeq(1);
        assertEquals(1, nativeUnit.params.getSeq());
    }

    @Test
    public void testNativeAdAsseturlSupport() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(false, nativeUnit.params.isAUrlSupport());
        nativeUnit.setAUrlSupport(true);
        assertEquals(true, nativeUnit.params.isAUrlSupport());
    }

    @Test
    public void testNativeAdDUrlSupport() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(false, nativeUnit.params.isDUrlSupport());
        nativeUnit.setDUrlSupport(true);
        assertEquals(true, nativeUnit.params.isDUrlSupport());
    }

    @Test
    public void testNativeAdPrivacy() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(false, nativeUnit.params.isPrivacy());
        nativeUnit.setPrivacy(true);
        assertEquals(true, nativeUnit.params.isPrivacy());
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
        assertEquals(0, nativeUnit.params.getEventTrackers().size());
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
        assertEquals(2, nativeUnit.params.getEventTrackers().size());

        NativeEventTracker eventTracker1 = nativeUnit.params.getEventTrackers().get(0);
        assertEquals(NativeEventTracker.EVENT_TYPE.IMPRESSION, eventTracker1.event);
        assertEquals(1, eventTracker1.event.getID());
        assertEquals(2, eventTracker1.getMethods().size());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE, eventTracker1.getMethods().get(0));
        assertEquals(1, eventTracker1.getMethods().get(0).getID());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.JS, eventTracker1.getMethods().get(1));
        assertEquals(2, eventTracker1.getMethods().get(1).getID());

        NativeEventTracker eventTracker2 = nativeUnit.params.getEventTrackers().get(1);
        assertEquals(NativeEventTracker.EVENT_TYPE.VIEWABLE_MRC50, eventTracker2.event);
        assertEquals(2, eventTracker2.event.getID());
        assertEquals(2, eventTracker2.getMethods().size());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM, eventTracker2.getMethods().get(0));
        assertEquals(500, eventTracker2.getMethods().get(0).getID());
        try {
            NativeEventTracker.EVENT_TRACKING_METHOD.CUSTOM.setID(600);
        } catch (Exception e) {
            fail();
        }
        assertEquals(600, eventTracker2.getMethods().get(0).getID());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE, eventTracker2.getMethods().get(1));
        assertEquals(1, eventTracker2.getMethods().get(1).getID());

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
        assertEquals(2, nativeUnit.params.getAssets().size());
        assertEquals(25, ((NativeTitleAsset) nativeUnit.params.getAssets().get(0)).getLen());
        assertEquals(30, ((NativeImageAsset) nativeUnit.params.getAssets().get(1)).getHMin());
        assertEquals(20, ((NativeImageAsset) nativeUnit.params.getAssets().get(1)).getWMin());
    }

}