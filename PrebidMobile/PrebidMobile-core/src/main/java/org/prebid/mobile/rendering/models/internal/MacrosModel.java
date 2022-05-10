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

import androidx.annotation.NonNull;

public class MacrosModel {
    public static final String MACROS_AUCTION_PRICE = "\\$\\{AUCTION_PRICE\\}";
    public static final String MACROS_AUCTION_PRICE_BASE_64 = "\\$\\{AUCTION_PRICE:B64\\}";

    private static final String MACROS_DEFAULT_VALUE = "\\\\\"\\\\\""; //String representation of "\"\""

    private final String replaceValue;

    public MacrosModel(String replaceValue) {
        this.replaceValue = replaceValue;
    }

    @NonNull
    public String getReplaceValue() {
        return replaceValue == null ? MACROS_DEFAULT_VALUE : replaceValue;
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

        return replaceValue != null ? replaceValue.equals(that.replaceValue) : that.replaceValue == null;
    }

    @Override
    public int hashCode() {
        return replaceValue != null ? replaceValue.hashCode() : 0;
    }
}
