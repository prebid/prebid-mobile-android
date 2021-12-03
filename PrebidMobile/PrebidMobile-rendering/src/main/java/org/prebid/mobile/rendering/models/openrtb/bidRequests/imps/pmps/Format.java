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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;

public class Format extends BaseBid {

    public Integer w;
    public Integer h;

    public Format(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "w", w);
        toJSON(jsonObject, "h", h);

        return jsonObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Format format = (Format) o;

        if (w != null ? !w.equals(format.w) : format.w != null) {
            return false;
        }
        return h != null ? h.equals(format.h) : format.h == null;
    }

    @Override
    public int hashCode() {
        int result = w != null ? w.hashCode() : 0;
        result = 31 * result + (h != null ? h.hashCode() : 0);
        return result;
    }
}
