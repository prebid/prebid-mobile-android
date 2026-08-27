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
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class LastKnownLocationInfoManagerTest {

    private LastKnownLocationInfoManager locationImpl;
    private Context context;
    /** Held for the lifetime of a test; the manager only weakly references it. */
    private Context permissionContext;

    private final int TWO_MINUTES = 1000 * 60 * 2;

    @Before
    public void setUp() {
        context = mock(Context.class);

        locationImpl = new LastKnownLocationInfoManager(context);
    }

    @Test
    public void isBetterLocationTest() {
        Location location = mock(Location.class);
        Location currentLocation = mock(Location.class);

        assertTrue(locationImpl.isBetterLocation(location, null));

        assertFalse(locationImpl.isBetterLocation(null, currentLocation));

        when(location.getTime()).thenReturn((long) 100);
        when(currentLocation.getTime()).thenReturn((long) 200);
        assertFalse(locationImpl.isBetterLocation(location, currentLocation));

        when(location.getTime()).thenReturn((long) TWO_MINUTES + 1);
        when(currentLocation.getTime()).thenReturn((long) 0);
        assertTrue(locationImpl.isBetterLocation(location, currentLocation));

        when(location.getTime()).thenReturn((long) 0);
        when(currentLocation.getTime()).thenReturn((long) TWO_MINUTES + 1);
        assertFalse(locationImpl.isBetterLocation(location, currentLocation));

        when(location.getTime()).thenReturn((long) 2);
        when(currentLocation.getTime()).thenReturn((long) 0);
        when(location.getAccuracy()).thenReturn((float) 10);
        when(currentLocation.getAccuracy()).thenReturn((float) 20);
        when(location.getProvider()).thenReturn("test");
        when(currentLocation.getProvider()).thenReturn("test");
        assertTrue(locationImpl.isBetterLocation(location, currentLocation));

        when(location.getAccuracy()).thenReturn((float) 20);
        when(currentLocation.getAccuracy()).thenReturn((float) 10);
        assertTrue(locationImpl.isBetterLocation(location, currentLocation));

        when(location.getAccuracy()).thenReturn((float) 0);
        when(currentLocation.getAccuracy()).thenReturn((float) 0);
        assertTrue(locationImpl.isBetterLocation(location, currentLocation));
    }

    @Test
    public void whenOnlyCoarseGranted_bothProvidersAreStillAsked() {
        LocationManager locationManager = mock(LocationManager.class);
        Location networkLocation = mock(Location.class);
        when(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(networkLocation);

        LastKnownLocationInfoManager manager =
                managerWith(locationManager, PERMISSION_GRANTED, PERMISSION_DENIED);

        assertTrue(manager.isLocationAvailable());
        // From API 31 a coarse-only client still receives a fix from GPS_PROVIDER,
        // coarsened by LocationFudger, so the provider must not be skipped on the
        // strength of the permission alone.
        verify(locationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        verify(locationManager).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    @Test
    public void whenCoarseOnlyAndGpsIsRefused_networkProviderIsStillRead() {
        // The pre-API-31 case, where LocationManagerService rejected GPS_PROVIDER for a
        // coarse-only client. The refusal must not discard the network fix, which is
        // the defect this class had.
        LocationManager locationManager = mock(LocationManager.class);
        Location networkLocation = mock(Location.class);
        when(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
                .thenThrow(new SecurityException("coarse permission is insufficient for GPS_PROVIDER"));
        when(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(networkLocation);
        when(networkLocation.getLatitude()).thenReturn(51.5);

        LastKnownLocationInfoManager manager =
                managerWith(locationManager, PERMISSION_GRANTED, PERMISSION_DENIED);

        assertTrue(manager.isLocationAvailable());
        assertEquals(51.5, manager.getLatitude(), 0.0001);
    }

    @Test
    public void whenAProviderIsAbsentFromTheDevice_theOtherIsStillRead() {
        LocationManager locationManager = mock(LocationManager.class);
        Location networkLocation = mock(Location.class);
        when(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
                .thenThrow(new IllegalArgumentException("no GPS_PROVIDER on this device"));
        when(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(networkLocation);

        LastKnownLocationInfoManager manager =
                managerWith(locationManager, PERMISSION_GRANTED, PERMISSION_GRANTED);

        assertTrue(manager.isLocationAvailable());
    }

    @Test
    public void whenNoLocationPermissionGranted_noProviderIsRead() {
        LocationManager locationManager = mock(LocationManager.class);

        LastKnownLocationInfoManager manager =
                managerWith(locationManager, PERMISSION_DENIED, PERMISSION_DENIED);

        assertFalse(manager.isLocationAvailable());
        verify(locationManager, never()).getLastKnownLocation(anyString());
    }

    @Test
    public void whenPermissionIsRevoked_theCachedFixIsCleared() {
        LocationManager locationManager = mock(LocationManager.class);
        Location networkLocation = mock(Location.class);
        when(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(networkLocation);

        LastKnownLocationInfoManager manager =
                managerWith(locationManager, PERMISSION_GRANTED, PERMISSION_DENIED);
        assertTrue(manager.isLocationAvailable());

        when(permissionContext.checkCallingOrSelfPermission(ACCESS_COARSE_LOCATION)).thenReturn(PERMISSION_DENIED);
        manager.resetLocation();

        assertFalse(manager.isLocationAvailable());
    }

    /**
     * Builds a manager over a mocked context with the given grants. The context is held
     * in a field because the manager keeps only a WeakReference to it.
     */
    private LastKnownLocationInfoManager managerWith(
            LocationManager locationManager,
            int coarseGrant,
            int fineGrant
    ) {
        permissionContext = mock(Context.class);
        when(permissionContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager);
        when(permissionContext.checkCallingOrSelfPermission(ACCESS_COARSE_LOCATION)).thenReturn(coarseGrant);
        when(permissionContext.checkCallingOrSelfPermission(ACCESS_FINE_LOCATION)).thenReturn(fineGrant);
        return new LastKnownLocationInfoManager(permissionContext);
    }
}
