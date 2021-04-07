package com.openx.apollo.models.openrtb.bidRequests.imps.pmps;

import com.openx.apollo.models.openrtb.bidRequests.BaseBid;

import org.json.JSONException;
import org.json.JSONObject;

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
