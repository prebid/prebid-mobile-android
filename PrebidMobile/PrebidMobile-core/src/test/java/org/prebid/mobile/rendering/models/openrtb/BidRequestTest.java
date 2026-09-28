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

package org.prebid.mobile.rendering.models.openrtb;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.prebid.mobile.EidsPlacement;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.*;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BidRequestTest {

    private static final String SDK_EID = "{\"source\":\"id5-sync.com\",\"uids\":[{\"id\":\"id5-uid\",\"atype\":1}],\"inserter\":\"prebid.org\",\"matcher\":\"id5-sync.com\",\"mm\":3}";
    private static final String PUBLISHER_EID = "{\"source\":\"publisher.com\",\"uids\":[{\"id\":\"publisher-uid\",\"atype\":3}]}";

    @After
    public void tearDown() {
        PrebidMobile.setEidsPlacement(EidsPlacement.COMPATIBLE);
        TargetingParams.setGlobalOrtbConfig(null);
    }

    @Test
    public void getJsonObject() throws Exception {
        BidRequest bidReq = new BidRequest();

        App app = new App();
        app.id = "auid";
        bidReq.setApp(app);
        Device device = new Device();
        device.h = 1111;
        bidReq.setDevice(device);

        Imp imp = new Imp();
        imp.instl = 0;
        ArrayList<Imp> imps = new ArrayList<>();
        imps.add(imp);
        bidReq.setImp(imps);
        Regs regs = new Regs();
        regs.coppa = 0;
        bidReq.setRegs(regs);

        User user = new User();
        user.keywords = "q, o";
        bidReq.setUser(user);

        JSONObject actualObj = bidReq.getJsonObject();
        String expectedString = "{\"app\":{\"id\":\"auid\"},\"regs\":{\"coppa\":0},\"imp\":[{\"instl\":0}],\"device\":{\"h\":1111},\"user\":{\"keywords\":\"q, o\"}}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        bidReq.getJsonObject();
    }

    @Test
    public void eidsPlacement_defaultIsCompatible() {
        assertEquals(EidsPlacement.COMPATIBLE, PrebidMobile.getEidsPlacement());
    }

    @Test
    public void eidsPlacementCompatible_sameEidsInUserAndUserExt() throws Exception {
        PrebidMobile.setEidsPlacement(EidsPlacement.COMPATIBLE);

        JSONObject user = requestWithSdkEids().getJsonObject().getJSONObject("user");

        assertEquals(eids(SDK_EID), user.getJSONArray("eids").toString());
        assertEquals(eids(SDK_EID), user.getJSONObject("ext").getJSONArray("eids").toString());
    }

    @Test
    public void eidsPlacementOpenRtb26_eidsOnlyInUser() throws Exception {
        PrebidMobile.setEidsPlacement(EidsPlacement.OPEN_RTB_2_6);
        BidRequest request = requestWithSdkEids();
        request.getUser().getExt().put("custom", "value");

        JSONObject user = request.getJsonObject().getJSONObject("user");

        assertEquals(eids(SDK_EID), user.getJSONArray("eids").toString());
        assertEquals("{\"custom\":\"value\"}", user.getJSONObject("ext").toString());
    }

    @Test
    public void eidsPlacementOpenRtb25_eidsOnlyInUserExt() throws Exception {
        PrebidMobile.setEidsPlacement(EidsPlacement.OPEN_RTB_2_5);

        JSONObject user = requestWithSdkEids().getJsonObject().getJSONObject("user");

        assertFalse(user.has("eids"));
        assertEquals(eids(SDK_EID), user.getJSONObject("ext").getJSONArray("eids").toString());
    }

    @Test
    public void eidsPlacement_withoutEids_nothingAdded() throws Exception {
        for (EidsPlacement placement : EidsPlacement.values()) {
            PrebidMobile.setEidsPlacement(placement);
            BidRequest request = new BidRequest();
            request.getUser().keywords = "keyword";

            JSONObject user = request.getJsonObject().getJSONObject("user");

            assertEquals("{\"keywords\":\"keyword\"}", user.toString());
        }
    }

    @Test
    public void eidsPlacement_changedBetweenRequests_leavesNoStaleEids() throws Exception {
        BidRequest request = requestWithSdkEids();

        PrebidMobile.setEidsPlacement(EidsPlacement.OPEN_RTB_2_5);
        JSONObject user = request.getJsonObject().getJSONObject("user");
        assertFalse(user.has("eids"));
        assertEquals(eids(SDK_EID), user.getJSONObject("ext").getJSONArray("eids").toString());

        PrebidMobile.setEidsPlacement(EidsPlacement.OPEN_RTB_2_6);
        user = request.getJsonObject().getJSONObject("user");
        assertEquals(eids(SDK_EID), user.getJSONArray("eids").toString());
        assertFalse(user.has("ext"));

        PrebidMobile.setEidsPlacement(EidsPlacement.OPEN_RTB_2_5);
        user = request.getJsonObject().getJSONObject("user");
        assertFalse(user.has("eids"));
        assertEquals(eids(SDK_EID), user.getJSONObject("ext").getJSONArray("eids").toString());
    }

    @Test
    public void eidsPlacementOpenRtb26_includesGlobalOrtbConfigEids() throws Exception {
        PrebidMobile.setEidsPlacement(EidsPlacement.OPEN_RTB_2_6);
        TargetingParams.setGlobalOrtbConfig("{\"user\":{\"ext\":{\"eids\":[" + PUBLISHER_EID + "]}}}");

        JSONObject user = requestWithSdkEids().getJsonObject().getJSONObject("user");

        assertEquals(eids(SDK_EID, PUBLISHER_EID), user.getJSONArray("eids").toString());
        assertFalse(user.has("ext"));
    }

    @Test
    public void placeEids_doesNotDuplicateEidsPresentInBothLocations() throws Exception {
        JSONObject request = new JSONObject("{\"user\":{\"eids\":[" + PUBLISHER_EID + "],\"ext\":{\"eids\":[" + PUBLISHER_EID + "," + SDK_EID + "]}}}");

        BidRequest.placeEids(request, EidsPlacement.COMPATIBLE);

        JSONObject user = request.getJSONObject("user");
        assertEquals(eids(PUBLISHER_EID, SDK_EID), user.getJSONArray("eids").toString());
        assertEquals(eids(PUBLISHER_EID, SDK_EID), user.getJSONObject("ext").getJSONArray("eids").toString());
    }

    @Test
    public void placeEids_isIdempotent() throws Exception {
        JSONObject request = new JSONObject("{\"user\":{\"ext\":{\"eids\":[" + SDK_EID + "]}}}");

        BidRequest.placeEids(request, EidsPlacement.COMPATIBLE);
        String once = request.toString();
        BidRequest.placeEids(request, EidsPlacement.COMPATIBLE);

        assertEquals(once, request.toString());
    }

    private static BidRequest requestWithSdkEids() throws Exception {
        BidRequest request = new BidRequest();
        request.getUser().getExt().put("eids", new JSONArray("[" + SDK_EID + "]"));
        return request;
    }

    private static String eids(String... eids) throws Exception {
        return new JSONArray("[" + String.join(",", eids) + "]").toString();
    }
}
