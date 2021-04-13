package org.prebid.mobile.rendering.bidding.data.bid;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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