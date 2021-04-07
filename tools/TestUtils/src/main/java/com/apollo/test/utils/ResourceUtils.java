package com.apollo.test.utils;

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
