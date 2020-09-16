package org.prebid.mobile.drprebid.ui.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    public void setCreativeServed(@Nullable Boolean creativeServed) {
        mCreativeServed.setValue(creativeServed);
    }
}
