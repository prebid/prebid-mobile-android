package com.openx.apollo.models.openrtb.bidRequests.assets;

import com.openx.apollo.models.openrtb.bidRequests.Ext;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static com.apollo.test.utils.ResourceUtils.assertJsonEquals;

public class NativeAssetTitleTest {

    @Test
    public void getAssetJsonObject_WithValidExt_ExtIsInJson() throws JSONException {
        NativeAssetTitle nativeAsset = new NativeAssetTitle();
        nativeAsset.setRequired(true);
        nativeAsset.setLen(25);
        nativeAsset.getTitleExt().put("key", "value");

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedTitleJsonObject = new JSONObject();

        Ext expectedExt = new Ext();
        expectedExt.put("key", "value");
        expectedJsonObject.put("required", 1);

        expectedTitleJsonObject.put("len", 25);
        expectedTitleJsonObject.put("ext", expectedExt.getJsonObject());

        expectedJsonObject.put("title", expectedTitleJsonObject);

        assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithNullExt_NoExtInJson() throws JSONException {
        NativeAssetTitle nativeAsset = new NativeAssetTitle();
        nativeAsset.setRequired(true);
        nativeAsset.setLen(25);

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedTitleJsonObject = new JSONObject();

        expectedJsonObject.put("required", 1);

        expectedTitleJsonObject.put("len", 25);

        expectedJsonObject.put("title", expectedTitleJsonObject);

        assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }
}