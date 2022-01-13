/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.models.openrtb.bidRequests.assets;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

import static org.prebid.mobile.test.utils.ResourceUtils.assertJsonEquals;

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