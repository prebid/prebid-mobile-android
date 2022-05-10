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

package org.prebid.mobile.api.data;

import org.prebid.mobile.rendering.models.AdPosition;

public enum BannerAdPosition {
    UNDEFINED(-1),
    UNKNOWN(0),
    HEADER(4),
    FOOTER(5),
    SIDEBAR(6);

    private final int value;

    BannerAdPosition(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BannerAdPosition mapToDisplayAdPosition(int adPosition) {
        final BannerAdPosition[] values = BannerAdPosition.values();
        for (BannerAdPosition bannerAdPosition : values) {
            if (bannerAdPosition.getValue() == adPosition) {
                return bannerAdPosition;
            }
        }
        return BannerAdPosition.UNDEFINED;
    }

    public static AdPosition mapToAdPosition(BannerAdPosition bannerAdPosition) {
        if (bannerAdPosition == null) {
            return AdPosition.UNDEFINED;
        }

        for (AdPosition adPosition : AdPosition.values()) {
            if (adPosition.getValue() == bannerAdPosition.getValue()) {
                return adPosition;
            }
        }
        return AdPosition.UNDEFINED;
    }
}
