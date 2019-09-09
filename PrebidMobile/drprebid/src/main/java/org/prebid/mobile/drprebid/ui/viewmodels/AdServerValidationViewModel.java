package org.prebid.mobile.drprebid.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class AdServerValidationViewModel extends ViewModel {
    private final MutableLiveData<Boolean> mRequestSent;
    private final MutableLiveData<Boolean> mCreativeServed;

    public AdServerValidationViewModel() {
        mRequestSent = new MutableLiveData<>();
        mCreativeServed = new MutableLiveData<>();
    }

    public LiveData<Boolean> getRequestSent() {
        return mRequestSent;
    }

    public void setRequestSent(boolean requestSent) {
        mRequestSent.setValue(requestSent);
    }

    public LiveData<Boolean> getCreativeServed() {
        return mCreativeServed;
    }

    public void setCreativeServed(boolean creativeServed) {
        mCreativeServed.setValue(creativeServed);
    }
}
