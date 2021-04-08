package org.prebid.mobile.rendering.bidding.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;

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
