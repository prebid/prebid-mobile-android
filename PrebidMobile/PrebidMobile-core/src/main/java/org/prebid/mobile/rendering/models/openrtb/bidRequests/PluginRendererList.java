/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class PluginRendererList extends BaseBid {

    public static String RENDERERS_KEY = "renderers";

    private List<PluginRenderer> renderers;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, RENDERERS_KEY, this.renderers);
        return jsonObject;
    }

    public void setList(List<PluginRenderer> renderers) {
        this.renderers = renderers;
    }

    public List<PluginRenderer> get() {
        return this.renderers;
    }

    @Override
    protected void toJSON(JSONObject jsonObject, String key, Object value) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        List<PluginRenderer> list = (List<PluginRenderer>) value;
        for (PluginRenderer plugin : list) {
            JSONObject pluginObj = new JSONObject();
            pluginObj.put("name", plugin.getName());
            pluginObj.put("version", plugin.getVersion());
            JSONObject pluginDataObj = plugin.getData();
            if (pluginDataObj != null) {
                pluginObj.put("data", pluginDataObj);
            }
            jsonArray.put(pluginObj);
        }
        jsonObject.put(key, jsonArray);
    }
}
