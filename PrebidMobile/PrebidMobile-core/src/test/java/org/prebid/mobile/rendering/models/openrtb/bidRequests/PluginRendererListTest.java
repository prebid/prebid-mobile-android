package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PluginRendererListTest {

    @Test
    public void testGetJsonObjectWithEmptyList() throws JSONException {
        PluginRendererList pluginRendererList = new PluginRendererList();
        List<PluginRenderer> emptyList = Arrays.asList();

        pluginRendererList.setList(emptyList);

        Assert.assertTrue(pluginRendererList.getJsonObject().has(PluginRendererList.RENDERERS_KEY));
        JSONArray jsonArray = pluginRendererList.getJsonObject().getJSONArray(PluginRendererList.RENDERERS_KEY);
        Assert.assertEquals(0, jsonArray.length());
    }

    @Test
    public void testGetJsonObjectWithNonEmptyList() throws JSONException {
        PluginRendererList pluginRendererList = new PluginRendererList();
        JSONObject pluginData = new JSONObject();
        pluginData.put("key1", 1);
        pluginData.put("key2", "value");
        pluginData.put("key3", true);


        List<PluginRenderer> pluginList = Arrays.asList(
                createPlugin("Plugin 1", "1.0", pluginData),
                createPlugin("Plugin 2", "2.0", pluginData)
        );

        pluginRendererList.setList(pluginList);

        Assert.assertTrue(pluginRendererList.getJsonObject().has(PluginRendererList.RENDERERS_KEY));
        JSONArray jsonArray = pluginRendererList.getJsonObject().getJSONArray(PluginRendererList.RENDERERS_KEY);
        Assert.assertEquals(pluginList.size(), jsonArray.length());

        for (int i = 0; i < pluginList.size(); i++) {
            JSONObject pluginObj = jsonArray.getJSONObject(i);
            PluginRenderer plugin = pluginList.get(i);

            Assert.assertEquals(plugin.getName(), pluginObj.getString("name"));
            Assert.assertEquals(plugin.getVersion(), pluginObj.getString("version"));
            Assert.assertEquals(plugin.getData(), pluginObj.getJSONObject("data"));
        }
    }

    private PluginRenderer createPlugin(String name, String version, JSONObject data) {
        PluginRenderer plugin = new PluginRenderer();
        plugin.setName(name);
        plugin.setVersion(version);
        plugin.setData(data);
        return plugin;
    }
}