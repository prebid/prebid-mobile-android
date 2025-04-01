/*
 *    Copyright 2020-2021 Prebid.org, Inc.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * User id object from an external third-party source for additional targeting.
 * <a href="https://github.com/InteractiveAdvertisingBureau/openrtb/blob/main/extensions/2.x_official_extensions/eids.md">OpenRTB extended identifiers</a>.
 */
public class ExternalUserId {

    private static final String TAG = "ExternalUserId";

    @NonNull
    private String source;
    @NonNull
    private List<UniqueId> uniqueIds;
    @Nullable
    private Map<String, Object> ext;

    /**
     * Default constructor.
     */
    public ExternalUserId(@NonNull String source, @NonNull List<UniqueId> uniqueIds) {
        this.source = source;
        this.uniqueIds = uniqueIds;
    }

    @NonNull
    public String getSource() {
        return source;
    }

    @NonNull
    public List<UniqueId> getUniqueIds() {
        return uniqueIds;
    }

    @NonNull
    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(@Nullable Map<String, Object> ext) {
        this.ext = ext;
    }

    @Nullable
    public JSONObject getJson() {
        if (source == null || source.isEmpty()) {
            LogUtil.warning(TAG, "Empty source");
            return null;
        }

        try {
            JSONObject rootJson = new JSONObject();
            JSONArray uniqueIdArray = new JSONArray();
            for (UniqueId uniqueId : uniqueIds) {
                JSONObject idJson = uniqueId.getJson();
                if (idJson == null) continue;
                uniqueIdArray.put(idJson);
            }
            if (uniqueIdArray.length() == 0) {
                LogUtil.warning(TAG, "No unique ids");
                return null;
            }

            rootJson.put("source", source);
            rootJson.put("uids", uniqueIdArray);
            if (ext != null) {
                rootJson.putOpt("ext", new JSONObject(ext));
            }
            return rootJson;
        } catch (JSONException e) {
            LogUtil.warning(TAG, "Can't create external user id json");
            return null;
        }
    }

    public static class UniqueId {

        @NonNull
        private String id;
        @NonNull
        private Integer atype;
        @Nullable
        private Map<String, Object> ext;

        public UniqueId(@NonNull String id, @NonNull Integer atype) {
            this.id = id;
            this.atype = atype;
        }

        public void setExt(@Nullable Map<String, Object> ext) {
            this.ext = ext;
        }

        @NonNull
        public String getId() {
            return id;
        }

        @NonNull
        public Integer getAtype() {
            return atype;
        }

        @Nullable
        public JSONObject getJson() {
            if (id == null || id.isEmpty()) {
                return null;
            }
            try {
                JSONObject uniqueId = new JSONObject();
                uniqueId.putOpt("id", id);
                uniqueId.putOpt("atype", atype);
                if (ext != null) {
                    uniqueId.putOpt("ext", new JSONObject(ext));
                }
                return uniqueId;
            } catch (JSONException e) {
                return null;
            }
        }

    }

}
