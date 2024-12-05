/*
 * © 2024 SMARTYADS. LDA doing business as “TEQBLAZE”.
 * All rights reserved. You may not use this file except in  compliance with the applicable  license granted to
 * you  by SMARTYADS,  LDA doing business as “TEQBLAZE”  (the "License"). Unless required by applicable law or
 * agreed to in writing, software distributed under the  License is distributed on  an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either  express or implied. Specific authorizations and restrictions
 * shall be provided for in the License.
 */

package org.prebid.mobile.rendering.interstitial.rewarded;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RewardManager {

    private boolean userRewardedAlready = false;

    @Nullable
    private Runnable rewardListener;
    @Nullable
    private Runnable afterRewardListener;
    @NonNull
    private RewardedExt rewardedExt = RewardedExt.defaultExt();

    public void notifyRewardListener() {
        if (rewardListener != null && !userRewardedAlready) {
            userRewardedAlready = true;
            rewardListener.run();
            if (afterRewardListener != null) {
                afterRewardListener.run();
                afterRewardListener = null;
            }
        }
    }

    public void clear() {
        rewardListener = null;
        afterRewardListener = null;
        userRewardedAlready = false;
        rewardedExt = RewardedExt.defaultExt();
    }

    public boolean getUserRewardedAlready() {
        return userRewardedAlready;
    }

    public void setUserRewardedAlready(boolean userRewardedAlready) {
        this.userRewardedAlready = userRewardedAlready;
    }

    @Nullable
    public Runnable getRewardListener() {
        return rewardListener;
    }

    public void setRewardListener(@Nullable Runnable rewardListener) {
        this.rewardListener = rewardListener;
    }

    @Nullable
    public Runnable getAfterRewardListener() {
        return afterRewardListener;
    }

    public void setAfterRewardListener(@Nullable Runnable afterRewardListener) {
        this.afterRewardListener = afterRewardListener;
    }

    @NonNull
    public RewardedExt getRewardedExt() {
        return rewardedExt;
    }

    public void setRewardedExt(@NonNull RewardedExt rewardedExt) {
        this.rewardedExt = rewardedExt;
    }
}
