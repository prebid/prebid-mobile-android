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
package org.prebid.mobile.api.rendering.pluginrenderer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PluginRendererData {

    private HashMap<String, Object> pluginRendererDataHashMap = new HashMap<>();

    public void put(String key, Integer value) {
        pluginRendererDataHashMap.put(key, value);
    }

    public void put(String key, Float value) {
        pluginRendererDataHashMap.put(key, value);
    }

    public void put(String key, Long value) {
        pluginRendererDataHashMap.put(key, value);
    }

    public void put(String key, String value) {
        pluginRendererDataHashMap.put(key, value);
    }

    public void put(String key, Boolean value) {
        pluginRendererDataHashMap.put(key, value);
    }

    public void put(String key, JSONObject value) {
        pluginRendererDataHashMap.put(key, value);
    }

    public void put(String key, JSONArray value) {
        pluginRendererDataHashMap.put(key, value);
    }

    public void put(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        Iterator<String> jsonIterator = jsonObject.keys();
        while (jsonIterator.hasNext()) {
            String key = jsonIterator.next();
            pluginRendererDataHashMap.put(key, jsonObject.opt(key));
        }
    }

    public void remove(String key) {
        pluginRendererDataHashMap.remove(key);
    }

    public Map<String, Object> getPluginRendererData() {
        return Collections.unmodifiableMap(pluginRendererDataHashMap);
    }
}
