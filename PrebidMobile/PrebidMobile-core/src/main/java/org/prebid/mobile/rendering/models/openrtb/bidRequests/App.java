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
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.apps.Publisher;

public class App extends BaseBid {

    public String id = null;
    public String name = null;
    public String bundle = null;
    public String domain = null;
    //TODO: ORTB2.5: remove this? After product's decision?
    public String storeurl = null;
    public String[] cat = null;
    public String[] sectioncat = null;
    public String[] pagecat = null;
    public String ver = null;
    public Integer privacypolicy = null;
    public Integer paid = null;
    public String keywords = null;
    public ContentObject contentObject = null;
    private Publisher publisher = null;
    private Ext ext = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "id", id);
        toJSON(jsonObject, "name", name);
        toJSON(jsonObject, "bundle", bundle);
        toJSON(jsonObject, "domain", domain);
        toJSON(jsonObject, "storeurl", storeurl);
        if (cat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String cat : cat) {

                jsonArray.put(cat);
            }
            toJSON(jsonObject, "cat", jsonArray);
        }

        if (sectioncat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String sectionCat : sectioncat) {

                jsonArray.put(sectionCat);
            }
            toJSON(jsonObject, "sectioncat", jsonArray);
        }

        if (pagecat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String pageCat : pagecat) {

                jsonArray.put(pageCat);
            }
            toJSON(jsonObject, "pagecat", jsonArray);
        }

        if (contentObject != null && contentObject.getJsonObject() != null) {
            JSONObject contentJson = contentObject.getJsonObject();
            toJSON(jsonObject, "content", contentJson);
        }

        toJSON(jsonObject, "ver", ver);
        toJSON(jsonObject, "privacypolicy", privacypolicy);
        toJSON(jsonObject, "paid", paid);
        toJSON(jsonObject, "keywords", keywords);
        toJSON(jsonObject, "publisher", (publisher != null) ? this.publisher.getJsonObject() : null);
        toJSON(jsonObject, "ext", (ext != null) ? ext.getJsonObject() : null);

        return jsonObject;
    }

    public Publisher getPublisher() {
        if (publisher == null) {
            publisher = new Publisher();
        }
        return publisher;
    }

    public Ext getExt() {
        if (ext == null) {
            ext = new Ext();
        }
        return ext;
    }
}
