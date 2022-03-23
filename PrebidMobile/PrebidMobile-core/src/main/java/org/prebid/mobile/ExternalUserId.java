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

import java.util.*;

/**
 * Defines the User Id Object from an External Third Party Source
 */
public class ExternalUserId {

    private String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    private String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    private Integer atype;

    public Integer getAtype() {
        return atype;
    }

    public void setAtype(Integer atype) {
        this.atype = atype;
    }

    private Map<String, Object> ext;

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    /**
     * Initialize ExternalUserId Class
     * - Parameter source: Source of the External User Id String.
     * - Parameter identifier: String of the External User Id.
     * - Parameter atype: (Optional) Integer of the External User Id.
     * - Parameter ext: (Optional) Map of the External User Id.
     */
    public ExternalUserId(@NonNull String source, @NonNull String identifier, Integer atype, Map<String, Object> ext) {
        this.source = source;
        this.identifier = identifier;
        this.atype = atype;
        this.ext = ext;
    }

    @Nullable
    public JSONObject getJson() {
        JSONObject result = new JSONObject();

        if (getSource() == null || getSource().isEmpty() || getIdentifier() == null || getIdentifier().isEmpty()) {
            return null;
        }

        try {
            JSONObject uidObject = new JSONObject();
            uidObject.putOpt("id", getIdentifier());
            uidObject.putOpt("adtype", getAtype());
            if (getExt() != null) {
                uidObject.putOpt("ext", new JSONObject(getExt()));
            }

            result.put("source", getSource());
            result.put("uids", new JSONArray().put(uidObject));
        } catch (JSONException e) {
            LogUtil.warning("ExternalUserId", "Can't create json object.");
            return null;
        }

        return result;
    }

    @Override
    public String toString() {
        JSONObject transformedUserIdObject = new JSONObject();
        try {
            transformedUserIdObject.put("source", getSource());
            transformedUserIdObject.put("id", getIdentifier());
            transformedUserIdObject.put("atype", getAtype());
            if (getExt() != null && !getExt().isEmpty()) {
                JSONObject extObject = new JSONObject(getExt());
                transformedUserIdObject.put("ext", extObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transformedUserIdObject.toString();

    }

    static List<ExternalUserId> getExternalUidListFromJson(String list) {
        List<ExternalUserId> externalUserIdList = new ArrayList<>();
        try {
            JSONArray jsonArr = new JSONArray(list);
            for (int i = 0; i < jsonArr.length(); i++) {
                if (getExternalUidFromJson(jsonArr.getJSONObject(i).toString()) != null) {
                    externalUserIdList.add(getExternalUidFromJson(jsonArr.getJSONObject(i).toString()));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return externalUserIdList;
    }

    static ExternalUserId getExternalUidFromJson(String json) {
        ExternalUserId extUId = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String source = jsonObject.has("source") ? jsonObject.optString("source") : null;
            String id = jsonObject.has("id") ? jsonObject.optString("id") : null;
            Integer aType = jsonObject.has("atype") ? jsonObject.optInt("atype") : null;
            Map<String, Object> ext = null;
            JSONObject extObj = jsonObject.optJSONObject("ext");
            if (extObj != null) {
                for (Iterator<String> it = extObj.keys(); it.hasNext(); ) {
                    ext = new HashMap<>();
                    String key = it.next();
                    ext.put(key, extObj.getString(key));
                }
            }
            extUId = new ExternalUserId(source, id, aType, ext);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return extUId;
    }
}
