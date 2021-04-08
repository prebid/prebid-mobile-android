package org.prebid.mobile.rendering.bidding.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;

public interface BannerEventHandler {
    AdSize[] getAdSizeArray();

    void setBannerEventListener(@NonNull
                                        BannerEventListener bannerViewListener);

    void requestAdWithBid(@Nullable Bid bid);

    void trackImpression();

    void destroy();
}
