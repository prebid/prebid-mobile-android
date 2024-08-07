package org.prebid.mobile;

import androidx.annotation.NonNull;

/**
 * Response object for native title asset.
 */
public class NativeTitle {

    private final String text;

    public NativeTitle(@NonNull String text) {
        this.text = text;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NativeTitle that = (NativeTitle) object;
        return text.equals(that.text);
    }

}
