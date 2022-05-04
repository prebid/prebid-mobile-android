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

package org.prebid.mobile.rendering.interstitial;

import android.app.Activity;
import android.content.Context;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdInterstitialDialogTest {

    private AdInterstitialDialog adInterstitialDialog;

    private Context mockContext;
    private WebViewBase mockWebViewBase;
    private BaseJSInterface mockBaseJSInterface;
    private FrameLayout mockAdContainer;
    private InterstitialManager mockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        mockContext = Robolectric.buildActivity(Activity.class).create().get();
        mockWebViewBase = mock(WebViewBase.class);
        mockAdContainer = mock(FrameLayout.class);
        mockBaseJSInterface = mock(BaseJSInterface.class);
        mockInterstitialManager = mock(InterstitialManager.class);

        when(mockWebViewBase.getMRAIDInterface()).thenReturn(mockBaseJSInterface);
        when(mockBaseJSInterface.getJsExecutor()).thenReturn(mock(JsExecutor.class));

        adInterstitialDialog = spy(new AdInterstitialDialog(mockContext,
                mockWebViewBase,
                mockAdContainer,
                mockInterstitialManager
        ));
    }

    @Test
    public void handleCloseClick() throws IllegalAccessException {
        InterstitialManager interstitialManager = mock(InterstitialManager.class);
        Field interstitialManagerField = WhiteBox.field(AdInterstitialDialog.class, "interstitialManager");
        interstitialManagerField.set(adInterstitialDialog, interstitialManager);

        adInterstitialDialog.handleCloseClick();
        verify(interstitialManager).interstitialClosed(mockWebViewBase);
    }

    @Test
    public void nullifyDialog() {
        adInterstitialDialog.nullifyDialog();

        verify(adInterstitialDialog, atLeastOnce()).cancel();
        verify(adInterstitialDialog).cleanup();
    }

    @Test
    public void cancelTest() {
        when(mockWebViewBase.isMRAID()).thenReturn(true);

        adInterstitialDialog.cancel();
        verify(mockBaseJSInterface).onStateChange(JSInterface.STATE_DEFAULT);
        verify(mockWebViewBase).detachFromParent();
    }
}