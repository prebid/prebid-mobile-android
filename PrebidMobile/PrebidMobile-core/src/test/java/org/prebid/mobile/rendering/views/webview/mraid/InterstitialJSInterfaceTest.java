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

package org.prebid.mobile.rendering.views.webview.mraid;

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class InterstitialJSInterfaceTest {

    private InterstitialJSInterface interstitialJSInterface;
    private Context context;
    private WebViewBase mockWebViewBase;

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        mockWebViewBase = mock(WebViewBase.class);

        interstitialJSInterface = new InterstitialJSInterface(context, mockWebViewBase, mock(JsExecutor.class));
    }

    @Test
    public void getPlacementTypeTest() {
        assertEquals("interstitial", interstitialJSInterface.getPlacementType());
    }

    @Test
    public void expandTest() {
        //do nothing
        interstitialJSInterface.expand();
    }

}