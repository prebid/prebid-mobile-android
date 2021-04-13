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