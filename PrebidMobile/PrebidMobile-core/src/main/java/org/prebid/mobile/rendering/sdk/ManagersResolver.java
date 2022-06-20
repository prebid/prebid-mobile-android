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

package org.prebid.mobile.rendering.sdk;

import android.content.Context;
import android.util.Log;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.sdk.deviceData.managers.*;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Map;

/**
 * Managers resolver supply ability to obtain a registered manager and use it
 * respectively.
 */
public class ManagersResolver {

    private static final String TAG = ManagersResolver.class.getSimpleName();
    private final Hashtable<ManagerType, Manager> registeredManagers = new Hashtable<>();
    private WeakReference<Context> contextReference;

    private void setContext(Context context) {
        contextReference = new WeakReference<>(context);
    }

    public Context getContext() {
        if (contextReference != null) {
            return contextReference.get();
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
        if (registeredManagers.containsKey(type)) {
            return registeredManagers.get(type);
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
     */
    @Nullable
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
        registeredManagers.put(ManagerType.DEVICE_MANAGER, manager);

        manager = new LastKnownLocationInfoManager();
        manager.init(context);
        registeredManagers.put(ManagerType.LOCATION_MANAGER, manager);

        manager = new NetworkConnectionInfoManager();
        manager.init(context);
        registeredManagers.put(ManagerType.NETWORK_MANAGER, manager);

        manager = new UserConsentManager();
        manager.init(context);
        registeredManagers.put(ManagerType.USER_CONSENT_MANAGER, manager);
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
            LogUtil.error(TAG, "Failed to register managers: " + Log.getStackTraceString(e));
        }
        finally {
            SdkInitializer.increaseTaskCount();
        }
    }

    public void dispose() {
        for (Map.Entry<ManagerType, Manager> entry : registeredManagers.entrySet()) {
            final Manager manager = entry.getValue();
            if (manager != null) {
                manager.dispose();
            }
        }
    }
}