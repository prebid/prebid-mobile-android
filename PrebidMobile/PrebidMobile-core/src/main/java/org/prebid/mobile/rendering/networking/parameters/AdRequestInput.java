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

package org.prebid.mobile.rendering.networking.parameters;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AdRequestInput {

    private static final String TAG = AdRequestInput.class.getSimpleName();

    private BidRequest bidRequest;

    public AdRequestInput() {
        bidRequest = new BidRequest();
    }

    public AdRequestInput getDeepCopy() {
        AdRequestInput newAdRequestInput = new AdRequestInput();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(bidRequest);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            newAdRequestInput.bidRequest = (BidRequest) ois.readObject();
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed to make deep copy of bid request");
            return null;
        }

        return newAdRequestInput;
    }

    public BidRequest getBidRequest() {
        return bidRequest;
    }

    public void setBidRequest(BidRequest bidRequest) {
        this.bidRequest = bidRequest;
    }
}
