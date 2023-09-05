package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PluginRendererListTest {

    HashMap<String, String> data = new HashMap<String, String>() {{
        put("extra", "value");
    }};

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

        List<PluginRenderer> pluginList = Arrays.asList(
                createPlugin("Plugin 1", "1.0", data),
                createPlugin("Plugin 2", "2.0", data)
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
            Assert.assertEquals(plugin.getData(), pluginObj.get("data"));
        }
    }

    @Test
    public void testGetJsonObjectWithNoData() throws JSONException {
        PluginRendererList pluginRendererList = new PluginRendererList();
        List<PluginRenderer> pluginList = Arrays.asList(
                createPlugin("Plugin 1", "1.0", data),
                createPlugin("Plugin 2", "2.0", data)
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
            Assert.assertEquals(plugin.getData(), pluginObj.get("data"));
        }
    }

    private PluginRenderer createPlugin(String name, String version, HashMap<String, String> data) {
        PluginRenderer plugin = new PluginRenderer();
        plugin.setName(name);
        plugin.setVersion(version);
        plugin.setData(data);
        return plugin;
    }
}