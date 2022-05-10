package org.prebid.mobile.drprebid.ui.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SdkValidationViewModel extends ViewModel {

    private final MutableLiveData<Boolean> adUnitRegistered;
    private final MutableLiveData<Boolean> prebidRequestSent;
    private final MutableLiveData<Boolean> prebidResponseReceived;
    private final MutableLiveData<Boolean> creativeContentCached;
    private final MutableLiveData<Boolean> adServerRequestSent;
    private final MutableLiveData<Boolean> creativeServed;

    public SdkValidationViewModel() {
        adUnitRegistered = new MutableLiveData<>();
        prebidRequestSent = new MutableLiveData<>();
        prebidResponseReceived = new MutableLiveData<>();
        creativeContentCached = new MutableLiveData<>();
        adServerRequestSent = new MutableLiveData<>();
        creativeServed = new MutableLiveData<>();
    }

    public LiveData<Boolean> getAdUnitRegistered() {
        return adUnitRegistered;
    }

    public void setAdUnitRegistered(boolean adUnitRegistered) {
        this.adUnitRegistered.setValue(adUnitRegistered);
    }

    public LiveData<Boolean> getPrebidRequestSent() {
        return prebidRequestSent;
    }

    public void setPrebidRequestSent(boolean prebidRequestSent) {
        this.prebidRequestSent.setValue(prebidRequestSent);
    }

    public LiveData<Boolean> getPrebidResponseReceived() {
        return prebidResponseReceived;
    }

    public void setPrebidResponseReceived(boolean prebidResponseReceived) {
        this.prebidResponseReceived.setValue(prebidResponseReceived);
    }

    public LiveData<Boolean> getCreativeContentCached() {
        return creativeContentCached;
    }

    public void setCreativeContentCached(boolean creativeContentCached) {
        this.creativeContentCached.setValue(creativeContentCached);
    }

    public LiveData<Boolean> getAdServerRequestSent() {
        return adServerRequestSent;
    }

    public void setAdServerRequestSent(boolean adServerRequestSent) {
        this.adServerRequestSent.setValue(adServerRequestSent);
    }

    public LiveData<Boolean> getCreativeServed() {
        return creativeServed;
    }

    public void setCreativeServed(@Nullable Boolean creativeServed) {
        this.creativeServed.setValue(creativeServed);
    }
}
