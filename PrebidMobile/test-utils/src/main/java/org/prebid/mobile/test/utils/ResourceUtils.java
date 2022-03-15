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

package org.prebid.mobile.test.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {
    public final static String USER_AGENT_HEADER = "User-Agent";
    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    public static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    public final static String ACCEPT_HEADER = "Accept";
    public final static String ACCEPT_HEADER_VALUE = "application/x-www-form-urlencoded,text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    public static String convertResourceToString(String fileName) throws IOException {
        InputStream fileStream = ResourceUtils.class.getClassLoader().getResourceAsStream(fileName);

        java.util.Scanner s = new java.util.Scanner(fileStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void assertJsonEquals(JSONObject expected, JSONObject actual) {
        JsonElement expectedJsonElement = JsonParser.parseString(expected.toString());
        JsonElement actualJsonElement = JsonParser.parseString(actual.toString());

        Assert.assertEquals(expectedJsonElement, actualJsonElement);
    }
}
