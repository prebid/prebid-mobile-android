package org.prebid.mobile.drprebid.model;

public class AdServerSettings extends SettingsItem {
    private AdServer adServer;
    private float bidPrice;
    private String adUnitId;

    public AdServerSettings() {
    }

    public AdServerSettings(AdServer adServer, float bidPrice, String adUnitId) {
        this.adServer = adServer;
        this.bidPrice = bidPrice;
        this.adUnitId = adUnitId;
    }

    public AdServer getAdServer() {
        return adServer;
    }

    public void setAdServer(AdServer adServer) {
        this.adServer = adServer;
    }

    public float getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(float bidPrice) {
        this.bidPrice = bidPrice;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }
}
