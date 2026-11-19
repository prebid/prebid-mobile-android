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

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;

/**
 * Package-private {@link ExtendedId} implementation that wraps a raw JSON object.
 * <p>
 * It keeps the JSON as a string, so neither the caller's object nor an object handed to a bid
 * request is shared: a provider that keeps updating its own {@code JSONObject} on another thread
 * can't race with the serialization of a request.
 */
class RawExtendedId implements ExtendedId {

    private static final String TAG = "RawExtendedId";

    @NonNull
    private final String json;
    @NonNull
    private final String source;

    RawExtendedId(@NonNull JSONObject json) {
        // optString would turn a null or numeric source into a string, while the JSON sent would keep
        // the original value. The JSON holds user identifiers, so the exception doesn't include it.
        Object source = json.opt("source");
        if (!(source instanceof String) || ((String) source).isEmpty()) {
            throw new IllegalArgumentException("Extended ID JSON must contain a non-empty \"source\" string");
        }
        // toString() returns null for JSON it can't encode, e.g. a NaN number.
        String encoded = json.toString();
        if (encoded == null) {
            throw new IllegalArgumentException("Extended ID JSON from source " + source + " can't be encoded");
        }
        this.source = (String) source;
        this.json = encoded;
    }

    @NonNull
    @Override
    public String getSource() {
        return source;
    }

    /**
     * Returns a new copy of the wrapped JSON on every call.
     */
    @Nullable
    @Override
    public JSONObject getJson() {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            LogUtil.warning(TAG, "Can't copy the extended ID JSON from source " + source);
            return null;
        }
    }
}
