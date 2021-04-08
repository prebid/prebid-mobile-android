package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps;

import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by latha.shivanna on 11/2/17.
 */
public class DealsTest {

    @Test
    public void getJsonObjectTest() throws Exception {

        Deals deals = new Deals();
        deals.id = "blah";
        deals.bidfloor = 1f;
        deals.bidfloorcur = "USD";
        deals.at = 1;
        deals.wseat = new String[]{"seat", "seat1"};
        deals.wadomain = new String[]{"domain", "domain1"};

        JSONObject actualObj = deals.getJsonObject();
        String expectedString = "{\"wadomain\":[\"domain\",\"domain1\"],\"at\":1,\"bidfloor\":1,\"bidfloorcur\":\"USD\",\"id\":\"blah\",\"wseat\":[\"seat\",\"seat1\"]}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        deals.getJsonObject();
    }
}