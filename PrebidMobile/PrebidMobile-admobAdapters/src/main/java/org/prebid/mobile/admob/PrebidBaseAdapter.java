package org.prebid.mobile.admob;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;

import java.util.HashMap;
import java.util.List;

public abstract class PrebidBaseAdapter extends Adapter {

    private final VersionInfo prebidVersion = getPrebidVersion();
    protected static final String TAG = "PrebidAdapter";

    @Override
    public void initialize(
            @NonNull Context context,
            @NonNull InitializationCompleteCallback callback,
            @NonNull List<MediationConfiguration> list
    ) {
        if (PrebidMobile.isSdkInitialized()) {
            callback.onInitializationSucceeded();
        } else {
            PrebidMobile.initializeSdk(context, status -> {
                if (status == InitializationStatus.SUCCEEDED) {
                    callback.onInitializationSucceeded();
                } else {
                    String description = status.getDescription() != null ? status.getDescription() : "";
                    callback.onInitializationFailed(description);
                }
            });
        }
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        return prebidVersion;
    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        return prebidVersion;
    }

    @Nullable
    protected String getResponseIdAndCheckParameters(
            @NonNull MediationAdConfiguration configuration,
            @NonNull String extraResponseIdKey,
            @NonNull OnLoadFailure onLoadFailure
    ) {
        Bundle serverParameters = configuration.getServerParameters();
        String adMobParameters = serverParameters.getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);

        String responseId = configuration.getMediationExtras().getString(extraResponseIdKey);
        if (responseId == null) {
            onLoadFailure.run(AdErrors.emptyResponseId());
            return null;
        }

        HashMap<String, String> prebidParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (prebidParameters == null) {
            onLoadFailure.run(AdErrors.emptyPrebidKeywords());
            return null;
        }

        if (!ParametersMatcher.doParametersMatch(adMobParameters, prebidParameters)) {
            onLoadFailure.run(AdErrors.notMatchedParameters());
            return null;
        }
        LogUtil.verbose(TAG, "Parameters are matched! (" + serverParameters + ")");

        return responseId;
    }

    private VersionInfo getPrebidVersion() {
        int[] versions = new int[]{0, 0, 0};
        try {
            String[] versionStrings = PrebidMobile.SDK_VERSION.split("\\.");
            if (versionStrings.length >= 3) {
                for (int i = 0; i < 3; i++) {
                    versions[i] = Integer.parseInt(versionStrings[i]);
                }
            }
        } catch (NumberFormatException ignore) {
        }
        return new VersionInfo(versions[0], versions[1], versions[2]);
    }

}
