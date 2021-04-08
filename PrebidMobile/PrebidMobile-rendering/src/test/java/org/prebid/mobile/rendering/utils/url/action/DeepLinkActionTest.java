package org.prebid.mobile.rendering.utils.url.action;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class DeepLinkActionTest {
    private DeepLinkAction mDeepLinkAction;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(DeepLinkActionTest.class);

        mDeepLinkAction = new DeepLinkAction();
    }

    @Test
    public void whenShouldOverrideUrlLoadingWithHttpHttpsScheme_ReturnFalse() {
        Uri httpUri = Uri.parse("http://openx.com");
        Uri httpsUri = Uri.parse("https://openx.com");

        assertFalse(mDeepLinkAction.shouldOverrideUrlLoading(httpUri));
        assertFalse(mDeepLinkAction.shouldOverrideUrlLoading(httpsUri));
    }

    @Test
    public void whenShouldOverrideUrlLoadingCustomScheme_ReturnTrue() {
        Uri customSchemeUri = Uri.parse("openx://open");

        assertTrue(mDeepLinkAction.shouldOverrideUrlLoading(customSchemeUri));
    }

    @Test
    public void whenShouldBeTriggeredByUserAction_ReturnTrue() {
        assertTrue(mDeepLinkAction.shouldBeTriggeredByUserAction());
    }
}
