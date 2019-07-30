package org.prebid.mobile;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;

public class NativeAdUnit extends AdUnit{
    public enum NATIVE_REQUEST_VERSION{
        VERSION_1_1,
        VERSION_1_2
    }

    public enum NATIVE_REQUEST_ASSET{
        TITLE,
        IMAGE,
        ICON,
        DATA
    }
    NativeAdUnit(@NonNull String configId) {
        super(configId, AdType.NATIVE);
    }
    NATIVE_REQUEST_VERSION request_version = NATIVE_REQUEST_VERSION.VERSION_1_1;

    HashSet<NATIVE_REQUEST_ASSET> asssets = new HashSet<>();

    public void setNativeRequestAPIVersion(NATIVE_REQUEST_VERSION version){
        this.request_version = version;
    }

    public void addNativeRequestAsset(NATIVE_REQUEST_ASSET asset){
        this.asssets.add(asset);
    }

}
