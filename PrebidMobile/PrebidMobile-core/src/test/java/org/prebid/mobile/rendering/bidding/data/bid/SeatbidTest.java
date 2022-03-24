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

package org.prebid.mobile.rendering.bidding.data.bid;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;

import static org.junit.Assert.*;

public class SeatbidTest {

    @Test
    public void whenFromJSONObjectAndJSONObjectPassed_ReturnParsedSeatbid()
    throws IOException, JSONException {
        JSONObject jsonSeatbid = new JSONObject(ResourceUtils.convertResourceToString("bidding_seatbid_obj.json"));
        Seatbid seatbid = Seatbid.fromJSONObject(jsonSeatbid);
        assertNotNull(seatbid);
        assertNotNull(seatbid.getBids());
        assertFalse(seatbid.getBids().isEmpty());
        assertEquals("prebid", seatbid.getSeat());
        assertEquals(1, seatbid.getGroup());
    }

    @Test
    public void whenFromJSONObjectAndNullPassed_ReturnNotNull() {
        assertNotNull(Seatbid.fromJSONObject(null));
    }
}