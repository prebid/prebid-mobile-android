package com.openx.apollo.bidding.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.listeners.RewardedVideoEventListener;

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
