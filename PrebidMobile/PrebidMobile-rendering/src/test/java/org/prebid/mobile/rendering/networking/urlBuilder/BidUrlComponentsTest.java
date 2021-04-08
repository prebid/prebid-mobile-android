package org.prebid.mobile.rendering.networking.urlBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidUrlComponentsTest {

    @Test
    public void whenGetQueryArgString_ReturnBidRequestJson() throws JSONException {
        AdRequestInput adRequestInput = new AdRequestInput();
        BidRequest mockBidRequest = mock(BidRequest.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "test");

        when(mockBidRequest.getJsonObject()).thenReturn(jsonObject);
        adRequestInput.setBidRequest(mockBidRequest);

        BidUrlComponents bidUrlComponents = new BidUrlComponents("", adRequestInput);
        assertEquals(jsonObject.toString(), bidUrlComponents.getQueryArgString());
    }
}