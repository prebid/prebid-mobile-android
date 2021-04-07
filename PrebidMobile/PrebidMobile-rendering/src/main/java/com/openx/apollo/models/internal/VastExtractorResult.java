package com.openx.apollo.models.internal;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.parser.AdResponseParserBase;
import com.openx.apollo.utils.helpers.Utils;

import java.util.Arrays;

public class VastExtractorResult {
    private final String mLoadIdentifier = String.valueOf(Utils.generateRandomInt());
    private AdException mAdException;
    private AdResponseParserBase[] mVastResponseParserArray;

    public VastExtractorResult(AdResponseParserBase[] vastResponseParserArray) {
        mVastResponseParserArray = vastResponseParserArray;
    }

    public VastExtractorResult(AdException adException) {
        mAdException = adException;
    }

    public AdException getAdException() {
        return mAdException;
    }

    public String getLoadIdentifier() {
        return mLoadIdentifier;
    }

    public AdResponseParserBase[] getVastResponseParserArray() {
        return mVastResponseParserArray;
    }

    public boolean hasException() {
        return mAdException != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VastExtractorResult that = (VastExtractorResult) o;

        return mLoadIdentifier != null
               ? mLoadIdentifier.equals(that.mLoadIdentifier)
               : that.mLoadIdentifier == null;
    }

    @Override
    public int hashCode() {
        int result = mLoadIdentifier != null ? mLoadIdentifier.hashCode() : 0;
        result = 31 * result + (mAdException != null ? mAdException.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(mVastResponseParserArray);
        return result;
    }
}
