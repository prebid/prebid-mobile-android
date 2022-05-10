package org.prebid.mobile.drprebid.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.PrebidServer;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<AdSize> adSize;
    private final MutableLiveData<Float> bidPrice;
    private final MutableLiveData<String> adUnitId;
    private final MutableLiveData<PrebidServer> prebidServer;
    private final MutableLiveData<String> accountId;
    private final MutableLiveData<String> configId;

    public SettingsViewModel() {
        adSize = new MutableLiveData<>();
        bidPrice = new MutableLiveData<>();
        adUnitId = new MutableLiveData<>();
        prebidServer = new MutableLiveData<>();
        accountId = new MutableLiveData<>();
        configId = new MutableLiveData<>();
    }

    public LiveData<AdSize> getAdSize() {
        return adSize;
    }

    public void setAdSize(AdSize adSize) {
        this.adSize.setValue(adSize);
    }

    public LiveData<Float> getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(float bidPrice) {
        this.bidPrice.setValue(bidPrice);
    }

    public LiveData<String> getAdUnitId() {
        return adUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.adUnitId.setValue(adUnitId);
    }

    public LiveData<PrebidServer> getPrebidServer() {
        return prebidServer;
    }

    public void setPrebidServer(PrebidServer prebidServer) {
        this.prebidServer.setValue(prebidServer);
    }

    public LiveData<String> getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId.setValue(accountId);
    }

    public LiveData<String> getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId.setValue(configId);
    }
}
