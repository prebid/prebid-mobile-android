package org.prebid.mobile.rendering.models.openrtb.bidRequests.mapper;

import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.PluginRenderer;

import java.util.ArrayList;
import java.util.List;

public class PluginRendererListMapper {

    public List<PluginRenderer> map(List<PrebidMobilePluginRenderer> prebidMobilePluginRenderers) {
        List<PluginRenderer> renderers = new ArrayList<>();
        for(PrebidMobilePluginRenderer element: prebidMobilePluginRenderers){
            PluginRenderer pluginRenderer = map(element);
            renderers.add(pluginRenderer);
        }
        return renderers;
    }

    private PluginRenderer map(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        PluginRenderer pluginRenderer = new PluginRenderer();
        pluginRenderer.setName(prebidMobilePluginRenderer.getName());
        pluginRenderer.setVersion(prebidMobilePluginRenderer.getVersion());
        pluginRenderer.setData(prebidMobilePluginRenderer.getData());
        return pluginRenderer;
    }
}
