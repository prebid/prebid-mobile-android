package org.prebid.mobile;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class BannerAdUnit extends AdUnit {
    private ArrayList<AdSize> sizes;


    public BannerAdUnit(@NonNull String configId) {
        super(configId, AdType.BANNER);
        this.sizes = new ArrayList<>();
    }

    // region BannerAdUnit only methods
    public void addSize(int width, int height) {
        sizes.add(new AdSize(width, height));
    }

    public void setAutoRefreshPeriodMillis(int periodMillis) {
        if (periodMillis < 30000) {
            return;
        }
        this.periodMillis = periodMillis;
        if (fetcher != null) {
            fetcher.setPeriodMillis(periodMillis);
        }
    }

    public void stopAutoRefersh() {
        if (fetcher != null) {
            fetcher.destroy();
            fetcher = null;
        }
    }

    // endregion
    ArrayList<AdSize> getSizes() {
        return this.sizes;
    }

}
