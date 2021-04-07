package com.openx.apollo.bidding.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.listeners.RewardedVideoEventListener;

public class StandaloneRewardedVideoEventHandler implements RewardedEventHandler {
    private RewardedVideoEventListener mListener;

    @Override
    public void setRewardedEventListener(
        @NonNull
            RewardedVideoEventListener listener) {
        mListener = listener;
    }

    @Override
    public void requestAdWithBid(
        @Nullable
            Bid bid) {
        mListener.onOXBSdkWin();
    }

    @Override
    public void show() {

    }

    @Override
    public void trackImpression() {

    }

    @Override
    public void destroy() {

    }
}
