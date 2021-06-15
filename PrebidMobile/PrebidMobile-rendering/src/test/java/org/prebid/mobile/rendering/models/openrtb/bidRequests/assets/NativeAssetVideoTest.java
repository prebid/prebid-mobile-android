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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.test.utils.ResourceUtils;

public class NativeAssetVideoTest {

    @Test
    public void getAssetJsonObject_AllValuesAssigned_AssignedValuesPresentInJson() throws JSONException {
        NativeAssetVideo nativeAsset = new NativeAssetVideo();
        nativeAsset.setRequired(true);
        nativeAsset.setMinDuration(4);
        nativeAsset.setMaxDuration(5);
        nativeAsset.setMimes(new String[]{"mp4"});
        nativeAsset.setProtocols(new Integer[]{1, 2});
        nativeAsset.getVideoExt().put("key", "value");

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedVideoJsonObject = new JSONObject();
        Ext expectedExt = new Ext();
        JSONArray expectedMimesJsonArray = new JSONArray();
        JSONArray expectedProtocolsJsonArray = new JSONArray();

        expectedJsonObject.put("required", 1);

        expectedExt.put("key", "value");
        expectedVideoJsonObject.put("minduration", 4);
        expectedVideoJsonObject.put("maxduration", 5);
        expectedVideoJsonObject.put("ext", expectedExt.getJsonObject());

        expectedProtocolsJsonArray.put(1);
        expectedProtocolsJsonArray.put(2);
        expectedMimesJsonArray.put("mp4");

        expectedVideoJsonObject.put("mimes", expectedMimesJsonArray);
        expectedVideoJsonObject.put("protocols", expectedProtocolsJsonArray);

        expectedJsonObject.put("video", expectedVideoJsonObject);

        ResourceUtils.assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }

    @Test
    public void getAssetJsonObject_WithNullValues_NullValuesAreNotIncludedInJson() throws JSONException {
        NativeAssetVideo nativeAsset = new NativeAssetVideo();
        nativeAsset.setRequired(true);
        nativeAsset.setProtocols(new Integer[]{1, 2});

        JSONObject expectedJsonObject = new JSONObject();
        JSONObject expectedVideoJsonObject = new JSONObject();
        JSONArray expectedProtocolsJsonArray = new JSONArray();

        expectedJsonObject.put("required", 1);

        expectedProtocolsJsonArray.put(1);
        expectedProtocolsJsonArray.put(2);

        expectedVideoJsonObject.put("protocols", expectedProtocolsJsonArray);

        expectedJsonObject.put("video", expectedVideoJsonObject);

        ResourceUtils.assertJsonEquals(expectedJsonObject, nativeAsset.getAssetJsonObject());
    }
}