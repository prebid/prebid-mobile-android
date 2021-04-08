package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps.Deals;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by latha.shivanna on 11/2/17.
 */
public class PmpTest {
    @Test
    public void getJsonObject() throws Exception {

        Pmp pmp = new Pmp();
        pmp.private_auction = 1;
        pmp.deals = new ArrayList<>();

        JSONObject actualObj = pmp.getJsonObject();
        String expectedString = "{\"private_auction\":1}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        pmp.getJsonObject();

        Pmp pmp1 = new Pmp();
        pmp1.private_auction = 1;

        List test = new ArrayList<>();
        Deals testDeal = new Deals();
        test.add(testDeal);
        pmp1.deals = test;

        JSONObject actualObj1 = pmp1.getJsonObject();
        String expectedString1 = "{\"deals\":[{}],\"private_auction\":1}";
        assertEquals("got: " + actualObj1.toString(), expectedString1, actualObj1.toString());
        pmp1.getJsonObject();
    }
}