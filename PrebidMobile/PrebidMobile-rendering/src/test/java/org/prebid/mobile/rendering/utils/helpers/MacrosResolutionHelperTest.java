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

package org.prebid.mobile.rendering.utils.helpers;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.models.internal.MacrosModel;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MacrosResolutionHelperTest {
    private static final String MACROS_AUCTION_BID_ID = "\\$\\{AUCTION_BID_ID\\}"; // for test purpose

    private static final String CREATIVE = "<script>\n"
                                           + "  \tlet pbNativeTagData = {};\n"
                                           + "  \tpbNativeTagData.pubUrl = \"%%PATTERN:url%%\";\n"
                                           + "\tpbNativeTagData.targetingMap = %%PATTERN:TARGETINGMAP%%;\n"
                                           + "  // if not DFP, use these params\n"
                                           + "  pbNativeTagData.adId = \"%%PATTERN:hb_adid%%\";\n"
                                           + "  pbNativeTagData.cacheHost = \"%%PATTERN:hb_cache_host%%\";\n"
                                           + "  pbNativeTagData.cachePath = \"%%PATTERN:hb_cache_path%%\";\n"
                                           + "  pbNativeTagData.uuid = \"%%PATTERN:hb_cache_id%%\";\n"
                                           + "  pbNativeTagData.env = \"%%PATTERN:hb_env%%\";\n"
                                           + "  pbNativeTagData.hbPb = \"%%PATTERN:hb_pb%%\";\n"
                                           + "\n"
                                           + "  \twindow.pbNativeTag.startTrackers(pbNativeTagData);\n"
                                           + "</script>";

    private static final String CREATIVE_MULTI_PATTERN = "<script>\n"
                                                         + "  \tlet pbNativeTagData = {};\n"
                                                         + "  \t%%PATTERN:test_key%%%%PATTERN:test_key1%%\n"
                                                         + "  \tpbNativeTagData.pubUrl = \"%%PATTERN:url%%\";\n"
                                                         + "\tpbNativeTagData.targetingMap = %%PATTERN:TARGETINGMAP%%;\n"
                                                         + "  // if not DFP, use these params\n"
                                                         + "  pbNativeTagData.adId = \"%%PATTERN:hb_adid%%\";\n"
                                                         + "  pbNativeTagData.cacheHost = \"%%PATTERN:hb_cache_host%%\";\n"
                                                         + "  pbNativeTagData.cachePath = \"%%PATTERN:hb_cache_path%%\";\n"
                                                         + "  pbNativeTagData.cachePath = \"%%PATTERN:hb_cache_path%%\";\n"
                                                         + "  pbNativeTagData.uuid = \"%%PATTERN:hb_cache_id%%\";\n"
                                                         + "  pbNativeTagData.env = \"%%PATTERN:hb_env%%\";\n"
                                                         + "  pbNativeTagData.hbPb = \"%%PATTERN:hb_pb%%\";\n"
                                                         + "\n"
                                                         + "  \twindow.pbNativeTag.startTrackers(pbNativeTagData);\n"
                                                         + "</script>";

    @Test
    public void resolveMarcos_SingleMatch_LocatedMarcosIsReplaced() {
        String expected = "<script>\n"
                          + "  \tlet pbNativeTagData = {};\n"
                          + "  \tpbNativeTagData.pubUrl = null;\n"
                          + "\tpbNativeTagData.targetingMap = {\"hb_pb\":\"12.2\",\"hb_adid\":\"123456\",\"hb_env\":\"env\",\"hb_cache_id\":\"222\",\"hb_cache_host\":\"https://cache.url.com\",\"hb_cache_path\":\"cache/path\"};\n"
                          + "  // if not DFP, use these params\n"
                          + "  pbNativeTagData.adId = \"123456\";\n"
                          + "  pbNativeTagData.cacheHost = \"https://cache.url.com\";\n"
                          + "  pbNativeTagData.cachePath = \"cache/path\";\n"
                          + "  pbNativeTagData.uuid = \"222\";\n"
                          + "  pbNativeTagData.env = \"env\";\n"
                          + "  pbNativeTagData.hbPb = \"12.2\";\n"
                          + "\n"
                          + "  \twindow.pbNativeTag.startTrackers(pbNativeTagData);\n"
                          + "</script>";

        Map<String, String> targetingMap = new HashMap<>();
        targetingMap.put("hb_adid", "123456");
        targetingMap.put("hb_cache_host", "https://cache.url.com");
        targetingMap.put("hb_cache_path", "cache/path");
        targetingMap.put("hb_env", "env");
        targetingMap.put("hb_pb", "12.2");
        targetingMap.put("hb_cache_id", "222");

        String actual = MacrosResolutionHelper.resolveTargetingMarcos(CREATIVE, targetingMap);

        assertEquals(expected, actual);
    }

    @Test
    public void resolveMarcos_MultipleMatch_LocatedMarcosAreReplaced() {
        String expected = "<script>\n"
                          + "  \tlet pbNativeTagData = {};\n"
                          + "  \ttesttest1\n"
                          + "  \tpbNativeTagData.pubUrl = null;\n"
                          + "\tpbNativeTagData.targetingMap = {\"hb_pb\":\"12.2\",\"hb_env\":\"env\",\"hb_cache_path\":\"cache/path\",\"hb_adid\":\"123456\",\"test_key\":\"test\",\"hb_cache_host\":\"https://cache.url.com\",\"test_key1\":\"test1\"};\n"
                          + "  // if not DFP, use these params\n"
                          + "  pbNativeTagData.adId = \"123456\";\n"
                          + "  pbNativeTagData.cacheHost = \"https://cache.url.com\";\n"
                          + "  pbNativeTagData.cachePath = \"cache/path\";\n"
                          + "  pbNativeTagData.cachePath = \"cache/path\";\n"
                          + "  pbNativeTagData.uuid = null;\n"
                          + "  pbNativeTagData.env = \"env\";\n"
                          + "  pbNativeTagData.hbPb = \"12.2\";\n"
                          + "\n"
                          + "  \twindow.pbNativeTag.startTrackers(pbNativeTagData);\n"
                          + "</script>";

        Map<String, String> targetingMap = new HashMap<>();
        targetingMap.put("hb_adid", "123456");
        targetingMap.put("hb_cache_host", "https://cache.url.com");
        targetingMap.put("hb_cache_path", "cache/path");
        targetingMap.put("hb_env", "env");
        targetingMap.put("hb_pb", "12.2");
        targetingMap.put("test_key", "test");
        targetingMap.put("test_key1", "test1");

        String actual = MacrosResolutionHelper.resolveTargetingMarcos(CREATIVE_MULTI_PATTERN, targetingMap);

        assertEquals(expected, actual);
    }

    @Test
    public void resolveAuctionMacros_MultipleMatch_LocatedMacrosAreReplaced()
    throws IOException, JSONException {
        String actualResponseWithMacros = ResourceUtils.convertResourceToString("native_bid_response_macros.json");
        String expectedResponseWithMacros = ResourceUtils.convertResourceToString("native_bid_response_macros_expected.json");

        Map<String, MacrosModel> macrosModelMap = new HashMap<>();
        Bid bid = Bid.fromJSONObject(new JSONObject(actualResponseWithMacros));
        String id = bid.getId();
        String price = String.valueOf(bid.getPrice());
        String base64Price = Base64.getEncoder().encodeToString(price.getBytes());

        macrosModelMap.put(MACROS_AUCTION_BID_ID, new MacrosModel(id));
        macrosModelMap.put(MacrosModel.MACROS_AUCTION_PRICE, new MacrosModel(price));
        macrosModelMap.put(MacrosModel.MACROS_AUCTION_PRICE_BASE_64, new MacrosModel(base64Price));

        String macrosAfterResolution = MacrosResolutionHelper.resolveAuctionMacros(actualResponseWithMacros, macrosModelMap);

        assertEquals(expectedResponseWithMacros, macrosAfterResolution);
    }

    @Test
    public void resolveAuctionMacros_ReplaceValueIsNull_LocatedMacrosAreReplacedWithEmptyString()
    throws IOException {
        String actualResponseWithMacros = ResourceUtils.convertResourceToString("native_bid_response_macros.json");
        String expectedResponseWithMacros = ResourceUtils.convertResourceToString("native_bid_response_macros_expected_empty.json");

        Map<String, MacrosModel> macrosModelMap = new HashMap<>();

        macrosModelMap.put(MACROS_AUCTION_BID_ID, new MacrosModel(null));
        macrosModelMap.put(MacrosModel.MACROS_AUCTION_PRICE, new MacrosModel(null));
        macrosModelMap.put(MacrosModel.MACROS_AUCTION_PRICE_BASE_64, new MacrosModel(null));

        String macrosAfterResolution = MacrosResolutionHelper.resolveAuctionMacros(actualResponseWithMacros, macrosModelMap);

        assertEquals(expectedResponseWithMacros, macrosAfterResolution);
    }
}