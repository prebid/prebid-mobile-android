package com.openx.apollo.sdk.deviceData.managers;

import com.openx.apollo.sdk.Manager;
import com.openx.apollo.sdk.ManagersResolver;

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
