package org.prebid.mobile.rendering.models.openrtb.bidRequests.assets;

import com.apollo.test.utils.ResourceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class NativeAssetImageTest {

    @Test
    public void getAssetJsonObject_WithValidExt_ExtIsInJson() throws JSONException {
        NativeAssetImage nativeAsset = new NativeAssetImage();
        nativeAsset.setRequired(true);
        nativeAsset.setType(NativeAssetImage.ImageType.MAIN);
        nativeAsset.setMimes(new String[]{"jpeg", "gif"});
        nativeAsset.setH(1);
        nativeAsset.setHMin(2);
        nativeAsset.setW(3);
        nativeAsset.setWMin(4);
        nativeAsset.getImageExt().put("key", "value");

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedImageJsonObject = new JSONObject();
        Ext expectedExt = new Ext();
        JSONArray expectedMimesJsonArray = new JSONArray();

        expectedExt.put("key", "value");
        expectedJsonObject.put("required", 1);

        expectedImageJsonObject.put("type", 3);
        expectedImageJsonObject.put("h", 1);
        expectedImageJsonObject.put("hmin", 2);
        expectedImageJsonObject.put("w", 3);
        expectedImageJsonObject.put("wmin", 4);

        expectedImageJsonObject.put("ext", expectedExt.getJsonObject());

        expectedMimesJsonArray.put("jpeg");
        expectedMimesJsonArray.put("gif");
        expectedImageJsonObject.put("mimes", expectedMimesJsonArray);

        expectedJsonObject.put("img", expectedImageJsonObject);

        ResourceUtils.assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithNullValues_NullValuesAreNotIncludedInJson() throws JSONException {
        NativeAssetImage nativeAsset = new NativeAssetImage();
        nativeAsset.setRequired(true);
        nativeAsset.setMimes(new String[]{"jpeg", "gif"});

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedImageJsonObject = new JSONObject();
        JSONArray expectedMimesJsonArray = new JSONArray();

        expectedJsonObject.put("required", 1);

        expectedMimesJsonArray.put("jpeg");
        expectedMimesJsonArray.put("gif");
        expectedImageJsonObject.put("mimes", expectedMimesJsonArray);

        expectedJsonObject.put("img", expectedImageJsonObject);

        ResourceUtils.assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithInvalidCustomImageTypeId_JsonShouldContainCustomIdSetPreviously() throws JSONException {
        NativeAssetImage nativeAsset = new NativeAssetImage();
        nativeAsset.setRequired(true);
        NativeAssetImage.ImageType custom = NativeAssetImage.ImageType.CUSTOM;
        custom.setId(500);
        custom.setId(3);
        nativeAsset.setType(custom);

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedImageJsonObject = new JSONObject();

        expectedJsonObject.put("required", 1);

        expectedImageJsonObject.put("type", 500);

        expectedJsonObject.put("img", expectedImageJsonObject);

        ResourceUtils.assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithValidCustomImageType_JsonShouldIncludeCustomImageType() throws JSONException {
        NativeAssetImage nativeAsset = new NativeAssetImage();
        nativeAsset.setRequired(true);
        NativeAssetImage.ImageType custom = NativeAssetImage.ImageType.CUSTOM;
        custom.setId(600);
        nativeAsset.setType(custom);

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedImageJsonObject = new JSONObject();

        expectedJsonObject.put("required", 1);
        expectedImageJsonObject.put("type", 600);

        expectedJsonObject.put("img", expectedImageJsonObject);

        ResourceUtils.assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }
}