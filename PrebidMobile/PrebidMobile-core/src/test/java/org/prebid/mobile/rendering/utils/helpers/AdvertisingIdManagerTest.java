package org.prebid.mobile.rendering.utils.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;

public class AdvertisingIdManagerTest {

    private MockedStatic<GoogleApiAvailability> staticMockGoogleApi;
    private MockedStatic<AdvertisingIdClient> staticMockIdClient;

    private AdvertisingIdClient.Info mockGoogleAdInfo = mock(AdvertisingIdClient.Info.class);
    private UserConsentManager mockConsentManager = mock(UserConsentManager.class);
    private GoogleApiAvailability mockGoogleAvailability = mock(GoogleApiAvailability.class);

    @Before
    public void setup() {
        staticMockGoogleApi = mockStatic(GoogleApiAvailability.class);
        staticMockIdClient = mockStatic(AdvertisingIdClient.class);

        PrebidContextHolder.setContext(mock(Context.class));

        AdvertisingIdManagerReflections.setAdvertisingId(null);
        AdvertisingIdManagerReflections.resetLastStartTime();
    }

    @After
    public void tearDown() {
        staticMockGoogleApi.close();
        staticMockIdClient.close();
    }

    @Test
    public void initData() {
        mockAccessDeviceData(true);

        assertNull(AdvertisingIdManager.getAdvertisingId(mockConsentManager));
        assertFalse(AdvertisingIdManager.isLimitedAdTrackingEnabled());
        verify(mockConsentManager, times(1)).canAccessDeviceData();
    }

    @Test
    public void noGooglePlayServices() {
        mockAccessDeviceData(true);
        mockAvailability(ConnectionResult.SERVICE_MISSING);

        AdvertisingIdManager.initAdvertisingId();

        assertNull(AdvertisingIdManager.getAdvertisingId(mockConsentManager));
        assertFalse(AdvertisingIdManager.isLimitedAdTrackingEnabled());

        verify(mockGoogleAvailability, times(1)).isGooglePlayServicesAvailable(any());
        verifyNoInteractions(mockGoogleAdInfo);
    }

    @Test
    public void emptyIdInfo() {
        mockAccessDeviceData(true);
        mockAdInfo(null, false);
        mockAvailability(ConnectionResult.SUCCESS);

        AdvertisingIdManager.initAdvertisingId();

        assertNull(AdvertisingIdManager.getAdvertisingId(mockConsentManager));
        assertFalse(AdvertisingIdManager.isLimitedAdTrackingEnabled());

        verify(mockGoogleAvailability, times(1)).isGooglePlayServicesAvailable(any());
        verify(mockGoogleAdInfo, times(1)).getId();
    }

    @Test
    public void filledData() {
        mockAccessDeviceData(true);
        mockAdInfo("id", true);
        mockAvailability(ConnectionResult.SUCCESS);

        AdvertisingIdManager.initAdvertisingId();

        assertEquals("id", AdvertisingIdManager.getAdvertisingId(mockConsentManager));
        assertTrue(AdvertisingIdManager.isLimitedAdTrackingEnabled());

        verify(mockGoogleAvailability, times(1)).isGooglePlayServicesAvailable(any());
        verify(mockGoogleAdInfo, times(1)).getId();
    }

    @Test
    public void filledData_noAccessDeviceData() {
        mockAccessDeviceData(false);
        mockAdInfo("id", true);
        mockAvailability(ConnectionResult.SUCCESS);

        AdvertisingIdManager.initAdvertisingId();

        assertNull(AdvertisingIdManager.getAdvertisingId(mockConsentManager));
        assertTrue(AdvertisingIdManager.isLimitedAdTrackingEnabled());

        verify(mockGoogleAvailability, times(1)).isGooglePlayServicesAvailable(any());
        verify(mockGoogleAdInfo, times(1)).getId();
    }

    @Test
    public void testRetryImmediately() {
        mockAccessDeviceData(false);
        mockAdInfo("id", true);
        mockAvailability(ConnectionResult.SUCCESS);

        AdvertisingIdManager.initAdvertisingId();

        Mockito.reset(mockGoogleAvailability);
        Mockito.reset(mockGoogleAdInfo);

        AdvertisingIdManager.initAdvertisingId();
        verifyNoInteractions(mockGoogleAvailability);
        verifyNoInteractions(mockGoogleAdInfo);
    }

    private void mockAccessDeviceData(boolean access) {
        when(mockConsentManager.canAccessDeviceData()).thenReturn(access);
    }

    private void mockAvailability(int result) {
        when(GoogleApiAvailability.getInstance()).thenReturn(mockGoogleAvailability);
        when(mockGoogleAvailability.isGooglePlayServicesAvailable(any())).thenReturn(result);
    }

    private void mockAdInfo(String id, boolean isLimitAdTrackingEnabled) {
        try {
            when(AdvertisingIdClient.getAdvertisingIdInfo(any())).thenReturn(mockGoogleAdInfo);
            when(mockGoogleAdInfo.getId()).thenReturn(id);
            when(mockGoogleAdInfo.isLimitAdTrackingEnabled()).thenReturn(isLimitAdTrackingEnabled);
        } catch (Exception exception) {
            fail(exception.getMessage());
        }
    }

    public static class AdvertisingIdManagerReflections {

        public static void setAdvertisingId(AdvertisingIdManager.AdvertisingId advertisingId) {
            Reflection.setStaticVariableTo(AdvertisingIdManager.class, "advertisingId", advertisingId);
        }

        public static void resetLastStartTime() {
            Reflection.setStaticVariableTo(AdvertisingIdManager.class, "lastStartTime", 0);
        }

    }

}