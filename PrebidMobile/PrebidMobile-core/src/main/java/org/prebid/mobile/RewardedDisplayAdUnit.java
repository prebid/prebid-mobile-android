/*
 *    Copyright 2018-2026 Prebid.org, Inc.
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

package org.prebid.mobile;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.prebid.mobile.rendering.models.AdPosition;

/**
 * Original API rewarded display ad unit.
 */
public class RewardedDisplayAdUnit extends InterstitialAdUnit {

    public RewardedDisplayAdUnit(@NonNull String configId) {
        super(configId);
        configuration.setRewarded(true);
        configuration.setAdPosition(AdPosition.FULLSCREEN);
    }

    public RewardedDisplayAdUnit(
            @NonNull String configId,
            @IntRange(from = 0, to = 100) int minWidthPerc,
            @IntRange(from = 0, to = 100) int minHeightPerc
    ) {
        this(configId);
        configuration.setMinSizePercentage(new AdSize(minWidthPerc, minHeightPerc));
    }

}
