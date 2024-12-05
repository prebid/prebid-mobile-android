/*
 * © 2024 SMARTYADS. LDA doing business as “TEQBLAZE”.
 * All rights reserved. You may not use this file except in  compliance with the applicable  license granted to
 * you  by SMARTYADS,  LDA doing business as “TEQBLAZE”  (the "License"). Unless required by applicable law or
 * agreed to in writing, software distributed under the  License is distributed on  an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either  express or implied. Specific authorizations and restrictions
 * shall be provided for in the License.
 */

package org.prebid.mobile.rendering.interstitial.rewarded;

import androidx.annotation.Nullable;

/**
 * Additional rules for closing rewarded ad.
 * Bid response JSON object: {@code seatbid.bid[].ext.rwdd.close}.
 */
public class RewardedClosingRules {

    private int postRewardTime = 0;
    private Action action = Action.CLOSE_BUTTON;

    public RewardedClosingRules() {
    }

    public RewardedClosingRules(@Nullable Integer postRewardTime, @Nullable Action action) {
        if (postRewardTime != null) {
            this.postRewardTime = postRewardTime;
        }
        if (action != null) {
            this.action = action;
        }
    }

    public int getPostRewardTime() {
        return postRewardTime;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        AUTO_CLOSE, CLOSE_BUTTON
    }

}
