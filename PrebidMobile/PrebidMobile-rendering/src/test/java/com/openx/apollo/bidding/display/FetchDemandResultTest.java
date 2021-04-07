package com.openx.apollo.bidding.display;

import com.openx.apollo.bidding.data.FetchDemandResult;

import org.junit.Test;

import static com.openx.apollo.bidding.data.FetchDemandResult.INVALID_ACCOUNT_ID;
import static com.openx.apollo.bidding.data.FetchDemandResult.INVALID_CONFIG_ID;
import static com.openx.apollo.bidding.data.FetchDemandResult.INVALID_SIZE;
import static com.openx.apollo.bidding.data.FetchDemandResult.NETWORK_ERROR;
import static com.openx.apollo.bidding.data.FetchDemandResult.NO_BIDS;
import static com.openx.apollo.bidding.data.FetchDemandResult.SERVER_ERROR;
import static com.openx.apollo.bidding.data.FetchDemandResult.TIMEOUT;
import static org.junit.Assert.assertEquals;

public class FetchDemandResultTest {

    @Test
    public void whenParseErrorMessageAndNoBidsMsg_NoBidsResult() {
        assertEquals(NO_BIDS, FetchDemandResult.parseErrorMessage("No bids"));
    }

    @Test
    public void whenParseErrorMessageAndTimeoutsMsg_TimeoutResult() {
        assertEquals(TIMEOUT, FetchDemandResult.parseErrorMessage("Creative factory Timeout"));
    }

    @Test
    public void whenParseErrorMessageAndNetworkErrorMsg_NetworkErrorResult() {
        assertEquals(NETWORK_ERROR, FetchDemandResult.parseErrorMessage("Network Error"));
    }

    @Test
    public void whenParseErrorMessageAndInvalidAccountMsg_InvalidAccountIdResult() {
        assertEquals(INVALID_ACCOUNT_ID, FetchDemandResult.parseErrorMessage("Invalid request: Stored Request with ID=\"0689a263-318d-448b-a3d4-b02e8a709d9da\" not found."));
    }

    @Test
    public void whenParseErrorMessageAndInvalidConfigMsg_InvalidConfigIdResult() {
        assertEquals(INVALID_CONFIG_ID, FetchDemandResult.parseErrorMessage("Invalid request: Stored Imp with ID=\"69cdbe88-1c3d-43a4-8770-72f30ccfb5c9a\" not found."));
    }

    @Test
    public void whenParseErrorMessageAndInvalidSizeMsg_InvalidSizeResult() {
        assertEquals(INVALID_SIZE, FetchDemandResult.parseErrorMessage("Invalid request: Request imp[0].banner.format[0] must define non-zero \"h\" and \"w\" properties."));
    }

    @Test
    public void whenParseErrorMessageAndNotDefinedMsg_PrebidServerErrorResult() {
        assertEquals(SERVER_ERROR, FetchDemandResult.parseErrorMessage(""));
    }
}