package org.prebid.mobile.api.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.ResultCode;

import java.util.Map;

public class BidInfo {

    @NonNull
    private ResultCode resultCode;
    @Nullable
    private Map<String, String> targetingKeywords;
    @Nullable
    private String nativeCacheId;
    @Nullable
    private Integer expirationTimeSeconds;

    public BidInfo(
            @NonNull ResultCode resultCode,
            @Nullable Map<String, String> targetingKeywords
    ) {
        this.resultCode = resultCode;
        this.targetingKeywords = targetingKeywords;
    }

    public void setNativeResult(
            String nativeCacheId,
            Integer expirationTimeSeconds
    ) {
        this.nativeCacheId = nativeCacheId;
        this.expirationTimeSeconds = expirationTimeSeconds;
    }

    @NonNull
    public ResultCode getResultCode() {
        return resultCode;
    }

    @Nullable
    public Map<String, String> getTargetingKeywords() {
        return targetingKeywords;
    }

    @Nullable
    public String getNativeCacheId() {
        return nativeCacheId;
    }

    @Nullable
    public Integer getExpirationTimeSeconds() {
        return expirationTimeSeconds;
    }

}
