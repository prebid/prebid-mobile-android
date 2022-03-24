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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.apps;

import androidx.annotation.VisibleForTesting;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;

//TODO: ORTB2.5: get back for In-stream video - per Product
public class Content extends BaseBid {

    @VisibleForTesting
    String id = null;
    @VisibleForTesting
    Integer episode = null;
    @VisibleForTesting
    String title = null;
    @VisibleForTesting
    String series = null;
    @VisibleForTesting
    String season = null;
    @VisibleForTesting
    String url = null;
    @VisibleForTesting
    String[] cat = null;
    @VisibleForTesting
    Integer videoquality = null;
    @VisibleForTesting
    Integer context = null;
    @VisibleForTesting
    Integer qagmediarating = null;
    @VisibleForTesting
    String contentrating = null;
    @VisibleForTesting
    String userrating = null;
    @VisibleForTesting
    String keywords = null;
    @VisibleForTesting
    String livestream = null;
    @VisibleForTesting
    String sourcerelationship = null;
    @VisibleForTesting
    String len = null;
    @VisibleForTesting
    String language = null;
    @VisibleForTesting
    String embeddable = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "id", this.id);
        toJSON(jsonObject, "episode", this.episode);
        toJSON(jsonObject, "title", this.title);
        toJSON(jsonObject, "series", this.series);
        toJSON(jsonObject, "season", this.season);
        toJSON(jsonObject, "url", this.url);

        if (cat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String catItem : cat) {

                jsonArray.put(catItem);
            }

            toJSON(jsonObject, "cat", jsonArray);
        }

        toJSON(jsonObject, "videoquality", this.videoquality);
        toJSON(jsonObject, "context", this.context);
        toJSON(jsonObject, "qagmediarating", this.qagmediarating);
        toJSON(jsonObject, "contentrating", this.contentrating);
        toJSON(jsonObject, "userrating", this.userrating);
        toJSON(jsonObject, "keywords", this.keywords);
        toJSON(jsonObject, "livestream", this.livestream);
        toJSON(jsonObject, "sourcerelationship", this.sourcerelationship);
        toJSON(jsonObject, "len", this.len);
        toJSON(jsonObject, "language", this.language);
        toJSON(jsonObject, "embeddable", this.embeddable);

        return jsonObject;
    }
}
