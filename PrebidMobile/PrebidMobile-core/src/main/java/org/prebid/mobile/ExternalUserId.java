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

import org.prebid.mobile.api.eid.ExtendedId;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User id object from an external third-party source for additional targeting.
 * <a href="https://github.com/InteractiveAdvertisingBureau/openrtb2.x/blob/main/2.6.md#3227---object-eid-">OpenRTB 2.6 EID object</a>,
 * <a href="https://github.com/InteractiveAdvertisingBureau/openrtb/blob/main/extensions/2.x_official_extensions/eids.md">OpenRTB 2.5 extended identifiers</a>.
 */
public class ExternalUserId implements ExtendedId {

    private static final String TAG = "ExternalUserId";

    @NonNull
    private final String source;
    @NonNull
    private final List<UniqueId> uniqueIds;
    @Nullable
    private Map<String, Object> ext;
    @Nullable
    private String inserter;
    @Nullable
    private String matcher;
    @Nullable
    private Integer mm;

    /**
     * Creates an external user ID for a given source.
     *
     * @param source    identifier of the ID source (e.g. {@code "criteo.com"}, {@code "pubcid.org"})
     * @param uniqueIds one or more unique identifiers from this source
     */
    public ExternalUserId(@NonNull String source, @NonNull List<UniqueId> uniqueIds) {
        this.source = source;
        this.uniqueIds = uniqueIds;
    }

    @NonNull
    @Override
    public String getSource() {
        return source;
    }

    @NonNull
    public List<UniqueId> getUniqueIds() {
        return uniqueIds;
    }

    @NonNull
    public Map<String, Object> getExt() {
        return ext == null ? Collections.emptyMap() : ext;
    }

    public void setExt(@Nullable Map<String, Object> ext) {
        this.ext = ext;
    }

    @Nullable
    public String getInserter() {
        return inserter;
    }

    /**
     * Canonical domain of the entity that added this ID to the request. ORTB: {@code user.eids[].inserter}
     */
    public void setInserter(@Nullable String inserter) {
        this.inserter = inserter;
    }

    @Nullable
    public String getMatcher() {
        return matcher;
    }

    /**
     * Technology that provided the match method in {@code mm}. ORTB: {@code user.eids[].matcher}
     */
    public void setMatcher(@Nullable String matcher) {
        this.matcher = matcher;
    }

    @Nullable
    public Integer getMm() {
        return mm;
    }

    /**
     * Match method used by the matcher, from the AdCOM 1.0 list "ID Match Methods". ORTB: {@code user.eids[].mm}
     */
    public void setMm(@Nullable Integer mm) {
        this.mm = mm;
    }

    @Nullable
    @Override
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
            rootJson.putOpt("inserter", inserter);
            rootJson.putOpt("matcher", matcher);
            rootJson.putOpt("mm", mm);
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
        private final String id;
        @NonNull
        private final Integer atype;
        @Nullable
        private Map<String, Object> ext;

        /**
         * @param id    the user identifier string
         * @param atype agent type that produced this ID
         *              (see <a href="https://github.com/InteractiveAdvertisingBureau/openrtb/blob/main/extensions/2.x_official_extensions/eids.md">OpenRTB EIDs spec</a>)
         */
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
