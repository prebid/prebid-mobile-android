package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps;

import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class BannerTest {
    @Test
    public void getJsonObject() throws Exception {

        Banner banner = new Banner();
        banner.pos = 1;
        banner.api = new int[]{1, 2};
        banner.addFormat(1, 2);

        JSONObject actualObj = banner.getJsonObject();
        String expectedString = "{\"pos\":1,\"format\":[{\"w\":1,\"h\":2}],\"api\":[1,2]}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        banner.getJsonObject();
    }
}