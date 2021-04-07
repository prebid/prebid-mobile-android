package com.openx.apollo.mraid;

import com.openx.apollo.sdk.ApolloSettings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertEquals;

@RunWith(JUnit4.class)
public class MraidEnvTest {

    @Test
    public void getWindowMraidEnv_ReturnProperlyFormedMraid() {
        ApolloSettings.isCoppaEnabled = true;
        String expectedValue = "window.MRAID_ENV = {"
                               + "version: \"" + ApolloSettings.MRAID_VERSION + "\","
                               + "sdk: \"" + ApolloSettings.SDK_NAME + "\","
                               + "sdkVersion: \"" + ApolloSettings.SDK_VERSION + "\","
                               + "appId: \"null\","
                               + "ifa: \"null\","
                               + "limitAdTracking: false,"
                               + "coppa: true"
                               + "};";

        assertEquals(expectedValue, MraidEnv.getWindowMraidEnv());
    }
}
