package com.openx.apollo.models.openrtb.bidRequests;

import com.openx.apollo.models.ntv.NativeAdConfiguration;
import com.openx.apollo.models.ntv.NativeEventTracker;
import com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetData;
import com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetImage;
import com.openx.apollo.sdk.ApolloSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class NativeTest {

    private NativeAssetData mNativeAssetData;
    private NativeAssetImage mNativeAssetImage;
    private Ext mExt;

    @Before
    public void setUp() {
        mNativeAssetData = new NativeAssetData();
        mNativeAssetData.setLen(100);
        mNativeAssetData.setType(NativeAssetData.DataType.SPONSORED);
        mNativeAssetData.setRequired(true);

        mNativeAssetImage = new NativeAssetImage();
        mNativeAssetImage.setW(100);
        mNativeAssetImage.setH(200);
        mNativeAssetImage.setType(NativeAssetImage.ImageType.ICON);
        mNativeAssetImage.setRequired(true);

        mExt = new Ext();
        mExt.put("test", "test");
    }

    @Test
    public void whenSetRequestFrom_GetJsonObjectEqualsExpectedJson() throws JSONException {
        Native nativeObj = new Native();
        nativeObj.setRequestFrom(getNativeAdConfiguration());
        nativeObj.getExt().put("test", "test");
        assertEquals(getExpectedString(), nativeObj.getJsonObject().toString());
    }

    private NativeAdConfiguration getNativeAdConfiguration() throws JSONException {
        NativeAdConfiguration nativeConfiguration = new NativeAdConfiguration();
        nativeConfiguration.setContextType(NativeAdConfiguration.ContextType.CONTENT_CENTRIC);
        nativeConfiguration.setContextSubType(NativeAdConfiguration.ContextSubType.GENERAL);
        nativeConfiguration.setPlacementType(NativeAdConfiguration.PlacementType.CONTENT_FEED);
        nativeConfiguration.setSeq(0);

        nativeConfiguration.getAssets().add(mNativeAssetData);
        nativeConfiguration.getAssets().add(mNativeAssetImage);

        ArrayList<NativeEventTracker.EventTrackingMethod> trackingMethods = new ArrayList<>();
        trackingMethods.add(NativeEventTracker.EventTrackingMethod.IMAGE);
        trackingMethods.add(NativeEventTracker.EventTrackingMethod.JS);
        NativeEventTracker nativeEventTracker = new NativeEventTracker(NativeEventTracker.EventType.IMPRESSION,
                                                                       trackingMethods);
        nativeEventTracker.setExt(mExt);

        nativeConfiguration.addTracker(nativeEventTracker);
        nativeConfiguration.setExt(mExt);

        return nativeConfiguration;
    }

    private String getExpectedString() throws JSONException {
        JSONArray expectedAssetArray = new JSONArray();
        expectedAssetArray.put(mNativeAssetData.getAssetJsonObject());
        expectedAssetArray.put(mNativeAssetImage.getAssetJsonObject());

        JSONObject expectedExt = new JSONObject();
        expectedExt.put("test", "test");

        JSONArray eventTrackersJsonArray = new JSONArray();
        JSONObject eventTrackerJson = new JSONObject();
        eventTrackerJson.put("event", 1);
        JSONArray methodsJson = new JSONArray();
        methodsJson.put(1);
        methodsJson.put(2);
        eventTrackerJson.put("methods", methodsJson);
        eventTrackerJson.put("ext", expectedExt);
        eventTrackersJsonArray.put(eventTrackerJson);

        JSONObject expectedRequestJson = new JSONObject();
        expectedRequestJson.put("ver", ApolloSettings.NATIVE_VERSION);
        expectedRequestJson.put("context", 1);
        expectedRequestJson.put("contextsubtype", 10);
        expectedRequestJson.put("plcmttype", 1);
        expectedRequestJson.put("seq", 0);
        expectedRequestJson.put("assets", expectedAssetArray);
        expectedRequestJson.put("eventtrackers", eventTrackersJsonArray);
        expectedRequestJson.put("ext", expectedExt);

        JSONObject expectedNativeJson = new JSONObject();
        expectedNativeJson.put("request", expectedRequestJson.toString());
        expectedNativeJson.put("ver", ApolloSettings.NATIVE_VERSION);
        expectedNativeJson.put("ext", expectedExt);

        return expectedNativeJson.toString();
    }
}