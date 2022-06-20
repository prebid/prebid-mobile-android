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

package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;

import static org.junit.Assert.*;

public class DeviceTest {

    private Device testDevice;

    @Before
    public void setUp() {
        testDevice = new Device();
    }

    @Test
    public void getJsonObjectTest() throws Exception {
        Geo testGeo = new Geo();

        testDevice.setGeo(testGeo);
        assertEquals(testGeo, testDevice.getGeo());

        testDevice.lmt = 1;
        testDevice.devicetype = 1;
        testDevice.make = "LG";
        testDevice.model = "Nexus 5";
        testDevice.os = "5.0";
        testDevice.osv = "5";
        testDevice.hwv = "5";
        testDevice.flashver = "1";
        testDevice.language = "en";
        testDevice.carrier = "T-mobile";
        testDevice.mccmnc = "321-444";
        testDevice.ifa = "1111";
        testDevice.didsha1 = "3414dsfd";
        testDevice.didmd5 = "didmd5";
        testDevice.dpidsha1 = "didsha1";
        testDevice.dpidmd5 = "dpidmd5";
        testDevice.h = 1221;
        testDevice.w = 567;
        testDevice.ppi = 11;
        testDevice.js = 1;
        testDevice.connectiontype = 1;
        testDevice.pxratio = 22f;
        testDevice.geo = testGeo;
        JSONObject actualObj = testDevice.getJsonObject();
        String expectedString = "{\"os\":\"5.0\",\"didmd5\":\"didmd5\",\"ifa\":\"1111\",\"hwv\":\"5\",\"h\":1221,\"ppi\":11,\"js\":1,\"language\":\"en\",\"devicetype\":1,\"pxratio\":22,\"geo\":{},\"lmt\":1,\"carrier\":\"T-mobile\",\"osv\":\"5\",\"dpidmd5\":\"dpidmd5\",\"mccmnc\":\"321-444\",\"flashver\":\"1\",\"didsha1\":\"3414dsfd\",\"w\":567,\"model\":\"Nexus 5\",\"connectiontype\":1,\"make\":\"LG\",\"dpidsha1\":\"didsha1\"}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        testDevice.getJsonObject();
    }

    @Test
    public void checkGeoTest() {
        Geo actualGeo = testDevice.getGeo();
        assertNotNull("Geo isn't null.", actualGeo);

        Geo geo = new Geo();
        geo.lat = 35.012345f;
        geo.lon = -115.12345f;
        geo.type = 1;
        geo.accuracy = 1;
        geo.lastfix = 1;
        geo.country = "USA";
        geo.region = "CA";
        geo.regionfips104 = "USA";
        geo.metro = "803";
        geo.city = "Los Angeles";
        geo.zip = "90049";
        geo.utcoffset = 1;
        testDevice.setGeo(geo);
        assertSame("Set and received geo values must match.", geo, testDevice.getGeo());

    }
}