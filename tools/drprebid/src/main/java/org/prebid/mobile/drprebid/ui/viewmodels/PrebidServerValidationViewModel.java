package org.prebid.mobile.drprebid.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class PrebidServerValidationViewModel extends ViewModel {
    private final MutableLiveData<Boolean> mBidRequestsSent;
    private final MutableLiveData<Integer> mBidRequestSentCount;
    private final MutableLiveData<Boolean> mBidResponsesReceived;
    private final MutableLiveData<Integer> mBidResponseReceivedCount;
    private final MutableLiveData<Float> mAverageCpm;
    private final MutableLiveData<Long> mAverageResponseTime;

    public PrebidServerValidationViewModel() {
        mBidRequestsSent = new MutableLiveData<>();
        mBidRequestSentCount = new MutableLiveData<>();
        mBidResponsesReceived = new MutableLiveData<>();
        mBidResponseReceivedCount = new MutableLiveData<>();
        mAverageCpm = new MutableLiveData<>();
        mAverageResponseTime = new MutableLiveData<>();
    }

    public LiveData<Boolean> getBidRequestsSent() {
        return mBidRequestsSent;
    }

    public void setBidRequestsSent(boolean requestsSent) {
        mBidRequestsSent.setValue(requestsSent);
    }

    public LiveData<Integer> getBidRequestSentCount() {
        return mBidRequestSentCount;
    }

    public void setBidRequestSentCount(int sentCount) {
        mBidRequestSentCount.setValue(sentCount);
    }

    public LiveData<Integer> getBidResponsesReceived() {
        return mBidResponseReceivedCount;
    }

    public void setBidResponseReceivedCount(int receivedCount) {
        mBidResponseReceivedCount.setValue(receivedCount);
    }

    public LiveData<Float> getAverageCpm() {
        return mAverageCpm;
    }

    public void setAverageCpm(float averageCpm) {
        mAverageCpm.setValue(averageCpm);
    }

    public LiveData<Long> getAverageResponseTime() {
        return mAverageResponseTime;
    }

    public void setAverageResponseTime(long responseTime) {
        mAverageResponseTime.setValue(responseTime);
    }
}
