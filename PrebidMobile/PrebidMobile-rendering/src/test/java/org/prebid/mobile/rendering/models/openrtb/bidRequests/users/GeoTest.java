package org.prebid.mobile.rendering.models.openrtb.bidRequests.users;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import static org.junit.Assert.assertEquals;

public class GeoTest {

    public final static String EXPECTED_GEO = "geo_expected.txt";

    @Test
    public void testGetJsonObject() throws Exception {

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

        JSONObject actualObj = geo.getJsonObject();
        String expectedString = ResourceUtils.convertResourceToString(EXPECTED_GEO);
        assertEquals(expectedString, actualObj.toString());
    }

}