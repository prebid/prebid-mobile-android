package org.prebid.mobile.rendering.networking.modelcontrollers;

import android.content.Context;
import android.text.TextUtils;

import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.networking.urlBuilder.BidPathBuilder;
import org.prebid.mobile.rendering.networking.urlBuilder.PathBuilderBase;

public class BidRequester extends Requester {

    private static final String REQUEST_NAME = "bidrequest";

    public BidRequester(Context context, AdConfiguration config, AdRequestInput adRequestInput, ResponseHandler responseHandler) {
        super(context, config, adRequestInput, responseHandler);
        mRequestName = REQUEST_NAME;
    }

    @Override
    public void startAdRequest() {
        if (TextUtils.isEmpty (mAdConfiguration.getConfigId())) {
            mAdResponseCallBack.onError("No configuration id specified.", 0);
            return;
        }

        getAdId();
    }

    @Override
    protected PathBuilderBase getPathBuilder() {
        return new BidPathBuilder();
    }
}
