package org.prebid.mobile.drprebid.model;

public class GeneralSettings extends SettingsItem {
    private AdFormat adFormat;
    private AdSize adSize;

    public GeneralSettings() {
    }

    public GeneralSettings(AdFormat adFormat, AdSize adSize) {
        this.adFormat = adFormat;
        this.adSize = adSize;
    }

    public AdFormat getAdFormat() {
        return adFormat;
    }

    public void setAdFormat(AdFormat adFormat) {
        this.adFormat = adFormat;
    }

    public AdSize getAdSize() {
        return adSize;
    }

    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }
}
