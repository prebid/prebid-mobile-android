package org.prebid.mobile.unittestutils;

import java.util.UUID;

public class TestConstants {
    public static final String bannerAdUnit1 = "Bannerxyz123";
    public static final String bannerAdUnit2 = "Banner123xyz";
    public static final String interstitialAdUnit = "interstitial123xyz";
    public static final int APNPlacementId_1 = 1111;
    public static final int APNPlacementId_2 = 2222;
    public static final int APNPlacementId_3 = 3333;
    public static final String configID1 = "138c4d03-0efb-4498-9dc6-cb5a9acb2ea4";
    public static final String configID2 = "0c286d00-b3ee-4550-b15d-f71f8e746865";
    public static final String configID3 = "35f1d17d-c99a-4d55-800e-062b80750d65";
    public static final String accountId = "bfa84af2-bd16-4d35-96ad-31c6bb888df0";
    public static final int width1 = 320;
    public static final int height1 = 50;
    public static final int width2 = 300;
    public static final int height2 = 250;

    public static final double cpm1 = 1.37;
    public static final double cpm2 = 0.51;
    public static final double cpm3 = 0.54;

    public static final String BIDDER_NAME = "MockBidder";

    private static String fake_auction_id;

    static {
        fake_auction_id = UUID.randomUUID().toString();
    }

    public static String getFakeAuctionID() {
        return fake_auction_id;
    }

    public static void updateFakeAuctionID() {
        fake_auction_id = UUID.randomUUID().toString();
    }
}
