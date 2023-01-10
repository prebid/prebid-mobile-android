/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.rendering.models.PlacementType;

import java.util.List;

public abstract class VideoBaseAdUnit extends AdUnit {

    VideoBaseAdUnit(@NonNull String configId, @NonNull AdFormat adType) {
        super(configId, adType);
    }

    @Nullable
    public Parameters getParameters() {
        return configuration.getVideoParameters();
    }

    public void setParameters(@Nullable Parameters parameters) {
        configuration.setVideoParameters(parameters);
    }

    /**
     * Describes an <a href="https://www.iab.com/wp-content/uploads/2016/03/OpenRTB-API-Specification-Version-2-5-FINAL.pdf">OpenRTB</a> video object
     */
    public static class Parameters {

        /**
         List of supported API frameworks for this impression. If an API is not explicitly listed, it is assumed not to be supported.
         */
        @Nullable
        private List<Signals.Api> api;

        /**
         Maximum bit rate in Kbps.
         */
        @Nullable
        private Integer maxBitrate;

        /**
         Maximum bit rate in Kbps.
         */
        @Nullable
        private Integer minBitrate;

        /**
         Maximum video ad duration in seconds.
         */
        @Nullable
        private Integer maxDuration;

        /**
         Minimum video ad duration in seconds.
         */
        @Nullable
        private Integer minDuration;

        /**
         Content MIME types supported

         # Example #
         * "video/mp4"
         * "video/x-ms-wmv"
         */
        @Nullable
        private List<String> mimes;

        /**
         Allowed playback methods. If none specified, assume all are allowed.
         */
        @Nullable
        private List<Signals.PlaybackMethod> playbackMethod;

        /**
         Array of supported video bid response protocols.
         */
        @Nullable
        private List<Signals.Protocols> protocols;

        /**
         Indicates the start delay in seconds for pre-roll, mid-roll, or post-roll ad placements.
         */
        @Nullable
        private Signals.StartDelay startDelay;

        /**
         Placement type for the impression.
         */
        @Nullable
        private Signals.Placement placement;

        /**
         Placement type for the impression.
         */
        @Nullable
        private Integer linearity;

        //Getters and setters
        @Nullable
        public List<Signals.Api> getApi() {
            return api;
        }

        public void setApi(@Nullable List<Signals.Api> api) {
            this.api = api;
        }

        @Nullable
        public Integer getMaxBitrate() {
            return maxBitrate;
        }

        public void setMaxBitrate(@Nullable Integer maxBitrate) {
            this.maxBitrate = maxBitrate;
        }

        @Nullable
        public Integer getMinBitrate() {
            return minBitrate;
        }

        public void setMinBitrate(@Nullable Integer minBitrate) {
            this.minBitrate = minBitrate;
        }

        @Nullable
        public Integer getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(@Nullable Integer maxDuration) {
            this.maxDuration = maxDuration;
        }

        @Nullable
        public Integer getMinDuration() {
            return minDuration;
        }

        public void setMinDuration(@Nullable Integer minDuration) {
            this.minDuration = minDuration;
        }

        @Nullable
        public List<String> getMimes() {
            return mimes;
        }

        public void setMimes(@Nullable List<String> mimes) {
            this.mimes = mimes;
        }

        @Nullable
        public List<Signals.PlaybackMethod> getPlaybackMethod() {
            return playbackMethod;
        }

        public void setPlaybackMethod(@Nullable List<Signals.PlaybackMethod> playbackMethod) {
            this.playbackMethod = playbackMethod;
        }

        @Nullable
        public List<Signals.Protocols> getProtocols() {
            return protocols;
        }

        public void setProtocols(@Nullable List<Signals.Protocols> protocols) {
            this.protocols = protocols;
        }

        @Nullable
        public Signals.StartDelay getStartDelay() {
            return startDelay;
        }

        public void setStartDelay(@Nullable Signals.StartDelay startDelay) {
            this.startDelay = startDelay;
        }

        @Nullable
        public Signals.Placement getPlacement() {
            return placement;
        }

        public void setPlacement(@Nullable Signals.Placement placement) {
            this.placement = placement;
        }

        @Nullable
        public Integer getLinearity() {
            return linearity;
        }

        public void setLinearity(@Nullable Integer linearity) {
            this.linearity = linearity;
        }
    }

}
