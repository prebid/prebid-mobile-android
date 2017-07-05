package org.prebid.mobile.unittestutils;

import java.util.ArrayList;
import java.util.Locale;

public class ServerResponsesBuilder {
    private static final String base_response = "{\"version\":\"0.0.1\",\"tags\":[%s]}";
    private static final String successful_bid = "{\"uuid\":\"FAKE UUID\",\"auction_id\":\"123456789\",\"ad\":{\"banner\":{\"width\":%d,\"height\":%d,\"content\":\"FAKE CONTENT\"},\"ad_type\":\"banner\",\"buyer_member_id\":1,\"creative_id\":1,\"media_type_id\":1,\"media_subtype_id\":1,\"cpm\":%f,\"notify_url\":\"FAKE NOTIFY URL\"},\"ut_url\":\"%s\",\"tag_id\":%d}";
    private static final String nobid = "{\"uuid\":\"FAKE UUID\",\"nobid\":true,\"tag_id\":%d}";
    public static final String ut_url = "pass_me_to_prebid_enabled_networks";
    ArrayList<String> tags;

    public ServerResponsesBuilder() {
        tags = new ArrayList<String>();
    }

    public void addBannerResponse(int placementId, int width, int height, double cpm) {
        String tag = String.format(Locale.ENGLISH, successful_bid, width, height, cpm, ut_url, placementId);
        tags.add(tag);
    }

    public void addNoBid(int placementId) {
        String tag = String.format(Locale.ENGLISH, nobid, placementId);
        tags.add(tag);
    }

    public String toServerResponse() {
        StringBuilder sb = new StringBuilder();
        int len = tags.size();
        for (int i = 0; i < len - 1; i++) {
            sb.append(tags.get(i)).append(",");
        }
        sb.append(tags.get(len - 1));
        return String.format(base_response, sb.toString());
    }
}
