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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Builds the targeting of a bid that a bid selector chose, from that bid alone.
 * <p>
 * The result is the bid's own keys plus the generic keys ({@code hb_pb}, {@code hb_bidder},
 * {@code hb_cache_id}, ...) that Prebid Server sets only on the winner it picked. A generic key
 * the bid lacks is filled in from the bid's bidder-specific form of it, e.g. {@code hb_pb} from
 * {@code hb_pb_appnexus}, so the price bucket is the one Prebid Server computed. A key the bid has
 * in neither form is left out. No value ever comes from another bid.
 * <p>
 * The bidder code (the key suffix) is the value of the bid's {@code hb_bidder} or
 * {@code hb_bidder_<code>} key, so a bidder alias resolves to its own keys. A bidder-specific key
 * may be truncated by Prebid Server.
 */
final class SelectedBidTargeting {

    @Nullable
    private final HashMap<String, String> targeting;
    @Nullable
    private final String failure;

    private SelectedBidTargeting(@Nullable HashMap<String, String> targeting, @Nullable String failure) {
        this.targeting = targeting;
        this.failure = failure;
    }

    /**
     * The built targeting, or {@code null} if it can't be built unambiguously.
     */
    @Nullable
    HashMap<String, String> getTargeting() {
        return targeting;
    }

    /**
     * Why the targeting can't be built, or {@code null} if it was built.
     */
    @Nullable
    String getFailure() {
        return failure;
    }

    @NonNull
    static SelectedBidTargeting make(@NonNull Map<String, String> bidTargeting) {
        HashMap<String, String> result = new HashMap<>(bidTargeting);

        String bidderCode = bidTargeting.get(TargetingKey.BIDDER);
        if (bidderCode == null) {
            TreeSet<String> codes = new TreeSet<>();
            for (Map.Entry<String, String> entry : bidTargeting.entrySet()) {
                if (entry.getValue() != null && isBidderSpecificForm(entry.getKey(), TargetingKey.BIDDER, entry.getValue())) {
                    codes.add(entry.getValue());
                }
            }

            if (codes.size() > 1) {
                return failure("the bid names more than one bidder: " + join(codes));
            }

            if (codes.isEmpty()) {
                // Without a bidder code there is no bidder-specific key to read
                return new SelectedBidTargeting(result, null);
            }

            bidderCode = codes.first();
            result.put(TargetingKey.BIDDER, bidderCode);
        }

        for (String key : TargetingKey.SELECTED_BID_KEYS) {
            if (bidTargeting.containsKey(key)) {
                continue;
            }

            String fullForm = key + "_" + bidderCode;
            if (bidTargeting.containsKey(fullForm)) {
                result.put(key, bidTargeting.get(fullForm));
                continue;
            }

            List<String> truncatedForms = new ArrayList<>();
            for (String candidate : bidTargeting.keySet()) {
                if (isBidderSpecificForm(candidate, key, bidderCode)) {
                    truncatedForms.add(candidate);
                }
            }

            if (truncatedForms.size() > 1) {
                Collections.sort(truncatedForms);
                return failure("more than one key of the bid can be " + key + ": " + join(truncatedForms));
            }

            if (truncatedForms.size() == 1) {
                result.put(key, bidTargeting.get(truncatedForms.get(0)));
            }
        }

        return new SelectedBidTargeting(result, null);
    }

    /**
     * Whether {@code candidate} is the bidder-specific form of {@code key} for {@code bidderCode}:
     * {@code <key>_<bidderCode>}, or that key truncated, but never shorter than {@code <key>_} plus
     * one character.
     */
    static boolean isBidderSpecificForm(@NonNull String candidate, @NonNull String key, @NonNull String bidderCode) {
        String prefix = key + "_";

        return candidate.length() > prefix.length()
                && candidate.startsWith(prefix)
                && (prefix + bidderCode).startsWith(candidate);
    }

    @NonNull
    private static SelectedBidTargeting failure(@NonNull String failure) {
        return new SelectedBidTargeting(null, failure);
    }

    @NonNull
    private static String join(@NonNull Iterable<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
