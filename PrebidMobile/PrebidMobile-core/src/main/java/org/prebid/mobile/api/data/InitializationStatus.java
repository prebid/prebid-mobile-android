package org.prebid.mobile.api.data;

import androidx.annotation.Nullable;

public enum InitializationStatus {

    SUCCEEDED,
    SERVER_STATUS_WARNING,
    FAILED;

    @Nullable
    private String description;

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

}
