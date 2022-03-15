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
