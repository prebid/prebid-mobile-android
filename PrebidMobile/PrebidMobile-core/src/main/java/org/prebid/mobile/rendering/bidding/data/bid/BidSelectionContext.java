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

package org.prebid.mobile.rendering.bidding.data.bid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.api.data.AdFormat;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * The ad unit whose auction a {@link PrebidBidSelecting} decides.
 */
public final class BidSelectionContext {

    @Nullable
    private final String configId;
    @NonNull
    private final Set<AdFormat> adFormats;

    BidSelectionContext(@Nullable String configId, @Nullable EnumSet<AdFormat> adFormats) {
        this.configId = configId;
        this.adFormats = adFormats == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(adFormats));
    }

    /**
     * The ad unit's configuration ID (the stored impression ID).
     */
    @Nullable
    public String getConfigId() {
        return configId;
    }

    /**
     * The ad formats the ad unit requested.
     */
    @NonNull
    public Set<AdFormat> getAdFormats() {
        return adFormats;
    }
}
