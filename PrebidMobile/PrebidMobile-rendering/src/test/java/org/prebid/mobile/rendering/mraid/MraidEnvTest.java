package org.prebid.mobile.rendering.mraid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;

import static junit.framework.TestCase.assertEquals;

@RunWith(JUnit4.class)
public class MraidEnvTest {

    @Test
    public void getWindowMraidEnv_ReturnProperlyFormedMraid() {
        PrebidRenderingSettings.isCoppaEnabled = true;
        String expectedValue = "window.MRAID_ENV = {"
                               + "version: \"" + PrebidRenderingSettings.MRAID_VERSION + "\","
                               + "sdk: \"" + PrebidRenderingSettings.SDK_NAME + "\","
                               + "sdkVersion: \"" + PrebidRenderingSettings.SDK_VERSION + "\","
                               + "appId: \"null\","
                               + "ifa: \"null\","
                               + "limitAdTracking: false,"
                               + "coppa: true"
                               + "};";

        assertEquals(expectedValue, MraidEnv.getWindowMraidEnv());
    }
}
