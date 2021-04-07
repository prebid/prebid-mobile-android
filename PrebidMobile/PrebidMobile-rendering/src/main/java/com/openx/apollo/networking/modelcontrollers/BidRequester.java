package com.openx.apollo.networking.modelcontrollers;

import android.content.Context;
import android.text.TextUtils;

import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.networking.ResponseHandler;
import com.openx.apollo.networking.parameters.AdRequestInput;
import com.openx.apollo.networking.urlBuilder.BidPathBuilder;
import com.openx.apollo.networking.urlBuilder.PathBuilderBase;

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
