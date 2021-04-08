package org.prebid.mobile.rendering.sdk.deviceData.managers;

import org.prebid.mobile.rendering.sdk.Manager;
import org.prebid.mobile.rendering.sdk.ManagersResolver;

/**
 * Manager for retrieving location information.
 *
 * @see ManagersResolver
 */
public interface LocationInfoManager extends Manager {
    /**
     * Get the latitude.
     *
     * @return the latitude
     */
    Double getLatitude();

    /**
     * Get the longitude.
     *
     * @return the longitude
     */
    Double getLongitude();

    Float getAccuracy();

    Long getElapsedSeconds();

    boolean isLocationAvailable();

    void resetLocation();
}
