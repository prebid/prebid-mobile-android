package org.prebid.mobile.rendering.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.apollo.test.utils.WhiteBox;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.deviceData.listeners.SdkInitListener;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PrebidRenderingSettingsTest {

    @Before
    public void setUp() throws Exception {

        initAndroidVersion();
    }

    // Sets Build.VERSION.SDK_INT to LOLLIPOP(21) which prevents ProviderInstaller from execution
    private void initAndroidVersion() throws NoSuchFieldException, IllegalAccessException {
        Field versionField = (Build.VERSION.class.getField("SDK_INT"));
        versionField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(versionField, versionField.getModifiers() & ~Modifier.FINAL);

        versionField.set(null, Build.VERSION_CODES.LOLLIPOP);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetDeviceName() throws Exception {
        assertEquals("Unknown robolectric", AppInfoManager.getDeviceName());
    }

    @Test
    public void testOnSDKInitWithoutVideoPreCache() throws Exception {
        //test if sdkinit is sent even if precache fails for any reason, as it is optional & should not avoid further sdk actions
        WhiteBox.field(PrebidRenderingSettings.class, "sIsSdkInitialized").set(null, false);
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        SdkInitListener mockSdkInitListener = mock(SdkInitListener.class);

        PrebidRenderingSettings.initializeSDK(context, mockSdkInitListener);
        verify(mockSdkInitListener, times(1)).onSDKInit();
    }

    @Test
    public void testWebViewHttpsEnabledTrue() {
        PrebidRenderingSettings.useHttpsWebViewBaseUrl(true);

        assertEquals(PrebidRenderingSettings.SCHEME_HTTPS, PrebidRenderingSettings.getWebViewBaseUrlScheme());

        PrebidRenderingSettings.useHttpsWebViewBaseUrl(false);
    }

    @Test
    public void testWebViewHttpsEnabledFalse() {
        PrebidRenderingSettings.useHttpsWebViewBaseUrl(false);

        assertEquals(PrebidRenderingSettings.SCHEME_HTTP, PrebidRenderingSettings.getWebViewBaseUrlScheme());
    }
}