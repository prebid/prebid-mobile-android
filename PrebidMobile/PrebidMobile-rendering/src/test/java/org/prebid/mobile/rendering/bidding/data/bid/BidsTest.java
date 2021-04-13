package org.prebid.mobile.rendering.bidding.data.bid;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BidsTest {

    @Test
    public void whenFromJSONObjectAndJSONObjectPassed_ReturnParsedBids()
    throws IOException, JSONException {
        JSONObject jsonBids = new JSONObject(ResourceUtils.convertResourceToString("bidding_bids_obj.json"));
        Bids bids = Bids.fromJSONObject(jsonBids);
        assertNotNull(bids);
        assertEquals("bidsCacheId", bids.getCacheId());
        assertEquals("bidsUrl", bids.getUrl());
    }

    @Test
    public void whenFromJSONObjectAndNullPassed_ReturnNotNull() {
        assertNotNull(Bids.fromJSONObject(null));
    }
}