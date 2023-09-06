package org.prebid.mobile.rendering.sdk;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserAgentFetcherTaskTest {

    @Before
    public void setUp() throws Exception {
        AppInfoManager.setUserAgent(null);
    }

    @Test
    public void runUserAgentTask_callCallbackListener() throws InterruptedException {
        InitializationNotifier initializationNotifier = mock(InitializationNotifier.class);

        UserAgentFetcherTask.run(initializationNotifier);

        Thread.sleep(2000);

        verify(initializationNotifier, times(1)).taskCompleted();
        assertNotNull(AppInfoManager.getUserAgent());
    }

}