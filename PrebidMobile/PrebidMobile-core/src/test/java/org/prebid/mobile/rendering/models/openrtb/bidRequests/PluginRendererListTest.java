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
        JSONObject jsonObject = (JSONObject) pluginRendererList.getJsonObject().get("sdk");

        Assert.assertTrue(jsonObject.has("renderers"));
        JSONArray jsonArray = jsonObject.getJSONArray("renderers");
        Assert.assertEquals(0, jsonArray.length());
    }

    @Test
    public void testGetJsonObjectWithNonEmptyList() throws JSONException {
        PluginRendererList pluginRendererList = new PluginRendererList();
        List<PluginRenderer> pluginList = Arrays.asList(
                createPlugin("Plugin 1", "1.0", "token1"),
                createPlugin("Plugin 2", "2.0", "token2")
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
            Assert.assertEquals(plugin.getToken(), pluginObj.getString("token"));
        }
    }

    @Test
    public void testGetJsonObjectWithNoToken() throws JSONException {
        PluginRendererList pluginRendererList = new PluginRendererList();
        List<PluginRenderer> pluginList = Arrays.asList(
                createPlugin("Plugin 1", "1.0", null),
                createPlugin("Plugin 2", "2.0", null)
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
            Assert.assertFalse(plugin.getToken(), pluginObj.has("token"));
        }
    }

    private PluginRenderer createPlugin(String name, String version, String token) {
        PluginRenderer plugin = new PluginRenderer();
        plugin.setName(name);
        plugin.setVersion(version);
        plugin.setToken(token);
        return plugin;
    }
}