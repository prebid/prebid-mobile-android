package com.openx.apollo.bidding.enums;

import androidx.annotation.Nullable;

import com.openx.apollo.models.PlacementType;

public enum VideoPlacementType {
    IN_BANNER(2),
    IN_ARTICLE(3),
    IN_FEED(4);

    private final int mValue;

    VideoPlacementType(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
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
