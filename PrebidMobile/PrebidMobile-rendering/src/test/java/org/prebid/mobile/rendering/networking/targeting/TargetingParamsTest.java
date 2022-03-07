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

import android.util.Pair;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.networking.parameters.UserParameters;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TargetingParamsTest {

    @Test
    public void setUserAge_CalculateYobAndSetAgeParameter() {
        final int age = 20;
        Integer expectedYob = Calendar.getInstance().get(Calendar.YEAR) - age;

        TargetingParams.setUserAge(age);

        assertEquals(String.valueOf(age), TargetingParams.getTargetingMap().get(TargetingParams.KEY_AGE));
        assertEquals(expectedYob, TargetingParams.getYearOfBirth());
    }

    @Test
    public void setUserKeywords_EqualToGetUserKeywords() {
        final String expectedKeywords = "keyworkds";
        TargetingParams.setUserKeywords(expectedKeywords);

        assertEquals(expectedKeywords, TargetingParams.getUserKeywordsSet());
    }

    @Test
    public void setUserCustomData_EqualToGetUserCustomData() {
        final String expectedCustomData = "custom_data";
        TargetingParams.setUserCustomData(expectedCustomData);

        assertEquals(expectedCustomData, TargetingParams.getUserCustomData());
    }

    @Test
    public void setUserGender_EqualToGetUserGenderAndIsInRequestParams() {
        final String expected = UserParameters.GENDER_FEMALE;

        TargetingParams.setGender(UserParameters.Gender.FEMALE);

        assertEquals(expected, TargetingParams.getGender());
        assertEquals(expected, TargetingParams.getTargetingMap().get(TargetingParams.KEY_GENDER));
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
        assertEquals(expected, TargetingParams.getTargetingMap().get(TargetingParams.KEY_APP_STORE_URL));
    }

    @Test
    public void setUserExt_EqualToGetUserExt() {
        final Ext expected = new Ext();
        expected.put("external", "value");

        TargetingParams.setUserExt(expected);

        assertEquals(expected, TargetingParams.getUserExt());
    }

    @Test
    public void setUserId_EqualToGetUserIdAndIsInRequestParams() {
        final String expected = "123";

        TargetingParams.setUserId(expected);

        assertEquals(expected, TargetingParams.getUserId());
        assertEquals(expected, TargetingParams.getTargetingMap().get(TargetingParams.KEY_USER_ID));
    }

    @Test
    public void setBuyerUid_EqualToGetBuyerUid() {
        final String expected = "12345";

        TargetingParams.setBuyerId(expected);

        assertEquals(expected, TargetingParams.getBuyerId());
    }

    @Test
    public void setPublisherName_EqualToGetPublisherName() {
        final String expected = "prebid";

        TargetingParams.setPublisherName(expected);

        assertEquals(expected, TargetingParams.getPublisherName());
    }

    @After
    public void cleanup() {
        TargetingParams.clear();

        assertNull(TargetingParams.getStoreUrl());
        assertNull(TargetingParams.getBuyerId());
        assertNull(TargetingParams.getPublisherName());

        assertNull(TargetingParams.getUserAge());
        assertNull(TargetingParams.getUserCustomData());
        assertNull(TargetingParams.getUserExt());
        assertNull(TargetingParams.getUserId());
        assertNull(TargetingParams.getGender());
        assertNull(TargetingParams.getUserLatLng());

        assertTrue(TargetingParams.getTargetingMap().isEmpty());
    }
}
