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

package org.prebid.mobile.api.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.prebid.mobile.api.data.FetchDemandResult.*;

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