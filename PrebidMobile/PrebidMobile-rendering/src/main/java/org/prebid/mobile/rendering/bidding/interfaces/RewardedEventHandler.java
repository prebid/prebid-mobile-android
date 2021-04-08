package org.prebid.mobile.rendering.bidding.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;

public interface RewardedEventHandler {
    void setRewardedEventListener(
        @NonNull
                RewardedVideoEventListener listener);

    void requestAdWithBid(@Nullable
                              Bid bid);

    void show();

    void trackImpression();

    void destroy();
}
