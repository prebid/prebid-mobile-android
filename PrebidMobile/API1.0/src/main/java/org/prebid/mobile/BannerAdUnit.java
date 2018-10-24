package org.prebid.mobile;

import android.support.annotation.NonNull;

import java.util.HashSet;

public class BannerAdUnit extends AdUnit {
    private HashSet<AdSize> sizes;


    public BannerAdUnit(@NonNull String configId, int width, int height) {
        super(configId, AdType.BANNER);
        this.sizes = new HashSet<>();
        this.sizes.add(new AdSize(width, height));
    }

    public void addAdditionalSize(int width, int height) {
        sizes.add(new AdSize(width, height));
    }

    HashSet<AdSize> getSizes() {
        return this.sizes;
    }

}
