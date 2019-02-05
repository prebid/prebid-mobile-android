package org.prebid.mobile.testutils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MockPrebidServerResponses {
    public static String noBid() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerNoBidResponse.json");
        return inputStreamToString(in);
    }

    public static String oneBidFromAppNexus() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerOneBidFromAppNexusResponse.json");
        return inputStreamToString(in);
    }

    public static String oneBidFromAppNexusOneBidFromRubicon() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerOneBidFromAppNexusOneBidFromRubicon.json");
        return inputStreamToString(in);
    }

    public static String invalidBidResponseWithoutCacheId() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerInvalidBidResponseWithoutCacheId.json");
        return inputStreamToString(in);
    }

    public static String validResponseAppNexusNoCacheIdAndRubiconHasCacheId() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerValidResponseAppNexusNoCacheIdAndRunbiconHasCacheId.json");
        return inputStreamToString(in);
    }

    public static String invalidBidResponseTopBidNoCacheId(){
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerInvalidResponseTopBidDoesNotHaveCacheId.json");
        return inputStreamToString(in);
    }

    public static String inputStreamToString(InputStream is) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            is.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
