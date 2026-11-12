/*
 *    Copyright 2020-2026 Prebid.org, Inc.
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

package org.prebid.mobile.api.eid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

/**
 * An OpenRTB Extended Identifier (EID).
 * <p>
 * Represents one entry of {@code user.eids} (OpenRTB 2.6) or {@code user.ext.eids} (OpenRTB 2.5) in a
 * bid request, as {@link org.prebid.mobile.PrebidMobile#setEidsPlacement} selects. See the <a href="https://github.com/InteractiveAdvertisingBureau/openrtb/blob/main/extensions/2.x_official_extensions/eids.md">
 * OpenRTB EID specification</a>.
 * <p>
 * Use {@link org.prebid.mobile.ExternalUserId} to build an EID from individual fields,
 * or {@link #wrap(JSONObject)} to wrap a pre-built JSON object.
 *
 * @see org.prebid.mobile.ExternalUserId
 */
public interface ExtendedId {

    /**
     * Returns the source domain of this EID (e.g. {@code "criteo.com"}).
     */
    @NonNull
    String getSource();

    /**
     * Returns the OpenRTB JSON representation of this EID, or {@code null} if it can't be built.
     * An EID whose JSON is {@code null} or empty is not sent.
     */
    @Nullable
    JSONObject getJson();

    /**
     * Wraps a pre-built OpenRTB EID JSON object as an {@link ExtendedId}, for identity libraries that
     * already produce the EID object, e.g.
     * {@code {"source": "id5-sync.com", "uids": [{"id": "...", "atype": 1}]}}.
     * <p>
     * The JSON is copied, so changing {@code json} afterwards doesn't change the EID. To send a new
     * value, return a new {@code ExtendedId} from {@link ExtendedIdProvider#getExtendedIds()}.
     *
     * @param json a valid OpenRTB EID JSON object
     * @return an ExtendedId backed by a copy of the given JSON
     * @throws IllegalArgumentException if {@code json} has no non-empty {@code "source"} string
     */
    @NonNull
    static ExtendedId wrap(@NonNull JSONObject json) {
        return new RawExtendedId(json);
    }
}
