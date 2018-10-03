package org.prebid.mobile;

public interface NewOnCompleteListener {
    /**
     * This method will be called when Prebid Mobile finishes attaching keywords to your ad object.
     * @param resultCode see object class definition for details
     */
    void onComplete(NewResultCode resultCode);
}
