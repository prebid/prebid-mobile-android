package com.openx.apollo.networking.targeting;

import android.util.Pair;

import com.openx.apollo.models.openrtb.bidRequests.Ext;
import com.openx.apollo.networking.parameters.UserParameters;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TargetingTest {

    @Test
    public void setUserAge_CalculateYobAndSetAgeParameter() {
        final int age = 20;
        Integer expectedYob = Calendar.getInstance().get(Calendar.YEAR) - age;

        Targeting.setUserAge(age);

        assertEquals(String.valueOf(age), Targeting.getTargetingMap().get(Targeting.KEY_AGE));
        assertEquals(expectedYob, Targeting.getUserYob());
    }

    @Test
    public void setUserKeywords_EqualToGetUserKeywords() {
        final String expectedKeywords = "keyworkds";
        Targeting.setUserKeywords(expectedKeywords);

        assertEquals(expectedKeywords, Targeting.getUserKeyWords());
    }

    @Test
    public void setUserCustomData_EqualToGetUserCustomData() {
        final String expectedCustomData = "custom_data";
        Targeting.setUserCustomData(expectedCustomData);

        assertEquals(expectedCustomData, Targeting.getUserCustomData());
    }

    @Test
    public void setUserGender_EqualToGetUserGenderAndIsInRequestParams() {
        final String expected = UserParameters.GENDER_FEMALE;

        Targeting.setUserGender(UserParameters.OXMGender.FEMALE);

        assertEquals(expected, Targeting.getUserGender());
        assertEquals(expected, Targeting.getTargetingMap().get(Targeting.KEY_GENDER));
    }

    @Test
    public void setUserLatLng_EqualsToGetLatLng() {
        float latitude = 11f;
        float longitude = 14f;
        Pair<Float, Float> expectedLatLng = new Pair<>(latitude, longitude);

        Targeting.setUserLatLng(latitude, longitude);

        assertEquals(expectedLatLng, Targeting.getUserLatLng());
    }

    @Test
    public void setCarrier_EqualToGetCarrierAndIsInRequestParams() {
        final String expected = "network_carrier";

        Targeting.setCarrier(expected);

        assertEquals(expected, Targeting.getCarrier());
        assertEquals(expected, Targeting.getTargetingMap().get(Targeting.KEY_CARRIER));
    }

    @Test
    public void setDeviceIpAddress_EqualToGetDeviceIpAddressAndIsInRequestParams() {
        final String expected = "192.168.0.1";

        Targeting.setDeviceIpAddress(expected);

        assertEquals(expected, Targeting.getDeviceIpAddress());
        assertEquals(expected, Targeting.getTargetingMap().get(Targeting.KEY_IP_ADDRESS));
    }

    @Test
    public void setAppStoreMarketUrl_EqualToGetAppStoreMarketUrlAndIsInRequestParams() {
        final String expected = "https://google.play.com";

        Targeting.setAppStoreMarketUrl(expected);

        assertEquals(expected, Targeting.getAppStoreMarketUrl());
        assertEquals(expected, Targeting.getTargetingMap().get(Targeting.KEY_APP_STORE_URL));
    }

    @Test
    public void setUserExt_EqualToGetUserExt() {
        final Ext expected = new Ext();
        expected.put("external", "value");

        Targeting.setUserExt(expected);

        assertEquals(expected, Targeting.getUserExt());
    }

    @Test
    public void setUserId_EqualToGetUserIdAndIsInRequestParams() {
        final String expected = "123";

        Targeting.setUserId(expected);

        assertEquals(expected, Targeting.getUserId());
        assertEquals(expected, Targeting.getTargetingMap().get(Targeting.KEY_USER_ID));
    }

    @Test
    public void setBuyerUid_EqualToGetBuyerUid() {
        final String expected = "12345";

        Targeting.setBuyerUid(expected);

        assertEquals(expected, Targeting.getBuyerUid());
    }

    @Test
    public void setPublisherName_EqualToGetPublisherName() {
        final String expected = "openx";

        Targeting.setPublisherName(expected);

        assertEquals(expected, Targeting.getPublisherName());
    }

    @After
    public void cleanup() {
        Targeting.clear();

        assertNull(Targeting.getAppStoreMarketUrl());
        assertNull(Targeting.getBuyerUid());
        assertNull(Targeting.getCarrier());
        assertNull(Targeting.getDeviceIpAddress());
        assertNull(Targeting.getPublisherName());

        assertNull(Targeting.getUserAge());
        assertNull(Targeting.getUserCustomData());
        assertNull(Targeting.getUserExt());
        assertNull(Targeting.getUserId());
        assertNull(Targeting.getUserGender());
        assertNull(Targeting.getUserLatLng());

        assertTrue(Targeting.getTargetingMap().isEmpty());
    }
}
