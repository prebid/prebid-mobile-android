package org.prebid.mobile.rendering.models.internal;

import androidx.annotation.NonNull;

public class MacrosModel {
    public static final String MACROS_AUCTION_PRICE = "\\$\\{AUCTION_PRICE\\}";
    public static final String MACROS_AUCTION_PRICE_BASE_64 = "\\$\\{AUCTION_PRICE:B64\\}";

    private static final String MACROS_DEFAULT_VALUE = "\\\\\"\\\\\""; //String representation of "\"\""

    private final String mReplaceValue;

    public MacrosModel(String replaceValue) {
        mReplaceValue = replaceValue;
    }

    @NonNull
    public String getReplaceValue() {
        return mReplaceValue == null ? MACROS_DEFAULT_VALUE : mReplaceValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MacrosModel that = (MacrosModel) o;

        return mReplaceValue != null
               ? mReplaceValue.equals(that.mReplaceValue)
               : that.mReplaceValue == null;
    }

    @Override
    public int hashCode() {
        return mReplaceValue != null ? mReplaceValue.hashCode() : 0;
    }
}
