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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;

public class DeviceTest {

    private Device mTestDevice;

    @Before
    public void setUp() {
        mTestDevice = new Device();
    }

    @Test
    public void getJsonObjectTest() throws Exception {
        Geo testGeo = new Geo();

        mTestDevice.setGeo(testGeo);
        assertEquals(testGeo, mTestDevice.getGeo());

        mTestDevice.lmt = 1;
        mTestDevice.devicetype = 1;
        mTestDevice.make = "LG";
        mTestDevice.model = "Nexus 5";
        mTestDevice.os = "5.0";
        mTestDevice.osv = "5";
        mTestDevice.hwv = "5";
        mTestDevice.flashver = "1";
        mTestDevice.language = "en";
        mTestDevice.carrier = "T-mobile";
        mTestDevice.mccmnc = "321-444";
        mTestDevice.ifa = "1111";
        mTestDevice.didsha1 = "3414dsfd";
        mTestDevice.didmd5 = "didmd5";
        mTestDevice.dpidsha1 = "didsha1";
        mTestDevice.dpidmd5 = "dpidmd5";
        mTestDevice.h = 1221;
        mTestDevice.w = 567;
        mTestDevice.ppi = 11;
        mTestDevice.js = 1;
        mTestDevice.connectiontype = 1;
        mTestDevice.pxratio = 22f;
        mTestDevice.geo = testGeo;
        JSONObject actualObj = mTestDevice.getJsonObject();
        String expectedString = "{\"os\":\"5.0\",\"didmd5\":\"didmd5\",\"ifa\":\"1111\",\"hwv\":\"5\",\"h\":1221,\"ppi\":11,\"js\":1,\"language\":\"en\",\"devicetype\":1,\"pxratio\":22,\"geo\":{},\"lmt\":1,\"carrier\":\"T-mobile\",\"osv\":\"5\",\"dpidmd5\":\"dpidmd5\",\"mccmnc\":\"321-444\",\"flashver\":\"1\",\"didsha1\":\"3414dsfd\",\"w\":567,\"model\":\"Nexus 5\",\"connectiontype\":1,\"make\":\"LG\",\"dpidsha1\":\"didsha1\"}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        mTestDevice.getJsonObject();
    }

    @Test
    public void checkGeoTest() {
        Geo actualGeo = mTestDevice.getGeo();
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
        mTestDevice.setGeo(geo);
        assertSame("Set and received geo values must match.", geo, mTestDevice.getGeo());

    }
}