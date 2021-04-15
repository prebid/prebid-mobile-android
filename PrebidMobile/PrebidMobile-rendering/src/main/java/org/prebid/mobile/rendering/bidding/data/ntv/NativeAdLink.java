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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

import java.util.List;

public class NativeAdLink {
    @NonNull
    private final String mUrl;
    @NonNull
    private final List<String> mClickTrackers;
    @NonNull
    private final String mFallback;
    @Nullable
    private final Ext mExt;

    NativeAdLink(
        @NonNull
            String url,
        @NonNull
            List<String> clickTrackers,
        @NonNull
            String fallback,
        @Nullable
            Ext ext) {
        mUrl = url;
        mClickTrackers = clickTrackers;
        mFallback = fallback;
        mExt = ext;
    }

    /**
     * @return if present - link url, empty string. otherwise
     */
    @NonNull
    public String getUrl() {
        return mUrl;
    }

    /**
     * @return list containing clickTrackers or empty list.
     */
    @NonNull
    public List<String> getClickTrackers() {
        return mClickTrackers;
    }

    /**
     * @return fallback url or empty string.
     */
    @NonNull
    public String getFallback() {
        return mFallback;
    }

    /**
     * @return if present - link level ext, else - null.
     */
    @Nullable
    public Ext getExt() {
        return mExt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NativeAdLink that = (NativeAdLink) o;

        if (!mUrl.equals(that.mUrl)) {
            return false;
        }
        if (!mClickTrackers.equals(that.mClickTrackers)) {
            return false;
        }
        if (!mFallback.equals(that.mFallback)) {
            return false;
        }
        return mExt != null ? mExt.equals(that.mExt) : that.mExt == null;
    }

    @Override
    public int hashCode() {
        int result = mUrl.hashCode();
        result = 31 * result + mClickTrackers.hashCode();
        result = 31 * result + mFallback.hashCode();
        result = 31 * result + (mExt != null ? mExt.hashCode() : 0);
        return result;
    }

    public boolean isDeeplink() {
        if (mUrl == null) {
            return false;
        }
        return !(mUrl.startsWith("http") || mUrl.startsWith("https"));
    }
}
