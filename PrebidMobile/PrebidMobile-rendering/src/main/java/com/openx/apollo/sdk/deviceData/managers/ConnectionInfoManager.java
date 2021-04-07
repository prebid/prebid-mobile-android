package com.openx.apollo.sdk.deviceData.managers;

import com.openx.apollo.networking.parameters.UserParameters;
import com.openx.apollo.sdk.ManagersResolver;

/**
 * Manager for retrieving network information.
 *
 * @see ManagersResolver
 */
public interface ConnectionInfoManager {

    /**
     * Get the active connection type.
     *
     * @return the active connection type
     */
    UserParameters.OXMConnectionType getConnectionType();
}
