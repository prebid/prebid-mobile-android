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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps.Deals;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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