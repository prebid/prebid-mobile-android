package org.prebid.mobile.drprebid.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.PrebidServer;

public class SettingsViewModel extends ViewModel {
    private MutableLiveData<AdSize> mAdSize;
    private MutableLiveData<Float> mBidPrice;
    private MutableLiveData<String> mAdUnitId;
    private MutableLiveData<PrebidServer> mPrebidServer;
    private MutableLiveData<String> mAccountId;
    private MutableLiveData<String> mConfigId;

    public LiveData<AdSize> getAdSize() {
        if (mAdSize == null) {
            mAdSize = new MutableLiveData<>();
        }
        return mAdSize;
    }

    public void setAdSize(AdSize adSize) {
        this.mAdSize.setValue(adSize);
    }

    public LiveData<Float> getBidPrice() {
        if (mBidPrice == null) {
            mBidPrice = new MutableLiveData<>();
        }
        return mBidPrice;
    }

    public void setBidPrice(float bidPrice) {
        this.mBidPrice.setValue(bidPrice);
    }

    public LiveData<String> getAdUnitId() {
        if (mAdUnitId == null) {
            mAdUnitId = new MutableLiveData<>();
        }
        return mAdUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.mAdUnitId.setValue(adUnitId);
    }

    public LiveData<PrebidServer> getPrebidServer() {
        if (mPrebidServer == null) {
            mPrebidServer = new MutableLiveData<>();
        }
        return mPrebidServer;
    }

    public void setPrebidServer(PrebidServer prebidServer) {
        this.mPrebidServer.setValue(prebidServer);
    }

    public LiveData<String> getAccountId() {
        if (mAccountId == null) {
            mAccountId = new MutableLiveData<>();
        }
        return mAccountId;
    }

    public void setAccountId(String accountId) {
        this.mAccountId.setValue(accountId);
    }

    public LiveData<String> getConfigId() {
        if (mConfigId == null) {
            mConfigId = new MutableLiveData<>();
        }
        return mConfigId;
    }

    public void setConfigId(String configId) {
        this.mConfigId.setValue(configId);
    }
}
