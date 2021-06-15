/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.bidding.data;

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
