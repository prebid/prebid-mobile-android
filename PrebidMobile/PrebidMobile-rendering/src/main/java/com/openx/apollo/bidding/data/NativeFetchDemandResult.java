package com.openx.apollo.bidding.data;

import java.util.Map;

public class NativeFetchDemandResult {
    private final FetchDemandResult mFetchDemandResult;
    private Map<String, String> mKeyWordsMap;

    public NativeFetchDemandResult(FetchDemandResult fetchDemandResult) {
        mFetchDemandResult = fetchDemandResult;
    }

    public FetchDemandResult getFetchDemandResult() {
        return mFetchDemandResult;
    }

    public Map<String, String> getKeyWordsMap() {
        return mKeyWordsMap;
    }

    public void setKeyWordsMap(Map<String, String> keyWordsMap) {
        mKeyWordsMap = keyWordsMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NativeFetchDemandResult that = (NativeFetchDemandResult) o;

        if (mFetchDemandResult != that.mFetchDemandResult) {
            return false;
        }
        return mKeyWordsMap != null
               ? mKeyWordsMap.equals(that.mKeyWordsMap)
               : that.mKeyWordsMap == null;
    }

    @Override
    public int hashCode() {
        int result = mFetchDemandResult != null ? mFetchDemandResult.hashCode() : 0;
        result = 31 * result + (mKeyWordsMap != null ? mKeyWordsMap.hashCode() : 0);
        return result;
    }
}
