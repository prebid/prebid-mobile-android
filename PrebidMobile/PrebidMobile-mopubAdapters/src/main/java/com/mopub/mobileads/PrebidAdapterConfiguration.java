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

package com.mopub.mobileads;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.BaseAdapterConfiguration;
import com.mopub.common.OnNetworkInitializationFinishedListener;
import com.mopub.common.Preconditions;

import org.prebid.mobile.rendering.BuildConfig;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;

import java.util.Map;

public class PrebidAdapterConfiguration extends BaseAdapterConfiguration {
    private static final String TAG = PrebidAdapterConfiguration.class.getSimpleName();

    // 4-digit versioning scheme, of which the leftmost 3 digits correspond to the network SDK version,
    // and the last digit denotes the minor version number referring to an adapter release
    private static final String VERSION = BuildConfig.VERSION;
    private static final String NETWORK_NAME = "prebid";

    @NonNull
    @Override
    public String getAdapterVersion() {
        return VERSION;
    }

    @Nullable
    @Override
    public String getBiddingToken(
        @NonNull
            Context context) {
        return null;
    }

    @NonNull
    @Override
    public String getMoPubNetworkName() {
        return NETWORK_NAME;
    }

    @NonNull
    @Override
    public String getNetworkSdkVersion() {
        return PrebidRenderingSettings.SDK_VERSION;
    }

    @Override
    public void initializeNetwork(
        @NonNull
        final
        Context context,
        @Nullable
            Map<String, String> configuration,
        @NonNull
        final
        OnNetworkInitializationFinishedListener listener) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(listener);

        listener.onNetworkInitializationFinished(this.getClass(), MoPubErrorCode.ADAPTER_INITIALIZATION_SUCCESS);
    }
}
