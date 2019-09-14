package org.prebid.mobile.drprebid.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SdkValidationViewModel extends ViewModel {
    private final MutableLiveData<Boolean> mAdUnitRegistered;
    private final MutableLiveData<Boolean> mPrebidRequestSent;
    private final MutableLiveData<Boolean> mPrebidResponseReceived;
    private final MutableLiveData<Boolean> mCreativeContentCached;
    private final MutableLiveData<Boolean> mAdServerRequestSent;
    private final MutableLiveData<Boolean> mCreativeServed;

    public SdkValidationViewModel() {
        mAdUnitRegistered = new MutableLiveData<>();
        mPrebidRequestSent = new MutableLiveData<>();
        mPrebidResponseReceived = new MutableLiveData<>();
        mCreativeContentCached = new MutableLiveData<>();
        mAdServerRequestSent = new MutableLiveData<>();
        mCreativeServed = new MutableLiveData<>();
    }

    public LiveData<Boolean> getAdUnitRegistered() {
        return mAdUnitRegistered;
    }

    public void setAdUnitRegistered(boolean adUnitRegistered) {
        mAdUnitRegistered.setValue(adUnitRegistered);
    }

    public LiveData<Boolean> getPrebidRequestSent() {
        return mPrebidRequestSent;
    }

    public void setPrebidRequestSent(boolean prebidRequestSent) {
        mPrebidRequestSent.setValue(prebidRequestSent);
    }

    public LiveData<Boolean> getPrebidResponseReceived() {
        return mPrebidResponseReceived;
    }

    public void setPrebidResponseReceived(boolean prebidResponseReceived) {
        mPrebidResponseReceived.setValue(prebidResponseReceived);
    }

    public LiveData<Boolean> getCreativeContentCached() {
        return mCreativeContentCached;
    }

    public void setCreativeContentCached(boolean creativeContentCached) {
        mCreativeContentCached.setValue(creativeContentCached);
    }

    public LiveData<Boolean> getAdServerRequestSent() {
        return mAdServerRequestSent;
    }

    public void setAdServerRequestSent(boolean adServerRequestSent) {
        mAdServerRequestSent.setValue(adServerRequestSent);
    }

    public LiveData<Boolean> getCreativeServed() {
        return mCreativeServed;
    }

    public void setCreativeServed(boolean creativeServed) {
        mCreativeServed.setValue(creativeServed);
    }
}
