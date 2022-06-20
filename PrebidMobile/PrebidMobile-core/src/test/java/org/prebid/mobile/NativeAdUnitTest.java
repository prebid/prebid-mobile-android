package org.prebid.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.EnumSet;

import static org.junit.Assert.*;

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
        AdUnitConfiguration configuration = nativeUnit.getConfiguration();

        assertNotNull(nativeUnit);
        assertEquals(PBS_CONFIG_ID_NATIVE_APPNEXUS, configuration.getConfigId());
        assertEquals(EnumSet.of(AdFormat.NATIVE), configuration.getAdFormats());
        assertNotNull(configuration.getNativeConfiguration());
    }

    @Test
    public void testNativeAdContextType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.getNativeConfiguration().getContextType());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC);
        assertNotNull(nativeUnit.getNativeConfiguration().getContextType());
        assertEquals(NativeAdUnit.CONTEXT_TYPE.CONTENT_CENTRIC, nativeUnit.getNativeConfiguration().getContextType());
        assertEquals(1, nativeUnit.getNativeConfiguration().getContextType().getID());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC, nativeUnit.getNativeConfiguration().getContextType());
        assertEquals(2, nativeUnit.getNativeConfiguration().getContextType().getID());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.PRODUCT);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.PRODUCT, nativeUnit.getNativeConfiguration().getContextType());
        assertEquals(3, nativeUnit.getNativeConfiguration().getContextType().getID());
        nativeUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.CUSTOM);
        assertEquals(NativeAdUnit.CONTEXT_TYPE.CUSTOM, nativeUnit.getNativeConfiguration().getContextType());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(500);
        assertEquals(500, nativeUnit.getNativeConfiguration().getContextType().getID());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(600);
        assertEquals(600, nativeUnit.getNativeConfiguration().getContextType().getID());
        NativeAdUnit.CONTEXT_TYPE.CUSTOM.setID(1);
        assertEquals(600, nativeUnit.getNativeConfiguration().getContextType().getID());
        assertFalse("Invalid CustomId", 1 == nativeUnit.getNativeConfiguration().getContextType().getID());
    }

    @Test
    public void testNativeAdContextSubType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.getNativeConfiguration().getContextSubtype());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL);
        assertNotNull(nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.GENERAL, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(10, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.ARTICAL, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(11, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.VIDEO);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.VIDEO, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(12, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.AUDIO);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.AUDIO, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(13, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.IMAGE);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.IMAGE, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(14, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.USER_GENERATED, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(15, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(20, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.EMAIL);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.EMAIL, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(21, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.CHAT_IM, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(22, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.SELLING);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.SELLING, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(30, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.APPLICATION_STORE, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(31, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.PRODUCT_REVIEW_SITES, nativeUnit.getNativeConfiguration().getContextSubtype());
        assertEquals(32, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        nativeUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM);
        assertEquals(NativeAdUnit.CONTEXTSUBTYPE.CUSTOM, nativeUnit.getNativeConfiguration().getContextSubtype());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(500);
        assertEquals(500, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(600);
        assertEquals(600, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        NativeAdUnit.CONTEXTSUBTYPE.CUSTOM.setID(10);
        assertEquals(600, nativeUnit.getNativeConfiguration().getContextSubtype().getID());
        assertFalse("Invalid CustomId", 1 == nativeUnit.getNativeConfiguration().getContextSubtype().getID());

    }

    @Test
    public void testNativeAdPlacementType() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNotNull(nativeUnit);
        assertNull(nativeUnit.getNativeConfiguration().getPlacementType());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        assertNotNull(nativeUnit.getNativeConfiguration().getPlacementType());
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED, nativeUnit.getNativeConfiguration().getPlacementType());
        assertEquals(1, nativeUnit.getNativeConfiguration().getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CONTENT_ATOMIC_UNIT, nativeUnit.getNativeConfiguration().getPlacementType());
        assertEquals(2, nativeUnit.getNativeConfiguration().getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.OUTSIDE_CORE_CONTENT, nativeUnit.getNativeConfiguration().getPlacementType());
        assertEquals(3, nativeUnit.getNativeConfiguration().getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET, nativeUnit.getNativeConfiguration().getPlacementType());
        assertEquals(4, nativeUnit.getNativeConfiguration().getPlacementType().getID());
        nativeUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CUSTOM);
        assertEquals(NativeAdUnit.PLACEMENTTYPE.CUSTOM, nativeUnit.getNativeConfiguration().getPlacementType());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(500);
        assertEquals(500, nativeUnit.getNativeConfiguration().getPlacementType().getID());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(600);
        assertEquals(600, nativeUnit.getNativeConfiguration().getPlacementType().getID());
        NativeAdUnit.PLACEMENTTYPE.CUSTOM.setID(1);
        assertEquals(600, nativeUnit.getNativeConfiguration().getPlacementType().getID());
        assertFalse("Invalid CustomId", 1 == nativeUnit.getNativeConfiguration().getPlacementType().getID());
    }

    @Test
    public void testNativeAdPlacementCount() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(1, nativeUnit.getNativeConfiguration().getPlacementCount());
        nativeUnit.setPlacementCount(123);
        assertEquals(123, nativeUnit.getNativeConfiguration().getPlacementCount());
    }

    @Test
    public void testNativeAdSequence() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(0, nativeUnit.getNativeConfiguration().getSeq());
        nativeUnit.setSeq(1);
        assertEquals(1, nativeUnit.getNativeConfiguration().getSeq());
    }

    @Test
    public void testNativeAdAsseturlSupport() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertFalse(nativeUnit.getNativeConfiguration().getAUrlSupport());
        nativeUnit.setAUrlSupport(true);
        assertTrue(nativeUnit.getNativeConfiguration().getAUrlSupport());
    }

    @Test
    public void testNativeAdDUrlSupport() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertFalse(nativeUnit.getNativeConfiguration().getDUrlSupport());
        nativeUnit.setDUrlSupport(true);
        assertTrue(nativeUnit.getNativeConfiguration().getDUrlSupport());
    }

    @Test
    public void testNativeAdPrivacy() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertFalse(nativeUnit.getNativeConfiguration().getPrivacy());
        nativeUnit.setPrivacy(true);
        assertTrue(nativeUnit.getNativeConfiguration().getPrivacy());
    }

    @Test
    public void testNativeAdExt() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertNull(nativeUnit.getNativeConfiguration().getExt());
        JSONObject ext = new JSONObject();
        try {
            ext.put("key", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        nativeUnit.setExt(ext);
        String value = "";
        try {
            JSONObject data = (JSONObject) nativeUnit.getNativeConfiguration().getExt();
            value = data.getString("key");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value", value);
    }

    @Test
    public void testNativeAdEventTrackers() {
        NativeAdUnit nativeUnit = new NativeAdUnit(PBS_CONFIG_ID_NATIVE_APPNEXUS);
        assertEquals(0, nativeUnit.getNativeConfiguration().getEventTrackers().size());
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
        assertEquals(2, nativeUnit.getNativeConfiguration().getEventTrackers().size());

        NativeEventTracker eventTracker1 = nativeUnit.getNativeConfiguration().getEventTrackers().get(0);
        assertEquals(NativeEventTracker.EVENT_TYPE.IMPRESSION, eventTracker1.event);
        assertEquals(1, eventTracker1.event.getID());
        assertEquals(2, eventTracker1.getMethods().size());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE, eventTracker1.getMethods().get(0));
        assertEquals(1, eventTracker1.getMethods().get(0).getID());
        assertEquals(NativeEventTracker.EVENT_TRACKING_METHOD.JS, eventTracker1.getMethods().get(1));
        assertEquals(2, eventTracker1.getMethods().get(1).getID());

        NativeEventTracker eventTracker2 = nativeUnit.getNativeConfiguration().getEventTrackers().get(1);
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
        assertTrue(nativeUnit.getNativeConfiguration().getAssets().isEmpty());

        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(25);
        NativeImageAsset image = new NativeImageAsset();
        image.setWMin(20);
        image.setHMin(30);
        nativeUnit.getNativeConfiguration().addAsset(title);
        nativeUnit.getNativeConfiguration().addAsset(image);

        assertNotNull(nativeUnit.getNativeConfiguration().getAssets());
        assertEquals(2, nativeUnit.getNativeConfiguration().getAssets().size());
        assertEquals(25, ((NativeTitleAsset) nativeUnit.getNativeConfiguration().getAssets().get(0)).getLen());
        assertEquals(30, ((NativeImageAsset) nativeUnit.getNativeConfiguration().getAssets().get(1)).getHMin());
        assertEquals(20, ((NativeImageAsset) nativeUnit.getNativeConfiguration().getAssets().get(1)).getWMin());
    }

}