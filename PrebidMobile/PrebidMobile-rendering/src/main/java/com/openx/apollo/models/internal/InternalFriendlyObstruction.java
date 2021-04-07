package com.openx.apollo.models.internal;

import android.view.View;

import java.lang.ref.WeakReference;

public class InternalFriendlyObstruction {
    private WeakReference<View> mViewWeakReference;
    private InternalFriendlyObstruction.Purpose mFriendlyObstructionPurpose;
    private String mDetailedDescription;

    public InternalFriendlyObstruction(View view, InternalFriendlyObstruction.Purpose friendlyObstructionPurpose, String detailedDescription) {
        mViewWeakReference = new WeakReference<>(view);
        mFriendlyObstructionPurpose = friendlyObstructionPurpose;
        mDetailedDescription = detailedDescription;
    }

    public View getView() {
        return mViewWeakReference.get();
    }

    public InternalFriendlyObstruction.Purpose getPurpose() {
        return mFriendlyObstructionPurpose;
    }

    public String getDetailedDescription() {
        return mDetailedDescription;
    }

    public enum Purpose {
        CLOSE_AD,
        OTHER,
        VIDEO_CONTROLS
    }
}
