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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.location.Location;

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
}