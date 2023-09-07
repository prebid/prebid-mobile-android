package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.prebid.mobile.api.rendering.pluginrenderer.PluginRendererData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PluginRendererListTest {

    private PluginRendererData pluginRendererData = new PluginRendererData();

    @Test
    public void testGetJsonObjectWithEmptyList() throws JSONException {
        PluginRendererList pluginRendererList = new PluginRendererList();
        List<PluginRenderer> emptyList = Arrays.asList();

        pluginRendererList.setList(emptyList);
        JSONObject jsonObject = (JSONObject) pluginRendererList.getJsonObject().get("sdk");

        Assert.assertTrue(jsonObject.has("renderers"));
        JSONArray jsonArray = jsonObject.getJSONArray("renderers");
        Assert.assertEquals(0, jsonArray.length());
    }

    @Test
    public void testGetJsonObjectWithNonEmptyList() throws JSONException {
        PluginRendererList pluginRendererList = new PluginRendererList();
        pluginRendererData.put("key1", 1);
        pluginRendererData.put("key2", "value");
        pluginRendererData.put("key3", true);


        List<PluginRenderer> pluginList = Arrays.asList(
                createPlugin("Plugin 1", "1.0", pluginRendererData),
                createPlugin("Plugin 2", "2.0", pluginRendererData)
        );

        pluginRendererList.setList(pluginList);
        JSONObject jsonObject = (JSONObject) pluginRendererList.getJsonObject().get("sdk");

        Assert.assertTrue(jsonObject.has("renderers"));
        JSONArray jsonArray = jsonObject.getJSONArray("renderers");
        Assert.assertEquals(pluginList.size(), jsonArray.length());

        for (int i = 0; i < pluginList.size(); i++) {
            JSONObject pluginObj = jsonArray.getJSONObject(i);
            PluginRenderer plugin = pluginList.get(i);

            Assert.assertEquals(plugin.getName(), pluginObj.getString("name"));
            Assert.assertEquals(plugin.getVersion(), pluginObj.getString("version"));
            // Check if hashmaps values are same regardless indexes, since aren't linkedhashmaps
            Assert.assertTrue(areHashMapsEqual(plugin.getData(), toHashMap((JSONObject) pluginObj.get("data"))));
        }
    }

    private boolean areHashMapsEqual(Map<String, Object> map1, Map<String, Object> map2) {
        if (map1 == null || map2 == null) {
            return false;
        }

        if (map1.size() != map2.size()) {
            return false;
        }

        long count = map1.entrySet()
                .stream()
                .filter(entry -> map2.containsKey(entry.getKey()) && entry.getValue().equals(map2.get(entry.getKey())))
                .count();

        return count == map1.size();
    }

    private HashMap<String, Object> toHashMap(JSONObject jsonObj) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObj.get(key);
            if (value instanceof JSONObject) {
                value = toHashMap((JSONObject) value);
            }
            map.put(key, value);
        }   return map;
    }

    private PluginRenderer createPlugin(String name, String version, PluginRendererData data) {
        PluginRenderer plugin = new PluginRenderer();
        plugin.setName(name);
        plugin.setVersion(version);
        plugin.setData(data);
        return plugin;
    }
}