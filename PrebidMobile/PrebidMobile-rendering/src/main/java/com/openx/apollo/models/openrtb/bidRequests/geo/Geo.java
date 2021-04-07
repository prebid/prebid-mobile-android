package com.openx.apollo.models.openrtb.bidRequests.geo;

import com.openx.apollo.models.openrtb.bidRequests.BaseBid;

import org.json.JSONException;
import org.json.JSONObject;

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
