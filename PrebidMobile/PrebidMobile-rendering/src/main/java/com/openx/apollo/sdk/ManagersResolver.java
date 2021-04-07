package com.openx.apollo.sdk;

import android.content.Context;
import android.util.Log;

import com.openx.apollo.sdk.deviceData.managers.ConnectionInfoManager;
import com.openx.apollo.sdk.deviceData.managers.DeviceInfoImpl;
import com.openx.apollo.sdk.deviceData.managers.DeviceInfoManager;
import com.openx.apollo.sdk.deviceData.managers.LastKnownLocationInfoManager;
import com.openx.apollo.sdk.deviceData.managers.LocationInfoManager;
import com.openx.apollo.sdk.deviceData.managers.NetworkConnectionInfoManager;
import com.openx.apollo.sdk.deviceData.managers.UserConsentManager;
import com.openx.apollo.utils.helpers.Utils;
import com.openx.apollo.utils.logger.OXLog;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Map;

/**
 * Managers resolver supply ability to obtain a registered manager and use it
 * respectively.
 */
public class ManagersResolver {
    private static final String TAG = ManagersResolver.class.getSimpleName();
    private final Hashtable<ManagerType, Manager> mRegisteredManagers = new Hashtable<>();
    private WeakReference<Context> mContextReference;

    private void setContext(Context context) {
        mContextReference = new WeakReference<>(context);
    }

    public Context getContext() {
        if (mContextReference != null) {
            return mContextReference.get();
        }

        return null;
    }

    /**
     * The Enum ManagerType.
     */
    public enum ManagerType {
        /**
         * The device manager.
         */
        DEVICE_MANAGER,

        /**
         * The location manager.
         */
        LOCATION_MANAGER,

        /**
         * The network manager.
         */
        NETWORK_MANAGER,

        /**
         * The GDPR manager.
         */
        USER_CONSENT_MANAGER

    }

    private ManagersResolver() {
        // Deny public constructor
    }

    private static class ManagersResolverHolder {
        public static final ManagersResolver instance = new ManagersResolver();
    }

    /**
     * Gets the singleton instance of ManagersResolver.
     *
     * @return ManagersResolver
     */
    public static ManagersResolver getInstance() {
        return ManagersResolverHolder.instance;
    }

    /**
     * Obtains the manager by type.
     *
     * @param type the manager type
     * @return Manager
     */
    public Manager getManager(ManagerType type) {
        if (mRegisteredManagers.containsKey(type)) {
            return mRegisteredManagers.get(type);
        }
        return null;
    }

    /**
     * Obtains the device manager.
     *
     * @return DeviceManager
     */
    public DeviceInfoManager getDeviceManager() {
        return (DeviceInfoManager) getManager(ManagerType.DEVICE_MANAGER);
    }

    /**
     * Obtains the location manager.
     *
     * @return LocationManager
     */
    public LocationInfoManager getLocationManager() {
        return (LocationInfoManager) getManager(ManagerType.LOCATION_MANAGER);
    }

    /**
     * Obtains the network manager.
     *
     * @return NetworkManager
     */
    public ConnectionInfoManager getNetworkManager() {
        return (ConnectionInfoManager) getManager(ManagerType.NETWORK_MANAGER);
    }

    /**
     * Obtains the UserConsent manager.
     *
     * @return UserConsentManager
     */
    public UserConsentManager getUserConsentManager() {
        return (UserConsentManager) getManager(ManagerType.USER_CONSENT_MANAGER);
    }

    private boolean isReady(Context context) {
        return context == getContext();
    }

    private void registerManagers(final Context context) {
        Manager manager;
        setContext(context);
        //Try with application context or activity context
        //MOB-2205 [Research] on how we can eliminate activity context from Native ads.
        Utils.DENSITY = context.getResources().getDisplayMetrics().density;

        manager = new DeviceInfoImpl();
        manager.init(context);
        mRegisteredManagers.put(ManagerType.DEVICE_MANAGER, manager);

        manager = new LastKnownLocationInfoManager();
        manager.init(context);
        mRegisteredManagers.put(ManagerType.LOCATION_MANAGER, manager);

        manager = new NetworkConnectionInfoManager();
        manager.init(context);
        mRegisteredManagers.put(ManagerType.NETWORK_MANAGER, manager);

        manager = new UserConsentManager();
        manager.init(context);
        mRegisteredManagers.put(ManagerType.USER_CONSENT_MANAGER, manager);
    }

    /**
     * Prepare managers for current context.
     */
    public void prepare(Context context) {
        try {
            if (!isReady(context)) {
                dispose();
                registerManagers(context);
            }
        }
        catch (Exception e) {
            OXLog.error(TAG, "Failed to register managers: " + Log.getStackTraceString(e));
        }
        finally {
            ApolloSettings.increaseTaskCount();
        }
    }

    public void dispose() {
        for (Map.Entry<ManagerType, Manager> entry : mRegisteredManagers.entrySet()) {
            final Manager manager = entry.getValue();
            if (manager != null) {
                manager.dispose();
            }
        }
    }
}