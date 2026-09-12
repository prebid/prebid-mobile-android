/*
 *    Copyright 2018-2026 Prebid.org, Inc.
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

package org.prebid.mobile.api.mediation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.PrebidBidSelecting;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class MediationNativeAdUnitTest {

    @Test
    public void setBidSelector_forwardsToAdUnitConfig() {
        MediationNativeAdUnit adUnit = new MediationNativeAdUnit("config-id", new Object());
        PrebidBidSelecting selector = new PrebidBidSelecting() {
            @Override
            public Bid selectBid(List<Bid> bids) {
                return bids.isEmpty() ? null : bids.get(0);
            }
        };

        adUnit.setBidSelector(selector);

        assertEquals(selector, adUnit.getBidSelector());
    }

}
