package com.openx.apollo.loading;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.CreativeModelsMaker;

public interface AdLoadListener {
    void onCreativeModelReady(CreativeModelsMaker.Result result);

    void onFailedToLoadAd(AdException e, String vastLoaderIdentifier);
}
