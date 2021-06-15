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

package org.prebid.mobile.rendering.models.internal;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.parser.AdResponseParserBase;
import org.prebid.mobile.rendering.utils.helpers.Utils;

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
