package org.prebid.mobile.rendering.sdk.deviceData.managers;

import org.prebid.mobile.rendering.networking.parameters.UserParameters;
import org.prebid.mobile.rendering.sdk.ManagersResolver;

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
