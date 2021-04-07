package com.openx.apollo.models.openrtb.bidRequests.assets;

import com.openx.apollo.models.openrtb.bidRequests.Ext;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static com.apollo.test.utils.ResourceUtils.assertJsonEquals;

public class NativeAssetDataTest {

    @Test
    public void getAssetJsonObject_AllValuesAssigned_AssignedValuesPresentInJson() throws JSONException {
        NativeAssetData nativeAsset = new NativeAssetData();
        nativeAsset.setRequired(true);
        nativeAsset.setLen(30);
        nativeAsset.setType(NativeAssetData.DataType.DOWNLOADS);
        nativeAsset.getDataExt().put("key", "value");

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedDataJsonObject = new JSONObject();
        Ext expectedExt = new Ext();

        expectedExt.put("key", "value");
        expectedJsonObject.put("required", 1);
        expectedDataJsonObject.put("len", 30);
        expectedDataJsonObject.put("type", 5);
        expectedDataJsonObject.put("ext", expectedExt.getJsonObject());

        expectedJsonObject.put("data", expectedDataJsonObject);

        assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithNullValues_NullValuesAreNotIncludedInJson() throws JSONException {
        NativeAssetData nativeAsset = new NativeAssetData();
        nativeAsset.setRequired(true);
        nativeAsset.setLen(30);

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedDataJsonObject = new JSONObject();

        expectedJsonObject.put("required", 1);
        expectedDataJsonObject.put("len", 30);

        expectedJsonObject.put("data", expectedDataJsonObject);

        assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithInvalidCustomImageTypeId_JsonShouldContainCustomIdSetPreviously() throws JSONException {
        NativeAssetData nativeAsset = new NativeAssetData();
        nativeAsset.setRequired(true);
        NativeAssetData.DataType custom = NativeAssetData.DataType.CUSTOM;
        custom.setId(500);
        custom.setId(3);
        nativeAsset.setType(custom);
        nativeAsset.setRequired(true);

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedImageJsonObject = new JSONObject();

        expectedJsonObject.put("required", 1);

        expectedImageJsonObject.put("type", 500);

        expectedJsonObject.put("data", expectedImageJsonObject);

        assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithValidCustomImageType_JsonShouldIncludeCustomImageType() throws JSONException {
        NativeAssetData nativeAsset = new NativeAssetData();
        nativeAsset.setRequired(true);
        NativeAssetData.DataType custom = NativeAssetData.DataType.CUSTOM;
        custom.setId(600);
        nativeAsset.setType(custom);

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedImageJsonObject = new JSONObject();

        expectedJsonObject.put("required", 1);
        expectedImageJsonObject.put("type", 600);

        expectedJsonObject.put("data", expectedImageJsonObject);

        assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }
}