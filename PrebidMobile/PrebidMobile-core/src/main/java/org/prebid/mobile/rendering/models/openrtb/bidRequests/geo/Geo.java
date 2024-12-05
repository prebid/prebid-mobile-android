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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.geo;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;

public class Geo extends BaseBid {

    public Float lat = null;
    public Float lon = null;
    public Integer type = null;
    public Integer accuracy = null;
    public Integer lastfix = null;

    public String country = null;
    public String region = null;

    //TODO: ORTB2.5: Auto detect? how?
    //Region of a country using FIPS 10-4 notation. While OpenRTB supports this attribute, it has been withdrawn by NIST in 2008.
    public String regionfips104 = null;

    public String metro = null;
    public String city = null;
    public String zip = null;
    public Integer utcoffset = null;

    /**
     * When you add a new field to this list, don't forget to add it to the {@link org.prebid.mobile.OpenRtbMerger}.
     */
    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "lat", this.lat);
        toJSON(jsonObject, "lon", this.lon);
        toJSON(jsonObject, "type", this.type);
        toJSON(jsonObject, "accuracy", this.accuracy);
        toJSON(jsonObject, "lastfix", this.lastfix);
        toJSON(jsonObject, "country", this.country);
        toJSON(jsonObject, "region", this.region);
        toJSON(jsonObject, "regionfips104", this.regionfips104);
        toJSON(jsonObject, "metro", this.metro);
        toJSON(jsonObject, "city", this.city);
        toJSON(jsonObject, "zip", this.zip);
        toJSON(jsonObject, "utcoffset", this.utcoffset);
        return jsonObject;
    }
}
