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
 * Package-private {@link ExtendedId} implementation that wraps a raw JSON object.
 */
class RawExtendedId implements ExtendedId {

    @NonNull
    private final JSONObject json;
    @NonNull
    private final String source;

    RawExtendedId(@NonNull JSONObject json) {
        String src = json.optString("source", "");
        if (src.isEmpty()) {
            throw new IllegalArgumentException("JSON must contain a non-empty \"source\" field");
        }
        this.json = json;
        this.source = src;
    }

    @NonNull
    @Override
    public String getSource() {
        return source;
    }

    @NonNull
    @Override
    public JSONObject getJson() {
        return json;
    }
}