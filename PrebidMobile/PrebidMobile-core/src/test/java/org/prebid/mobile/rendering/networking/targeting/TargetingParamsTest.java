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

package org.prebid.mobile.rendering.networking.targeting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.util.Pair;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TargetingParamsTest {

    @Test
    public void setUserKeywords_EqualToGetUserKeywords() {
        final String expectedKeywords = "keyworkds";
        TargetingParams.addUserKeyword(expectedKeywords);

        assertEquals(expectedKeywords, TargetingParams.getUserKeywords());
    }

    @Test
    public void setUserLatLng_EqualsToGetLatLng() {
        float latitude = 11f;
        float longitude = 14f;
        Pair<Float, Float> expectedLatLng = new Pair<>(latitude, longitude);

        TargetingParams.setUserLatLng(latitude, longitude);

        assertEquals(expectedLatLng, TargetingParams.getUserLatLng());
    }

    @Test
    public void setAppStoreMarketUrl_EqualToGetAppStoreMarketUrlAndIsInRequestParams() {
        final String expected = "https://google.play.com";

        TargetingParams.setStoreUrl(expected);

        assertEquals(expected, TargetingParams.getStoreUrl());
    }

    @Test
    public void setUserExt_EqualToGetUserExt() {
        final Ext expected = new Ext();
        expected.put("external", "value");

        TargetingParams.setUserExt(expected);

        assertEquals(expected, TargetingParams.getUserExt());
    }


    @Test
    public void setPublisherName_EqualToGetPublisherName() {
        final String expected = "prebid";

        TargetingParams.setPublisherName(expected);

        assertEquals(expected, TargetingParams.getPublisherName());
    }

    @After
    public void cleanup() {
        TargetingParams.setStoreUrl(null);
        TargetingParams.setPublisherName(null);
        TargetingParams.setUserExt(null);
        TargetingParams.setUserLatLng(null, null);

        assertNull(TargetingParams.getStoreUrl());
        assertNull(TargetingParams.getPublisherName());
        assertNull(TargetingParams.getUserExt());
        assertNull(TargetingParams.getUserLatLng());
    }
}
