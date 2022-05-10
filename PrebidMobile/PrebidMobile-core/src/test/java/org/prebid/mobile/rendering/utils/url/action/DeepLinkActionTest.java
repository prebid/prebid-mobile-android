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
    private DeepLinkAction deepLinkAction;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(DeepLinkActionTest.class);

        deepLinkAction = new DeepLinkAction();
    }

    @Test
    public void whenShouldOverrideUrlLoadingWithHttpHttpsScheme_ReturnFalse() {
        Uri httpUri = Uri.parse("http://prebid.com");
        Uri httpsUri = Uri.parse("https://prebid.com");

        assertFalse(deepLinkAction.shouldOverrideUrlLoading(httpUri));
        assertFalse(deepLinkAction.shouldOverrideUrlLoading(httpsUri));
    }

    @Test
    public void whenShouldOverrideUrlLoadingCustomScheme_ReturnTrue() {
        Uri customSchemeUri = Uri.parse("prebid://open");

        assertTrue(deepLinkAction.shouldOverrideUrlLoading(customSchemeUri));
    }

    @Test
    public void whenShouldBeTriggeredByUserAction_ReturnTrue() {
        assertTrue(deepLinkAction.shouldBeTriggeredByUserAction());
    }
}
