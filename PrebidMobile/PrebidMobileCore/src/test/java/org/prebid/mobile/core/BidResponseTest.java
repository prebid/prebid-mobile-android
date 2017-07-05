package org.prebid.mobile.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.unittestutils.Lock;
import org.prebid.mobile.unittestutils.ServerResponsesBuilder;
import org.prebid.mobile.unittestutils.TestConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class BidResponseTest {

    @Test
    public void testObjectCreation() {
        BidResponse response = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        assertEquals(TestConstants.cpm1, response.getCpm());
        assertEquals(ServerResponsesBuilder.ut_url, response.getCreative());
        assertEquals(5 * 60 * 1000, response.getExpiryTime());
        response.setBidderCode(TestConstants.BIDDER_NAME);
        assertEquals(TestConstants.BIDDER_NAME, response.getBidderCode());
    }

    @Test
    public void testAddCustomKeyword() throws Exception {
        BidResponse response = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        response.addCustomKeyword("test_key", "test_value");
        assertNotNull(response.getCustomKeywords());
        assertFalse(response.getCustomKeywords().isEmpty());
        assertEquals(1, response.getCustomKeywords().size());
        assertEquals("test_key", response.getCustomKeywords().get(0).first);
        assertEquals("test_value", response.getCustomKeywords().get(0).second);
    }

    @Test
    public void testExpiration() throws Exception {
        BidResponse response = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        assertFalse(response.isExpired());
        response.setExpiryTime(10);
        Lock.pause(15);
        assertTrue(response.isExpired());
    }
}
