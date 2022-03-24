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

package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.bidding.config.MockMediationUtils;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.test.utils.WhiteBox;
import org.prebid.mobile.units.configuration.AdFormat;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationRewardedVideoAdUnitTest {

    private Context mContext;
    private MediationRewardedVideoAdUnit mMopubRewardedAdUnit;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        PrebidMobile.setPrebidServerAccountId("id");
        mMopubRewardedAdUnit = new MediationRewardedVideoAdUnit(mContext, "config", new MockMediationUtils());
        WhiteBox.setInternalState(mMopubRewardedAdUnit, "mBidLoader", mock(BidLoader.class));
    }

    @After
    public void cleanup() {
        PrebidMobile.setPrebidServerAccountId(null);
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForInterstitial() {
        mMopubRewardedAdUnit.initAdConfig("config", null);
        AdUnitConfiguration adConfiguration = mMopubRewardedAdUnit.mAdUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(Collections.singletonList(AdFormat.VAST), adConfiguration.getAdFormats());
        assertTrue(adConfiguration.isRewarded());
    }

}