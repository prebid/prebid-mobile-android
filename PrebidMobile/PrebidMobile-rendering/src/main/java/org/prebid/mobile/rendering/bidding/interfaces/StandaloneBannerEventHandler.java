package org.prebid.mobile.rendering.bidding.interfaces;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;

public class StandaloneBannerEventHandler implements BannerEventHandler {
    private BannerEventListener mBannerViewListener;

    @Override
    public AdSize[] getAdSizeArray() {
        return new AdSize[0];
    }

    @Override
    public void setBannerEventListener(BannerEventListener bannerViewListener) {
        mBannerViewListener = bannerViewListener;
    }

    @Override
    public void requestAdWithBid(Bid bid) {
        mBannerViewListener.onOXBSdkWin();
    }

    @Override
    public void trackImpression() {

    }

    @Override
    public void destroy() {

    }
}
