package org.prebid.mobile.app;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;

public class Util {
    public static void enableAdditionalFunctionality(AdUnit adUnit) {
        enableCOPPA();
        addFirstPartyData(adUnit);
        setStoredResponse();
        setRequestTimeoutMillis();
        enablePbsDebug();
    }

    private static void enableCOPPA() {
        TargetingParams.setSubjectToCOPPA(true);
    }

    private static void addFirstPartyData(AdUnit adUnit) {
        //Access Control List
        TargetingParams.addBidderToAccessControlList(TargetingParams.BIDDER_NAME_RUBICON_PROJECT);

        //PBAdSlot(should be set together with Access Control List)
        adUnit.setPbAdSlot("/1111111/homepage/med-rect-2");

        //global user data
//        TargetingParams.addUserData("globalUserDataKey1", "globalUserDataValue1");

        //global context data
        TargetingParams.addContextData("globalContextDataKey1", "globalContextDataValue1");

        //adunit context data
        adUnit.addContextData("adunitContextDataKey1", "adunitContextDataValue1");

        //global context keywords
        TargetingParams.addContextKeyword("globalContextKeywordValue1");
        TargetingParams.addContextKeyword("globalContextKeywordValue2");

        //global user keywords
        TargetingParams.addUserKeyword("globalUserKeywordValue1");
        TargetingParams.addUserKeyword("globalUserKeywordValue2");

        //adunit context keywords
        adUnit.addContextKeyword("adunitContextKeywordValue1");
        adUnit.addContextKeyword("adunitContextKeywordValue2");

    }

    private static void setStoredResponse() {
        PrebidMobile.setStoredAuctionResponse("111122223333");
    }

    private static void setRequestTimeoutMillis() {
        PrebidMobile.setTimeoutMillis(5_000);
    }

    private static void enablePbsDebug() {
        PrebidMobile.setPbsDebug(true);
    }
}
