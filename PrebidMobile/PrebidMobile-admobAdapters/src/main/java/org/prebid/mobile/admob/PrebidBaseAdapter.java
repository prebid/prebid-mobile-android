package org.prebid.mobile.admob;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;

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
            PrebidMobile.initializeSdk(context, new SdkInitializationListener() {
                @Override
                public void onSdkInit() {
                    callback.onInitializationSucceeded();
                }

                @Override
                public void onSdkFailedToInit(InitError error) {
                    callback.onInitializationFailed(error.getError());
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
