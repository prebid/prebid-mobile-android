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

import static org.junit.Assert.assertEquals;

public class RegsTest {
    @Test
    public void getJsonObject() throws Exception {
        Regs regs = new Regs();

        regs.coppa = 1;

        JSONObject actualObj = regs.getJsonObject();
        String expectedString = "{\"coppa\":1}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        regs.getJsonObject();
    }

    @Test
    public void emptyExtObjectShouldRender() throws Exception {
        Regs regs = new Regs();

        regs.coppa = 1;

        regs.getExt();

        JSONObject actualObj = regs.getJsonObject();
        String expectedString = "{\"ext\":{},\"coppa\":1}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        regs.getJsonObject();
    }

    @Test
    public void populatedExtObjectShouldRender() throws Exception {
        Regs regs = new Regs();
        regs.coppa = 1;

        //Add a JSON Array
        JSONArray myJSONArray = new JSONArray();
        myJSONArray.put("item1");
        myJSONArray.put("item2");

        regs.getExt().put("array_key", myJSONArray);

        //Add a JSON Object
        JSONObject myJSONObj = new JSONObject();
        myJSONObj.put("hello_key", "world_value");

        regs.getExt().put("my_object", myJSONObj);

        //Add a JSON String
        regs.getExt().put("my_string_key", "my_string_value");

        //Add another JSON Object
        JSONObject secondJSONObj = new JSONObject();
        secondJSONObj.put("second_key", "second_value");

        regs.getExt().put("second_object", secondJSONObj);
        JSONObject actualObj = regs.getJsonObject();

        /*
         *  It appears that the JSON "toString()" function may have a particular order of precedence.
         *  No matter what order you put objects in the Ext object, when you finally call "toString()" function,
         *  the serialized string is ordered by these types: JSONObject, String, JSONArray.  JSONObjects specifically go first,
         *  in the order they were added.  Very Interesting.
         */
        String expectedString = "{\"ext\":{\"my_object\":{\"hello_key\":\"world_value\"},\"second_object\":{\"second_key\":\"second_value\"},\"my_string_key\":\"my_string_value\",\"array_key\":[\"item1\",\"item2\"]},\"coppa\":1}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        regs.getJsonObject();
    }
}