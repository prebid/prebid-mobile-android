package org.prebid.mobile;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class NewBannerAdUnit extends NewAdUnit {
    private ArrayList<AdSize> sizes;

    public NewBannerAdUnit(@NonNull String configId) {
        super(configId, AdType.BANNER);
        this.sizes = new ArrayList<>();
    }

    // region BannerAdUnit only methods
    public void addSize(int width, int height) {
        sizes.add(new AdSize(width, height));
    }

    ArrayList<AdSize> getSizes() {
        return this.sizes;
    }

}
