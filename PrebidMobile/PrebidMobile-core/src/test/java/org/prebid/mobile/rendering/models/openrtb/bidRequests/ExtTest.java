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

public class ExtTest {
    @Test
    public void setAndGetStringObject() {
        Ext ext = new Ext();

        ext.put("my_string_key", "my_string_value");
        JSONObject actualObj = ext.getJsonObject();
        String expectedString = "{\"my_string_key\":\"my_string_value\"}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        ext.getJsonObject();
    }

    @Test
    public void setAndGetIntegerObject() {
        Ext ext = new Ext();

        ext.put("my_integer_key", new Integer(1));
        String expectedString = "{\"my_integer_key\":1}";
        JSONObject actualObj = ext.getJsonObject();
        assertEquals("got: " + actualObj, expectedString, actualObj.toString());
        ext.getJsonObject();
    }

    @Test
    public void setAndGetJSONObject() throws Exception {
        Ext ext = new Ext();

        JSONObject myJSONObj = new JSONObject();
        myJSONObj.put("hello_key", "world_value");

        ext.put("my_object", myJSONObj);
        JSONObject actualObj = ext.getJsonObject();
        String expectedString = "{\"my_object\":{\"hello_key\":\"world_value\"}}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        ext.getJsonObject();
    }

    @Test
    public void setAndGetJSONArray() {
        Ext ext = new Ext();

        JSONArray myJSONArray = new JSONArray();
        myJSONArray.put("item1");
        myJSONArray.put("item2");

        ext.put("array_key", myJSONArray);
        JSONObject actualObj = ext.getJsonObject();
        String expectedString = "{\"array_key\":[\"item1\",\"item2\"]}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        ext.getJsonObject();
    }
}