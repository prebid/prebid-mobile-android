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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;

import static org.junit.Assert.assertEquals;

public class UserTest {
    @Test
    public void getJsonObject() throws Exception {
        User user = new User();

        user.yob = 2016;
        user.gender = "M";
        user.geo = new Geo();
        user.geo.accuracy = 1;
        user.keywords = "q, a";

        JSONObject actualObj = user.getJsonObject();
        String expectedString = "{\"geo\":{\"accuracy\":1},\"gender\":\"M\",\"keywords\":\"q, a\",\"yob\":2016}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        user.getJsonObject();
    }

    @Test
    public void setAndGetExt() throws Exception {
        User user = new User();

        //Add a JSON Array
        JSONArray myJSONArray = new JSONArray();
        myJSONArray.put("item1");
        myJSONArray.put("item2");

        user.getExt().put("array_key", myJSONArray);

        //Add a JSON Object
        JSONObject myJSONObj = new JSONObject();
        myJSONObj.put("hello_key", "world_value");

        user.getExt().put("my_object", myJSONObj);

        //Add a JSON String
        user.getExt().put("ox_consent_format", "my_string_value");

        //Add another JSON Object
        JSONObject secondJSONObj = new JSONObject();
        secondJSONObj.put("second_key", "second_value");

        user.getExt().put("second_object", secondJSONObj);
        JSONObject actualObj = user.getJsonObject();

        /*
         *  It appears that the JSON "toString()" function may have a particular order of precedence.
         *  No matter what order you put objects in the Ext object, when you finally call "toString()" function,
         *  the serialized string is ordered by these types: JSONObject, String, JSONArray.  JSONObjects specifically go first,
         *  in the order they were added.  Very Interesting.
         */
        String expectedString = "{\"ext\":{\"my_object\":{\"hello_key\":\"world_value\"},\"ox_consent_format\":\"my_string_value\",\"second_object\":{\"second_key\":\"second_value\"},\"array_key\":[\"item1\",\"item2\"]}}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        user.getJsonObject();
    }

    @Test
    public void geoTest() throws Exception {
        // Get default empty geo
        User user = new User();
        assertEquals(new Geo().getJsonObject().toString(), user.getGeo().getJsonObject().toString());

        // Set geo
        Geo geo = new Geo();
        geo.lat = 1.2f;
        geo.lon = 3.4f;
        user.setGeo(geo);
        assertEquals(geo, user.getGeo());

        // Unset geo
        user.setGeo(null);
        assertEquals(new Geo().getJsonObject().toString(), user.getGeo().getJsonObject().toString());
    }
}