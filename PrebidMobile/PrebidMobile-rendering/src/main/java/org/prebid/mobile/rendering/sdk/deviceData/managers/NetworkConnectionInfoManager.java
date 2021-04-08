package org.prebid.mobile.rendering.sdk.deviceData.managers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.prebid.mobile.rendering.networking.parameters.UserParameters;
import org.prebid.mobile.rendering.sdk.BaseManager;

public final class NetworkConnectionInfoManager extends BaseManager implements ConnectionInfoManager {
    private ConnectivityManager mConnectivityManager;

    /**
     * @see ConnectionInfoManager
     */
    @Override
    public void init(Context context) {
        super.init(context);
        if (super.isInit() && getContext() != null) {
            mConnectivityManager = (ConnectivityManager) getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }

    /**
     * @see ConnectionInfoManager
     */
    @SuppressLint("MissingPermission")
    @Override
    public UserParameters.OXMConnectionType getConnectionType() {
        NetworkInfo info = null;
        UserParameters.OXMConnectionType result = UserParameters.OXMConnectionType.OFFLINE;
        if (isInit() && getContext() != null) {
            if (mConnectivityManager != null) {
                if (getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                    info = mConnectivityManager.getActiveNetworkInfo();
                }
            }
            if (info != null) {
                int netType = info.getType();
                if (info.isConnected()) {
                    boolean isMobile = netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager.TYPE_MOBILE_DUN || netType == ConnectivityManager.TYPE_MOBILE_HIPRI || netType == ConnectivityManager.TYPE_MOBILE_MMS || netType == ConnectivityManager.TYPE_MOBILE_SUPL;
                    result = isMobile
                             ? UserParameters.OXMConnectionType.CELL
                             : UserParameters.OXMConnectionType.WIFI;
                }
            }
        }
        return result;
    }
}