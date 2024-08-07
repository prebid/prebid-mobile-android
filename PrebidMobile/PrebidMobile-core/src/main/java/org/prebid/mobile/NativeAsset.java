package org.prebid.mobile;

import org.json.JSONObject;

/**
 * Base class for requesting native assets.
 */
public abstract class NativeAsset {

    enum REQUEST_ASSET {
        TITLE,
        IMAGE,
        DATA
    }

    private REQUEST_ASSET type;

    NativeAsset(REQUEST_ASSET type) {
        this.type = type;
    }

    public REQUEST_ASSET getType() {
        return type;
    }

    public abstract JSONObject getJsonObject(int idCount);

}
