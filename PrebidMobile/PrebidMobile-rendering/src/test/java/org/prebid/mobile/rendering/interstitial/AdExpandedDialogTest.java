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
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdExpandedDialogTest {

    private AdExpandedDialog mAdExpandedDialog;

    private Context mMockContext;
    private WebViewBase mMockWebViewBase;
    private BaseJSInterface mMockBaseJSInterface;
    private PrebidWebViewBase mMockPrebidWebView;
    private InterstitialManager mMockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        mMockContext = Robolectric.buildActivity(Activity.class).create().get();
        mMockWebViewBase = mock(WebViewBase.class);
        mMockBaseJSInterface = mock(BaseJSInterface.class);
        mMockInterstitialManager = mock(InterstitialManager.class);

        mMockPrebidWebView = mock(PrebidWebViewBase.class);

        when(mMockWebViewBase.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        when(mMockWebViewBase.getPreloadedListener()).thenReturn(mMockPrebidWebView);

        mAdExpandedDialog = new AdExpandedDialog(mMockContext, mMockWebViewBase, mMockInterstitialManager);
    }

    @Test
    public void handleCloseClick() throws IllegalAccessException {
        InterstitialManager interstitialManager = mock(InterstitialManager.class);
        Field interstitialManagerField = WhiteBox.field(AdInterstitialDialog.class, "mInterstitialManager");
        interstitialManagerField.set(mAdExpandedDialog, interstitialManager);

        mAdExpandedDialog.handleCloseClick();
        verify(interstitialManager).interstitialClosed(eq(mMockWebViewBase));
    }

    @Test
    public void cancelTest() {
        when(mMockPrebidWebView.getCreative()).thenReturn(mock(HTMLCreative.class));
        when(mMockWebViewBase.getJSName()).thenReturn("");

        mAdExpandedDialog.cancel();
        verify(mMockPrebidWebView, atLeast(1)).addView(any(View.class));
        verify(mMockBaseJSInterface).onStateChange(eq(JSInterface.STATE_DEFAULT));
    }
}