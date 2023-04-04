package org.prebid.mobile;


import androidx.annotation.Nullable;

import java.util.List;

/**
 * Describes an <a href="https://www.iab.com/wp-content/uploads/2016/03/OpenRTB-API-Specification-Version-2-5-FINAL.pdf">OpenRTB</a> banner object
 */
public class BannerParameters {

    /**
     * List of supported API frameworks for this impression. If an API is not explicitly listed, it is assumed not to be supported.
     */
    @Nullable
    private List<Signals.Api> api;

    @Nullable
    public List<Signals.Api> getApi() {
        return api;
    }

    public void setApi(@Nullable List<Signals.Api> api) {
        this.api = api;
    }

}
