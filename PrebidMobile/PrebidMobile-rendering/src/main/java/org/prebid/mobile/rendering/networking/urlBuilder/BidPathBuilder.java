package org.prebid.mobile.rendering.networking.urlBuilder;

import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;

public class BidPathBuilder extends PathBuilderBase {

    @Override
    public String buildURLPath(String domain) {
        return PrebidRenderingSettings.getBidServerHost().getHostUrl();
    }
}
