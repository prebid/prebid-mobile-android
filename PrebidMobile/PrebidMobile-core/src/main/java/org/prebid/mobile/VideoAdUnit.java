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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class VideoAdUnit extends VideoBaseAdUnit {

    private final AdSize adSize;

    @Deprecated
    @Nullable
    private PlacementType type;

    public VideoAdUnit(@NonNull String configId, int width, int height) {
        super(configId, AdType.VIDEO);
        adSize = new AdSize(width, height);
    }

    /**
     * @deprecated Replaced by {@link #VideoAdUnit(String, int, int)}}
     */
    @Deprecated
    public VideoAdUnit(@NonNull String configId, int width, int height, PlacementType type) {
        this(configId, width, height);
        this.type = type;
    }

    AdSize getAdSize() {
        return adSize;
    }

    @Deprecated
    public PlacementType getType() {
        return type;
    }

    /**
     * @deprecated Replaced by {@link Signals.Placement}
     */
    @Deprecated
    public enum PlacementType {
        @Deprecated
        IN_BANNER(2),
        @Deprecated
        IN_ARTICLE(3),
        @Deprecated
        IN_FEED(4);

        private final int value;

        @Deprecated
        PlacementType(int value) {
            this.value = value;
        }

        @Deprecated
        public int getValue() {
            return value;
        }
    }
}
