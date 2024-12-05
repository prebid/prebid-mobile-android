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
import org.jetbrains.annotations.NotNull;

/**
 * Represents rewarded ext object. Bid response JSON object: {@code seatbid.bid[].ext.rwdd}.
 * It's responsible for the reward and the completion rules.
 */
public class RewardedExt {

    @Nullable
    private final Reward reward;
    @NotNull
    private final RewardedCompletionRules completionRules;
    @NotNull
    private final RewardedClosingRules closingRules;

    public RewardedExt(
            @Nullable Reward reward,
            @NotNull RewardedCompletionRules completionRules,
            @NotNull RewardedClosingRules closingData
    ) {
        this.reward = reward;
        this.completionRules = completionRules;
        this.closingRules = closingData;
    }

    @Nullable
    public Reward getReward() {
        return reward;
    }

    @NotNull
    public RewardedCompletionRules getCompletionRules() {
        return completionRules;
    }

    @NotNull
    public RewardedClosingRules getClosingRules() {
        return closingRules;
    }

    public static RewardedExt defaultExt() {
        return new RewardedExt(null, new RewardedCompletionRules(), new RewardedClosingRules());
    }

}
