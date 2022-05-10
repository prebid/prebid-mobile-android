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

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.parser.AdResponseParserBase;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.Arrays;

public class VastExtractorResult {

    private final String loadIdentifier = String.valueOf(Utils.generateRandomInt());
    private AdException adException;
    private AdResponseParserBase[] vastResponseParserArray;

    public VastExtractorResult(AdResponseParserBase[] vastResponseParserArray) {
        this.vastResponseParserArray = vastResponseParserArray;
    }

    public VastExtractorResult(AdException adException) {
        this.adException = adException;
    }

    public AdException getAdException() {
        return adException;
    }

    public String getLoadIdentifier() {
        return loadIdentifier;
    }

    public AdResponseParserBase[] getVastResponseParserArray() {
        return vastResponseParserArray;
    }

    public boolean hasException() {
        return adException != null;
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

        return loadIdentifier != null ? loadIdentifier.equals(that.loadIdentifier) : that.loadIdentifier == null;
    }

    @Override
    public int hashCode() {
        int result = loadIdentifier != null ? loadIdentifier.hashCode() : 0;
        result = 31 * result + (adException != null ? adException.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(vastResponseParserArray);
        return result;
    }
}
