package org.prebid.mobile.unittestutils;

import java.util.Locale;

public class ServerResponses {

    private static final String base_response = "{\"version\":\"0.0.1\",\"tags\":[%s]}";
    private static final String successful_bid = "{\"uuid\":\"FAKE UUID\",\"auction_id\":\"123456789\",\"ad\":{\"banner\":{\"width\":%d,\"height\":%d,\"content\":\"FAKE CONTENT\"},\"ad_type\":\"banner\",\"buyer_member_id\":1,\"creative_id\":1,\"media_type_id\":1,\"media_subtype_id\":1,\"cpm\":%f,\"notify_url\":\"FAKE NOTIFY URL\"},\"ut_url\":\"%s\",\"tag_id\":%d}";
    private static final String nobid = "{\"uuid\":\"FAKE UUID\",\"nobid\":true,\"tag_id\":%d}";
    public static final String ut_url = "pass_me_to_prebid_enabled_networks";

    public static String getSingleBidForCPM(double cpm) {
        return getSingleBid(TestConstants.APNPlacementId_1, TestConstants.width1, TestConstants.height1, cpm);
    }

    public static String getSingleBid(int placementID, int width, int height, double cpm) {
        String successfulTag = String.format(Locale.ENGLISH, successful_bid, width, height, cpm, ut_url, placementID);
        return String.format(Locale.ENGLISH, base_response, successfulTag);
    }

    public static String getMultipleBids() {
        String tag1 = String.format(Locale.ENGLISH, successful_bid, TestConstants.width1, TestConstants.height1, TestConstants.cpm1, ut_url, TestConstants.APNPlacementId_1);
        String tag2 = String.format(Locale.ENGLISH, successful_bid, TestConstants.width1, TestConstants.height1, TestConstants.cpm1, ut_url, TestConstants.APNPlacementId_1);
        return String.format(base_response, tag1 + "," + tag2);
    }

    public static String getNoBid() {
        String tag1 = String.format(Locale.ENGLISH, nobid, TestConstants.APNPlacementId_1);
        String tag2 = String.format(Locale.ENGLISH, nobid, TestConstants.APNPlacementId_2);
        return String.format(base_response, tag1 + "," + tag2);
    }

}

