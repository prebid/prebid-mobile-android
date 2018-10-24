package org.prebid.mobile;

public interface OnCompleteListener {
    /**
     * This method will be called when PrebidMobile Mobile finishes attaching keywords to your ad object.
     * @param resultCode see object class definition for details
     */
    void onComplete(ResultCode resultCode);
}
