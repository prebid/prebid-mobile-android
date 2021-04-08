package org.prebid.mobile.rendering.bidding.data;

import org.json.JSONException;
import org.json.JSONObject;

public class AdSize {
    public int width;
    public int height;

    public AdSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdSize adSize = (AdSize) o;

        if (width != adSize.width) {
            return false;
        }
        return height == adSize.height;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("w", width);
            jsonObject.put("h", height);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
