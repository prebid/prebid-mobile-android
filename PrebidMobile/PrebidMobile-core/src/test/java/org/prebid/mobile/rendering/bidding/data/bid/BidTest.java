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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class BidTest {

    @Test
    public void whenFromJSONObjectAndJSONObjectPassed_ReturnParsedBid()
    throws IOException, JSONException {
        JSONObject jsonBid = new JSONObject(ResourceUtils.convertResourceToString("bidding_bid_obj.json"));
        Bid bid = Bid.fromJSONObject(jsonBid);
        assertEquals("adm", bid.getAdm());
        assertEquals("nurl", bid.getNurl());
        assertNull(bid.getEvents());
        verifyBid(bid);
    }

    @Test
    public void whenFromJSONObject_JSONObjectContainingMacrosPassed_ReturnParsedBidWithReplacedMacros()
    throws IOException, JSONException {
        JSONObject jsonBid = new JSONObject(ResourceUtils.convertResourceToString("bidding_bid_obj_macros.json"));
        Bid bid = Bid.fromJSONObject(jsonBid);
        assertNotNull(bid);
        assertEquals("exampleadm?price=0.15&with_base64=MC4xNQ==", bid.getAdm());
        assertEquals("http://textlink.com?price=0.15", bid.getNurl());
        verifyBid(bid);
    }

    @Test
    public void events_eventListContainsWinEvent() throws Exception {
        JSONObject jsonBid = new JSONObject(ResourceUtils.convertResourceToString("bidding_bid_obj_events.json"));
        Bid bid = Bid.fromJSONObject(jsonBid);

        Map<String, String> events = bid.getEvents();
        assertEquals(2, events.size());

        String value = events.get(BidInfo.EVENT_WIN);
        assertEquals(value, "https://win.com");

        value = events.get(BidInfo.EVENT_IMP);
        assertEquals(value, "https://imp.com");

        verifyBid(bid);
    }

    @Test
    public void whenFromJSONObjectAndNullPassed_ReturnNotNull() {
        assertNotNull(Bid.fromJSONObject(null));
    }

    private void verifyBid(Bid bid) {
        assertNotNull(bid);
        assertEquals("bidId", bid.getId());
        assertEquals("impId", bid.getImpId());
        assertEquals(0.15, bid.getPrice(), 0);
        assertEquals("crid", bid.getCrid());
        assertEquals(320, bid.getWidth());
        assertEquals(50, bid.getHeight());
        assertNotNull(bid.getPrebid());
        assertEquals("burl", bid.getBurl());
        assertEquals("lurl", bid.getLurl());
        assertEquals("adid", bid.getAdid());
        assertArrayEquals(new String[]{"domain1", "domain2"}, bid.getAdomain());
        assertEquals("bundle", bid.getBundle());
        assertEquals("iurl", bid.getIurl());
        assertEquals("cid", bid.getCid());
        assertEquals("tactic", bid.getTactic());
        assertArrayEquals(new String[]{"cat1", "cat2"}, bid.getCat());
        assertArrayEquals(new int[]{1, 2}, bid.getAttr());
        assertEquals(111, bid.getApi());
        assertEquals(112, bid.getProtocol());
        assertEquals(113, bid.getQagmediarating());
        assertEquals("language", bid.getLanguage());
        assertEquals("dealid", bid.getDealId());
        assertEquals(114, bid.getWRatio());
        assertEquals(115, bid.getHRatio());
        assertEquals(116, bid.getExp());
    }
}