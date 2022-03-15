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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class DeviceInfoImplTest {

    private DeviceInfoImpl mDeviceInfoImpl;
    @Mock
    private Context mMockContext;
    @Mock
    private TelephonyManager mMockTelephonyManger;
    @Mock
    private WindowManager mMockWindowManager;
    @Mock
    private PowerManager mMockPowerManager;
    @Mock
    private KeyguardManager mMockKeyguardManager;
    @Mock
    private PackageManager mMockPackageManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mDeviceInfoImpl = new DeviceInfoImpl();

        when(mMockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mMockTelephonyManger);
        when(mMockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mMockWindowManager);
        when(mMockContext.getSystemService(Context.POWER_SERVICE)).thenReturn(mMockPowerManager);
        when(mMockContext.getSystemService(Context.KEYGUARD_SERVICE)).thenReturn(mMockKeyguardManager);
        when(mMockContext.getPackageManager()).thenReturn(mMockPackageManager);

        mDeviceInfoImpl.init(mMockContext);
    }

    @Test
    public void hasTelephonyTest() throws IllegalAccessException {
        when(mMockPackageManager.hasSystemFeature(anyString())).thenReturn(Boolean.TRUE);
        assertTrue(mDeviceInfoImpl.hasTelephony());

        WhiteBox.field(DeviceInfoImpl.class, "mTelephonyManager").set(mDeviceInfoImpl, null);
        assertFalse(mDeviceInfoImpl.hasTelephony());

        WhiteBox.field(DeviceInfoImpl.class, "mTelephonyManager").set(mDeviceInfoImpl, mMockTelephonyManger);
        WhiteBox.field(DeviceInfoImpl.class, "mPackageManager").set(mDeviceInfoImpl, null);
        assertFalse(mDeviceInfoImpl.hasTelephony());
    }

    @Test
    public void getMccMncTest() {
        when(mMockTelephonyManger.getNetworkOperator()).thenReturn("0123456");
        assertEquals("012-3456", mDeviceInfoImpl.getMccMnc());

        when(mMockTelephonyManger.getNetworkOperator()).thenReturn("01");
        assertNull(mDeviceInfoImpl.getMccMnc());
    }

    @Test
    public void getCarrierTest() {
        when(mMockTelephonyManger.getNetworkOperatorName()).thenReturn("test");
        assertEquals("test", mDeviceInfoImpl.getCarrier());
    }

    @Test
    public void isScreenOnTest() throws IllegalAccessException {
        when(mMockPowerManager.isScreenOn()).thenReturn(true);
        assertTrue(mDeviceInfoImpl.isScreenOn());

        WhiteBox.field(DeviceInfoImpl.class, "mPowerManager").set(mDeviceInfoImpl, null);
        assertFalse(mDeviceInfoImpl.isScreenOn());
    }

    @Test
    public void isScreenLockedTest() throws IllegalAccessException {
        when(mMockKeyguardManager.inKeyguardRestrictedInputMode()).thenReturn(true);
        assertTrue(mDeviceInfoImpl.isScreenLocked());

        WhiteBox.field(DeviceInfoImpl.class, "mKeyguardManager").set(mDeviceInfoImpl, null);
        assertFalse(mDeviceInfoImpl.isScreenLocked());
    }

    @Test
    public void isActivityOrientationLockedWithApplicationContext_ReturnFalse() {
        assertFalse(mDeviceInfoImpl.isActivityOrientationLocked(mMockContext));
    }

    @Test
    public void isActivityOrientationLockedWithActivityContextAndActivityLocked_ReturnTrue() {
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getRequestedOrientation()).thenReturn(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        assertTrue(mDeviceInfoImpl.isActivityOrientationLocked(mockActivity));
    }

    @Test
    public void isActivityOrientationLockedWithActivityContextAndActivityNotLocked_ReturnFalse() {
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getRequestedOrientation()).thenReturn(ActivityInfo.SCREEN_ORIENTATION_USER);

        assertFalse(mDeviceInfoImpl.isActivityOrientationLocked(mockActivity));
    }

    @Test
    public void playVideoTest() {
        mDeviceInfoImpl.playVideo("test");
        verify(mMockContext).startActivity(any(Intent.class));
    }

    @Test
    public void getDeviceDensityTest() throws IllegalAccessException {
        Resources mockResources = mock(Resources.class);
        DisplayMetrics mockMetrics = mock(DisplayMetrics.class);
        mockMetrics.density = 2.0f;

        when(mMockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getDisplayMetrics()).thenReturn(mockMetrics);
        assertEquals(2.0f, mDeviceInfoImpl.getDeviceDensity(), 0);

        WeakReference<Context> weakReference = new WeakReference<>(null);
        WhiteBox.field(DeviceInfoImpl.class, "mContextReference").set(mDeviceInfoImpl, weakReference);
        assertEquals(1.0f, mDeviceInfoImpl.getDeviceDensity(), 0);
    }

    @Test
    public void isPermissionGrantedTest() {
        when(mMockContext.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);
        assertTrue(mDeviceInfoImpl.isPermissionGranted("test"));
    }

    @Test
    public void canStorePictureTest() {
        assertTrue(mDeviceInfoImpl.canStorePicture());
    }

    @Test
    public void storePictureOnQ_GetOutputStreamForQ() throws Exception {
        DeviceInfoImpl spyDeviceImpl = spy(mDeviceInfoImpl);
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
        DeviceInfoImpl spyDeviceImpl = spy(mDeviceInfoImpl);
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
        DeviceInfoImpl spyDeviceImpl = spy(mDeviceInfoImpl);
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
        DeviceInfoImpl spyDeviceImpl = spy(mDeviceInfoImpl);

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        doReturn(null).when(spyDeviceImpl).getOutputStreamPreQ(anyString());

        spyDeviceImpl.storePicture(url);
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(spyDeviceImpl).getOutputStream(filenameCaptor.capture());
        assertFalse(filenameCaptor.getValue().contains("."));
    }
}