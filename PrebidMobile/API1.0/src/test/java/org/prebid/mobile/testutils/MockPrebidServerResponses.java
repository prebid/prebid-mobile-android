/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile.testutils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MockPrebidServerResponses {
    public static String noBid() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerNoBidResponse.json");
        return inputStreamToString(in);
    }

    public static String noBidFromRubicon() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerNoBidFromRubiconResponse.json");
        return inputStreamToString(in);
    }

    public static String oneBidFromAppNexus() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerOneBidFromAppNexusResponse.json");
        return inputStreamToString(in);
    }

    public static String oneBidFromRubicon() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerOneBidFromRubiconResponse.json");
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

    public static String invalidBidRubiconResponseWithoutCacheId() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerInvalidBidRubiconResponseWithoutCacheId.json");
        return inputStreamToString(in);
    }

    public static String validResponseAppNexusNoCacheIdAndRubiconHasCacheId() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerValidResponseAppNexusNoCacheIdAndRunbiconHasCacheId.json");
        return inputStreamToString(in);
    }

    public static String invalidBidResponseTopBidNoCacheId() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerInvalidResponseTopBidDoesNotHaveCacheId.json");
        return inputStreamToString(in);
    }

    public static String validBidResponseTwoBidsOnTheSameSeat() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerValidBidResponseTwoBidsOnTheSameSeat.json");
        return inputStreamToString(in);
    }

    public static String noBidResponseNoTmax() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerNoBidNoTmax.json");
        return inputStreamToString(in);
    }
    public static String noBidResponseTmaxTooLarge() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerNoBidTmaxTooLarge.json");
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
