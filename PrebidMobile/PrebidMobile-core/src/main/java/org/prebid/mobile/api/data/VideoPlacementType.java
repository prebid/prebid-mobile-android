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

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.models.PlacementType;

/**
 * Video placement type for additional targeting.
 */
public enum VideoPlacementType {
    IN_BANNER(2),
    IN_ARTICLE(3),
    IN_FEED(4);

    private final int value;

    VideoPlacementType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Nullable
    public static VideoPlacementType mapToVideoPlacementType(int placementTypeValue) {
        for (VideoPlacementType videoPlacementType : VideoPlacementType.values()) {
            if (videoPlacementType.getValue() == placementTypeValue) {
                return videoPlacementType;
            }
        }

        return null;
    }

    public static PlacementType mapToPlacementType(VideoPlacementType videoPlacementType) {
        if (videoPlacementType == null) {
            return PlacementType.UNDEFINED;
        }

        for (PlacementType placementType : PlacementType.values()) {
            if (placementType.getValue() == videoPlacementType.getValue()) {
                return placementType;
            }
        }

        return PlacementType.UNDEFINED;
    }
}
