package com.openx.apollo.networking.urlBuilder;

import com.openx.apollo.sdk.ApolloSettings;

public class BidPathBuilder extends PathBuilderBase {

    @Override
    public String buildURLPath(String domain) {
        return ApolloSettings.getBidServerHost();
    }
}
