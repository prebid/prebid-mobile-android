/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.api.rendering.listeners;

import androidx.annotation.Nullable;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.RewardedAdUnit;
import org.prebid.mobile.rendering.interstitial.rewarded.Reward;

/**
 * Listener interface representing RewardedAdUnit events.
 * All methods will be invoked on the main thread.
 */
public interface RewardedAdUnitListener {
    /**
     * Executed when the ad is loaded and is ready for display.
     *
     * @param rewardedAdUnit view of the corresponding event. Contains reward instance inside. Prebod reward is always null.
     */
    void onAdLoaded(RewardedAdUnit rewardedAdUnit);

    /**
     * Executed when the ad is displayed on screen.
     *
     * @param rewardedAdUnit view of the corresponding event.
     */
    void onAdDisplayed(RewardedAdUnit rewardedAdUnit);

    /**
     * Executed when an error is encountered on initialization / loading or display step.
     *
     * @param rewardedAdUnit view of the corresponding event.
     * @param exception  exception containing detailed message and error type.
     */
    void onAdFailed(RewardedAdUnit rewardedAdUnit, AdException exception);

    /**
     * Executed when rewardedAdUnit is clicked.
     *
     * @param rewardedAdUnit view of the corresponding event.
     */
    void onAdClicked(RewardedAdUnit rewardedAdUnit);

    /**
     * Executed when rewardedAdUnit is closed.
     *
     * @param rewardedAdUnit view of the corresponding event.
     */
    void onAdClosed(RewardedAdUnit rewardedAdUnit);

    /**
     * Executed when user receives reward.
     *
     * @param rewardedAdUnit the rewarded ad unit
     * @param reward the reward object. It can be null if it is not set in the ad server.
     */
    void onUserEarnedReward(RewardedAdUnit rewardedAdUnit, @Nullable Reward reward);
}
