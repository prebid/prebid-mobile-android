package org.prebid.mobile.mopub.mock;

import java.util.HashMap;

public class TestResponse {

    public static String getResponse() {
        return "{\n" +
                "  \"id\": \"id\",\n" +
                "  \"seatbid\": [\n" +
                "    {\n" +
                "      \"bid\": [\n" +
                "        {\n" +
                "          \"id\": \"bidId\",\n" +
                "          \"impid\": \"impId\",\n" +
                "          \"price\": 0.15,\n" +
                "          \"adm\": \"adm\",\n" +
                "          \"crid\": \"crid\",\n" +
                "          \"w\": 320,\n" +
                "          \"h\": 50,\n" +
                "          \"ext\": {\n" +
                "            \"prebid\": {\n" +
                "              \"cache\": {\n" +
                "                \"key\": \"cacheKey\",\n" +
                "                \"url\": \"cacheUrl\",\n" +
                "                \"bids\": {\n" +
                "                  \"url\": \"bidsUrl\",\n" +
                "                  \"cacheId\": \"bidsCacheId\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"targeting\": {\n" +
                "                \"hb_pb\": \"value1\",\n" +
                "                \"hb_bidder\": \"value2\",\n" +
                "                \"hb_cache_id\": \"value3\"\n" +
                "              },\n" +
                "              \"type\": \"type\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"seat\": \"prebid\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"cur\": \"USD\",\n" +
                "  \"bidid\": \"bidid\",\n" +
                "  \"customdata\": \"custom\",\n" +
                "  \"nbr\": 1,\n" +
                "  \"ext\": {\n" +
                "    \"responsetimemillis\": {\n" +
                "      \"prebid\": 63\n" +
                "    },\n" +
                "    \"tmaxrequest\": 3000\n" +
                "  }\n" +
                "}";
    }

    public static HashMap<String, String> getKeywordsMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("hb_pb", "value1");
        result.put("hb_bidder", "value2");
        result.put("hb_cache_id", "value3");
        return result;
    }

    public static String getKeywordsString() {
        return "hb_pb:value1,hb_bidder:value2,hb_cache_id:value3,";
    }

}
