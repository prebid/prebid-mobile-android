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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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