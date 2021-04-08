package org.prebid.mobile.rendering.models;

/**
 * A wrapper to get all ad related details like transaction state, price(in future), etc
 */
public class AdDetails {

    private String mTransactionId;

    public String getTransactionId() {
        return mTransactionId;
    }

    public void setTransactionId(String transactionId) {
        mTransactionId = transactionId;
    }
}
