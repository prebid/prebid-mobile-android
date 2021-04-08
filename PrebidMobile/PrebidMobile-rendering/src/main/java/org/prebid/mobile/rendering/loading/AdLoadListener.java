package org.prebid.mobile.rendering.loading;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.CreativeModelsMaker;

public interface AdLoadListener {
    void onCreativeModelReady(CreativeModelsMaker.Result result);

    void onFailedToLoadAd(AdException e, String vastLoaderIdentifier);
}
