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

package org.prebid.mobile.rendering.sdk.deviceData.managers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;

import org.prebid.mobile.rendering.sdk.BaseManager;
import org.prebid.mobile.LogUtil;

public final class LastKnownLocationInfoManager extends BaseManager implements LocationInfoManager {

    private android.location.LocationManager locManager;
    private Location location;
    private static final String TAG = LastKnownLocationInfoManager.class.getSimpleName();

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public LastKnownLocationInfoManager(Context context) {
        super(context);

        if (getContext() != null) {
            resetLocation();
        }
    }

    @Override
    public void resetLocation() {
        if (getContext() == null) {
            return;
        }

        locManager = (android.location.LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locManager == null) {
            return;
        }

        // GPS_PROVIDER requires ACCESS_FINE_LOCATION; NETWORK_PROVIDER is satisfied by
        // ACCESS_COARSE_LOCATION. Since Android 12 the user can grant approximate
        // location on its own, so each provider is asked for separately and only when
        // the permission it actually needs is held.
        Location gpsLastKnownLocation = isFineLocationPermissionGranted()
                ? lastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                : null;
        Location ntwLastKnownLocation = isLocationPermissionGranted()
                ? lastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                : null;

        if (gpsLastKnownLocation != null) {
            location = gpsLastKnownLocation;

            if (ntwLastKnownLocation != null && isBetterLocation(ntwLastKnownLocation, location)) {
                location = ntwLastKnownLocation;
            }
        } else if (ntwLastKnownLocation != null) {
            location = ntwLastKnownLocation;
        }
    }

    /**
     * Reads a single provider in isolation. A provider that the platform refuses or
     * that does not exist on this device must not stop the other provider from being
     * read.
     */
    @SuppressLint("MissingPermission")
    private Location lastKnownLocation(String provider) {
        try {
            return locManager.getLastKnownLocation(provider);
        } catch (SecurityException exception) {
            LogUtil.warning(TAG, "No permission to read the last known location from " + provider + ".");
        } catch (IllegalArgumentException exception) {
            LogUtil.warning(TAG, "Location provider " + provider + " does not exist on this device.");
        }
        return null;
    }

    /**
     * Determine whether one Location reading is better than the current
     * Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new
     *                            one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        }
        else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        }
        else if (isNewer && !isLessAccurate) {
            return true;
        }
        else {
            return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
        }
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * @see LocationListener
     */
    @Override
    public Double getLatitude() {
        return location != null ? location.getLatitude() : null;
    }

    /**
     * @see LocationListener
     */
    @Override
    public Double getLongitude() {
        return location != null ? location.getLongitude() : null;
    }

    @Override
    public Float getAccuracy() {
        return location != null ? location.getAccuracy() : null;
    }

    @Override
    public Long getElapsedSeconds() {
        return location != null ? (System.currentTimeMillis() - location.getTime()) / 1000 : null;
    }

    @Override
    public boolean isLocationAvailable() {
        return location != null;
    }

    /** True when either approximate or precise location has been granted. */
    private boolean isLocationPermissionGranted() {
        return getContext() != null
               && (getContext().checkCallingOrSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                   || isFineLocationPermissionGranted());
    }

    /** True only for precise location, which is what GPS_PROVIDER requires. */
    private boolean isFineLocationPermissionGranted() {
        return getContext() != null
               && getContext().checkCallingOrSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
    }
}
