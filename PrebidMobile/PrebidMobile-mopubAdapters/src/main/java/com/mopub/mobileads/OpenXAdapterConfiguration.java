package com.mopub.mobileads;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.BaseAdapterConfiguration;
import com.mopub.common.OnNetworkInitializationFinishedListener;
import com.mopub.common.Preconditions;

import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;

import java.util.Map;

public class OpenXAdapterConfiguration extends BaseAdapterConfiguration {
    private static final String TAG = OpenXAdapterConfiguration.class.getSimpleName();

    public final static String KEY_OPENX_CONFIGURATION = "openx_sdk_initialization_options";

    // 4-digit versioning scheme, of which the leftmost 3 digits correspond to the network SDK version,
    // and the last digit denotes the minor version number referring to an adapter release
    private static final String VERSION = "4.11.0.0";
    private static final String NETWORK_NAME = "openx";

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
