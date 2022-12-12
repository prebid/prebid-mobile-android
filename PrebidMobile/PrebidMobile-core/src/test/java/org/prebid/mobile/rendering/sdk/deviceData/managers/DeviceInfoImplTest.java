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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@RunWith(RobolectricTestRunner.class)
public class DeviceInfoImplTest {

    private DeviceInfoImpl deviceInfoImpl;
    @Mock
    private Context mockContext;
    @Mock
    private TelephonyManager mockTelephonyManger;
    @Mock
    private WindowManager mockWindowManager;
    @Mock
    private PowerManager mockPowerManager;
    @Mock
    private KeyguardManager mockKeyguardManager;
    @Mock
    private PackageManager mockPackageManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManger);
        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager);
        when(mockContext.getSystemService(Context.POWER_SERVICE)).thenReturn(mockPowerManager);
        when(mockContext.getSystemService(Context.KEYGUARD_SERVICE)).thenReturn(mockKeyguardManager);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);

        deviceInfoImpl = new DeviceInfoImpl(mockContext);
    }

    @Test
    public void hasTelephonyTest() throws IllegalAccessException {
        when(mockPackageManager.hasSystemFeature(anyString())).thenReturn(Boolean.TRUE);
        assertTrue(deviceInfoImpl.hasTelephony());

        WhiteBox.field(DeviceInfoImpl.class, "telephonyManager").set(deviceInfoImpl, null);
        assertFalse(deviceInfoImpl.hasTelephony());

        WhiteBox.field(DeviceInfoImpl.class, "telephonyManager").set(deviceInfoImpl, mockTelephonyManger);
        WhiteBox.field(DeviceInfoImpl.class, "packageManager").set(deviceInfoImpl, null);
        assertFalse(deviceInfoImpl.hasTelephony());
    }

    @Test
    public void getMccMncTest() {
        when(mockTelephonyManger.getNetworkOperator()).thenReturn("0123456");
        assertEquals("012-3456", deviceInfoImpl.getMccMnc());

        when(mockTelephonyManger.getNetworkOperator()).thenReturn("01");
        assertNull(deviceInfoImpl.getMccMnc());
    }

    @Test
    public void getCarrierTest() {
        when(mockTelephonyManger.getNetworkOperatorName()).thenReturn("test");
        assertEquals("test", deviceInfoImpl.getCarrier());
    }

    @Test
    public void isScreenOnTest() throws IllegalAccessException {
        when(mockPowerManager.isScreenOn()).thenReturn(true);
        assertTrue(deviceInfoImpl.isScreenOn());

        WhiteBox.field(DeviceInfoImpl.class, "powerManager").set(deviceInfoImpl, null);
        assertFalse(deviceInfoImpl.isScreenOn());
    }

    @Test
    public void isScreenLockedTest() throws IllegalAccessException {
        when(mockKeyguardManager.inKeyguardRestrictedInputMode()).thenReturn(true);
        assertTrue(deviceInfoImpl.isScreenLocked());

        WhiteBox.field(DeviceInfoImpl.class, "keyguardManager").set(deviceInfoImpl, null);
        assertFalse(deviceInfoImpl.isScreenLocked());
    }

    @Test
    public void isActivityOrientationLockedWithApplicationContext_ReturnFalse() {
        assertFalse(deviceInfoImpl.isActivityOrientationLocked(mockContext));
    }

    @Test
    public void isActivityOrientationLockedWithActivityContextAndActivityLocked_ReturnTrue() {
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getRequestedOrientation()).thenReturn(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        assertTrue(deviceInfoImpl.isActivityOrientationLocked(mockActivity));
    }

    @Test
    public void isActivityOrientationLockedWithActivityContextAndActivityNotLocked_ReturnFalse() {
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getRequestedOrientation()).thenReturn(ActivityInfo.SCREEN_ORIENTATION_USER);

        assertFalse(deviceInfoImpl.isActivityOrientationLocked(mockActivity));
    }

    @Test
    public void playVideoTest() {
        deviceInfoImpl.playVideo("test", mockContext);
        verify(mockContext).startActivity(any(Intent.class));
    }

    @Test
    public void getDeviceDensityTest() throws IllegalAccessException {
        Resources mockResources = mock(Resources.class);
        DisplayMetrics mockMetrics = mock(DisplayMetrics.class);
        mockMetrics.density = 2.0f;

        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getDisplayMetrics()).thenReturn(mockMetrics);
        assertEquals(2.0f, deviceInfoImpl.getDeviceDensity(), 0);

        WeakReference<Context> weakReference = new WeakReference<>(null);
        WhiteBox.field(DeviceInfoImpl.class, "contextReference").set(deviceInfoImpl, weakReference);
        assertEquals(1.0f, deviceInfoImpl.getDeviceDensity(), 0);
    }

    @Test
    public void isPermissionGrantedTest() {
        when(mockContext.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);
        assertTrue(deviceInfoImpl.isPermissionGranted("test"));
    }

    @Test
    public void canStorePictureTest() {
        assertTrue(deviceInfoImpl.canStorePicture());
    }

    @Test
    public void storePictureOnQ_GetOutputStreamForQ() throws Exception {
        DeviceInfoImpl spyDeviceImpl = spy(deviceInfoImpl);
        String url = "http://test.com/somefile.png";

        Field versionField = WhiteBox.field(Build.VERSION.class, "SDK_INT");
        versionField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(versionField, versionField.getModifiers() & ~Modifier.FINAL);

        versionField.set(null, 29);

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        when(spyDeviceImpl.getOutPutStreamForQ(anyString(), any(Context.class)))
            .thenReturn(null);

        spyDeviceImpl.storePicture(url);

        verify(spyDeviceImpl).getOutputStream(anyString());
        verify(spyDeviceImpl).getOutPutStreamForQ(anyString(), any(Context.class));
    }

    @Test
    public void storePicturePreQ_GetOutputStreamPreQ() throws Exception {
        DeviceInfoImpl spyDeviceImpl = spy(deviceInfoImpl);
        String url = "http://test.com/somefile.png";

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        doReturn(null)
            .when(spyDeviceImpl).getOutputStreamPreQ(anyString());

        spyDeviceImpl.storePicture(url);

        verify(spyDeviceImpl).getOutputStream(anyString());
        verify(spyDeviceImpl).getOutputStreamPreQ(anyString());
    }

    @Test
    public void storePictureWithFileExtension_AddFieExtensionToFileName()
    throws Exception {
        DeviceInfoImpl spyDeviceImpl = spy(deviceInfoImpl);
        final String url = "http://test.com/somefile.png";
        final String extension = ".png";

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        doReturn(null).when(spyDeviceImpl).getOutputStreamPreQ(anyString());
        spyDeviceImpl.storePicture(url);

        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(spyDeviceImpl).getOutputStream(filenameCaptor.capture());
        assertTrue(filenameCaptor.getValue().endsWith(extension));
    }

    @Test
    public void storePictureEmptyOrNullFileExtension_FileNameUnmodified() throws Exception {
        final String url = "http://test.com/somefile";
        DeviceInfoImpl spyDeviceImpl = spy(deviceInfoImpl);

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        doReturn(null).when(spyDeviceImpl).getOutputStreamPreQ(anyString());

        spyDeviceImpl.storePicture(url);
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(spyDeviceImpl).getOutputStream(filenameCaptor.capture());
        assertFalse(filenameCaptor.getValue().contains("."));
    }
}