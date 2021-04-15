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

package org.prebid.mobile.rendering.bidding.data.ntv;

import android.text.TextUtils;

import androidx.annotation.NonNull;

public class MediaData {
    @NonNull
    private final String mVastTag;

    public MediaData(
        @NonNull
            String vastTag) {
        mVastTag = vastTag;
    }

    public MediaData() {
        mVastTag = "";
    }

    /**
     * @return if present - vast tag, else - empty string.
     */
    @NonNull
    String getVastTag() {
        return mVastTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MediaData mediaData = (MediaData) o;

        return mVastTag.equals(mediaData.mVastTag);
    }

    @Override
    public int hashCode() {
        return mVastTag.hashCode();
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(mVastTag);
    }
}
