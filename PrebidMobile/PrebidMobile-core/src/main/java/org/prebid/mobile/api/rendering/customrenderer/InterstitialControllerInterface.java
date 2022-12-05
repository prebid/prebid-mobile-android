package org.prebid.mobile.api.rendering.customrenderer;

import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

public interface InterstitialControllerInterface {

    void loadAd(AdUnitConfiguration adUnitConfiguration, BidResponse bidResponse);

    void show();

    void destroy();
}
