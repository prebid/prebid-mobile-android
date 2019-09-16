/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile.addendum;

class PbError {

    private final String domain = "com.prebidmobile.android";
    private final int code;
    private final String description;

    PbError(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public final String getDomain() {
        return domain;
    }

    public final int getCode() {
        return code;
    }

    public final String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PbError)) return false;

        PbError pbError = (PbError) o;

        return code == pbError.code;
    }

    @Override
    public String toString() {
        return "PbError{" +
                "domain='" + domain + '\'' +
                ", code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
