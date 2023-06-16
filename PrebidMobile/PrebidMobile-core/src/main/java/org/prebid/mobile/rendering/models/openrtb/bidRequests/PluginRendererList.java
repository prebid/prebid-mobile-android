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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PluginRendererList extends BaseBid {

    private List<PluginRenderer> renderers;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "plugin_renderers", this.renderers); // TODO it will not handle the list, make a test with proxyman
        return jsonObject;
    }

    public void setList(List<PluginRenderer> renderers) {
        this.renderers = renderers;
    }

    public List<PluginRenderer> getList() {
        return this.renderers;
    }
}
