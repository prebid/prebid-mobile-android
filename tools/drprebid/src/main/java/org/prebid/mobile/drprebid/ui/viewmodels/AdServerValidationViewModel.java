package org.prebid.mobile.drprebid.ui.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdServerValidationViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requestSent;
    private final MutableLiveData<Boolean> creativeServed;

    public AdServerValidationViewModel() {
        requestSent = new MutableLiveData<>();
        creativeServed = new MutableLiveData<>();
    }

    public LiveData<Boolean> getRequestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent.setValue(requestSent);
    }

    public LiveData<Boolean> getCreativeServed() {
        return creativeServed;
    }

    public void setCreativeServed(@Nullable Boolean creativeServed) {
        this.creativeServed.setValue(creativeServed);
    }
}
