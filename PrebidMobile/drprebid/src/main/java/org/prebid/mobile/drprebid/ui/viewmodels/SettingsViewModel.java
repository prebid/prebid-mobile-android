package org.prebid.mobile.drprebid.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.PrebidServer;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<AdSize> mAdSize;
    private final MutableLiveData<Float> mBidPrice;
    private final MutableLiveData<String> mAdUnitId;
    private final MutableLiveData<PrebidServer> mPrebidServer;
    private final MutableLiveData<String> mAccountId;
    private final MutableLiveData<String> mConfigId;

    public SettingsViewModel() {
        mAdSize = new MutableLiveData<>();
        mBidPrice = new MutableLiveData<>();
        mAdUnitId = new MutableLiveData<>();
        mPrebidServer = new MutableLiveData<>();
        mAccountId = new MutableLiveData<>();
        mConfigId = new MutableLiveData<>();
    }

    public LiveData<AdSize> getAdSize() {
        return mAdSize;
    }

    public void setAdSize(AdSize adSize) {
        this.mAdSize.setValue(adSize);
    }

    public LiveData<Float> getBidPrice() {
        return mBidPrice;
    }

    public void setBidPrice(float bidPrice) {
        this.mBidPrice.setValue(bidPrice);
    }

    public LiveData<String> getAdUnitId() {
        return mAdUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.mAdUnitId.setValue(adUnitId);
    }

    public LiveData<PrebidServer> getPrebidServer() {
        return mPrebidServer;
    }

    public void setPrebidServer(PrebidServer prebidServer) {
        this.mPrebidServer.setValue(prebidServer);
    }

    public LiveData<String> getAccountId() {
        return mAccountId;
    }

    public void setAccountId(String accountId) {
        this.mAccountId.setValue(accountId);
    }

    public LiveData<String> getConfigId() {
        return mConfigId;
    }

    public void setConfigId(String configId) {
        this.mConfigId.setValue(configId);
    }
}
