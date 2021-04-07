package com.openx.apollo.networking.parameters;

import com.openx.apollo.models.openrtb.BidRequest;
import com.openx.apollo.utils.logger.OXLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AdRequestInput {

    private static final String TAG = AdRequestInput.class.getSimpleName();

    private BidRequest mBidRequest;

    public AdRequestInput() {
        mBidRequest = new BidRequest();
    }

    public AdRequestInput getDeepCopy() {
        AdRequestInput newAdRequestInput = new AdRequestInput();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(mBidRequest);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            newAdRequestInput.mBidRequest = (BidRequest) ois.readObject();
        }
        catch (Exception e) {
            OXLog.error(TAG, "Failed to make deep copy of bid request");
            return null;
        }

        return newAdRequestInput;
    }

    public BidRequest getBidRequest() {
        return mBidRequest;
    }

    public void setBidRequest(BidRequest bidRequest) {
        mBidRequest = bidRequest;
    }
}
