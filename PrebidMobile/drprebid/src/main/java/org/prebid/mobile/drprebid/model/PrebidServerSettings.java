package org.prebid.mobile.drprebid.model;

public class PrebidServerSettings extends SettingsItem {
    private PrebidServer prebidServer;
    private String customPrebidServerUrl;
    private String accountId;
    private String configId;

    public PrebidServerSettings() {
    }

    public PrebidServerSettings(PrebidServer prebidServer, String customPrebidServerUrl, String accountId, String configId) {
        this.prebidServer = prebidServer;
        this.customPrebidServerUrl = customPrebidServerUrl;
        this.accountId = accountId;
        this.configId = configId;
    }

    public PrebidServer getPrebidServer() {
        return prebidServer;
    }

    public void setPrebidServer(PrebidServer prebidServer) {
        this.prebidServer = prebidServer;
    }

    public String getCustomPrebidServerUrl() {
        return customPrebidServerUrl;
    }

    public void setCustomPrebidServerUrl(String customPrebidServerUrl) {
        this.customPrebidServerUrl = customPrebidServerUrl;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }
}
