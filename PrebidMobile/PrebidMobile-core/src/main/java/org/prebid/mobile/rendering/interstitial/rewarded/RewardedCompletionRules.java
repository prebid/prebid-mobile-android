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
 * Rules for completion rewarded ad.
 * Bid response JSON object: {@code seatbid.bid[].ext.rwdd.completion}.
 */
public class RewardedCompletionRules {

    public static final int DEFAULT_BANNER_TIME_MS = 120_000;

    public RewardedCompletionRules() {
    }

    public RewardedCompletionRules(
            @Nullable Integer bannerTime,
            @Nullable Integer videoTime,
            @Nullable Integer endCardTime,
            @Nullable String bannerEvent,
            @Nullable PlaybackEvent videoEvent,
            @Nullable String endCardEvent
    ) {
        if (bannerTime != null) {
            this.bannerTime = bannerTime;
        }
        if (endCardTime != null) {
            this.endCardTime = endCardTime;
        }
        this.videoEvent = videoEvent;
        this.videoTime = videoTime;
        this.bannerEvent = bannerEvent;
        this.endCardEvent = endCardEvent;
    }

    private int bannerTime = DEFAULT_BANNER_TIME_MS / 1000;
    private int endCardTime = DEFAULT_BANNER_TIME_MS / 1000;
    @Nullable
    private Integer videoTime;

    @Nullable
    private String bannerEvent;
    @Nullable
    private PlaybackEvent videoEvent;
    @Nullable
    private String endCardEvent;

    public int getBannerTime() {
        return bannerTime;
    }

    @Nullable
    public Integer getVideoTime() {
        return videoTime;
    }

    public int getEndCardTime() {
        return endCardTime;
    }

    @Nullable
    public String getBannerEvent() {
        return bannerEvent;
    }

    @Nullable
    public PlaybackEvent getVideoEvent() {
        return videoEvent;
    }

    @Nullable
    public String getEndCardEvent() {
        return endCardEvent;
    }

    public static PlaybackEvent getDefaultPlaybackEvent() {
        return PlaybackEvent.COMPLETE;
    }

    public enum PlaybackEvent {
        START, FIRST_QUARTILE, MIDPOINT, THIRD_QUARTILE, COMPLETE;

        @Nullable
        public static PlaybackEvent fromString(String playbackEventString) {
            if (playbackEventString.isEmpty()) return null;

            if (playbackEventString.equalsIgnoreCase("start")) {
                return START;
            } else if (playbackEventString.equalsIgnoreCase("firstquartile")) {
                return FIRST_QUARTILE;
            } else if (playbackEventString.equalsIgnoreCase("midpoint")) {
                return MIDPOINT;
            } else if (playbackEventString.equalsIgnoreCase("thirdquartile")) {
                return THIRD_QUARTILE;
            } else if (playbackEventString.equalsIgnoreCase("complete")) {
                return COMPLETE;
            }

            return null;
        }

        public long getCompletionTime(long duration) {
            if (this == START) {
                return 0;
            } else if (this == FIRST_QUARTILE) {
                return (int) (duration * 0.25);
            } else if (this == MIDPOINT) {
                return (int) (duration * 0.50);
            } else if (this == THIRD_QUARTILE) {
                return (int) (duration * 0.75);
            } else if (this == COMPLETE) {
                return duration;
            }

            return 0;
        }

    }

}
