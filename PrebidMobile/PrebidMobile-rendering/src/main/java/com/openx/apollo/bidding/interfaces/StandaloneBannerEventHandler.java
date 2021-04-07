package com.openx.apollo.bidding.interfaces;

import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.listeners.BannerEventListener;

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
