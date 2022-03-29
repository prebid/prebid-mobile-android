package org.prebid.mobile.drprebid.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PrebidServerValidationViewModel extends ViewModel {

    private final MutableLiveData<Boolean> bidRequestsSent;
    private final MutableLiveData<Integer> bidRequestSentCount;
    private final MutableLiveData<Boolean> bidResponsesReceived;
    private final MutableLiveData<Integer> bidResponseReceivedCount;
    private final MutableLiveData<Float> averageCpm;
    private final MutableLiveData<Long> averageResponseTime;

    public PrebidServerValidationViewModel() {
        bidRequestsSent = new MutableLiveData<>();
        bidRequestSentCount = new MutableLiveData<>();
        bidResponsesReceived = new MutableLiveData<>();
        bidResponseReceivedCount = new MutableLiveData<>();
        averageCpm = new MutableLiveData<>();
        averageResponseTime = new MutableLiveData<>();
    }

    public LiveData<Boolean> getBidRequestsSent() {
        return bidRequestsSent;
    }

    public void setBidRequestsSent(boolean requestsSent) {
        bidRequestsSent.setValue(requestsSent);
    }

    public LiveData<Integer> getBidRequestSentCount() {
        return bidRequestSentCount;
    }

    public void setBidRequestSentCount(int sentCount) {
        bidRequestSentCount.setValue(sentCount);
    }

    public LiveData<Integer> getBidResponsesReceived() {
        return bidResponseReceivedCount;
    }

    public void setBidResponseReceivedCount(int receivedCount) {
        bidResponseReceivedCount.setValue(receivedCount);
    }

    public LiveData<Float> getAverageCpm() {
        return averageCpm;
    }

    public void setAverageCpm(float averageCpm) {
        this.averageCpm.setValue(averageCpm);
    }

    public LiveData<Long> getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(long responseTime) {
        averageResponseTime.setValue(responseTime);
    }
}
