package com.openx.apollo.bidding.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.listeners.BannerEventListener;

public interface BannerEventHandler {
    AdSize[] getAdSizeArray();

    void setBannerEventListener(@NonNull
                                    BannerEventListener bannerViewListener);

    void requestAdWithBid(@Nullable Bid bid);

    void trackImpression();

    void destroy();
}
