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

public class NativeAssetTest {
    @Test
    public void getParentJsonObject_EqualsToExpected() throws JSONException {
        NativeAsset nativeAsset = new NativeAsset() {
            @Override
            public JSONObject getAssetJsonObject() throws JSONException {
                return null;
            }
        };
        nativeAsset.setRequired(true);
        nativeAsset.getAssetExt().put("key", "value");

        JSONObject expectedJsonObject = new JSONObject();
        Ext expectedExt = new Ext();
        expectedExt.put("key", "value");

        expectedJsonObject.put("required", 1);
        expectedJsonObject.put("ext", expectedExt.getJsonObject());

        assertJsonEquals(expectedJsonObject, nativeAsset.getParentJsonObject());
    }
}