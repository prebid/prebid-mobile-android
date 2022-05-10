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

package org.prebid.mobile.rendering.views.browser;

import android.content.Intent;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.VideoView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.rendering.views.browser.AdBrowserActivity.EXTRA_URL;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdBrowserActivityTest {

    private AdBrowserActivity adBrowserActivity;
    private Intent intent;

    @Before
    public void setUp() throws Exception {
        intent = new Intent();
        intent.putExtra(AdBrowserActivity.EXTRA_IS_VIDEO, false);
        intent.putExtra(EXTRA_URL, "tel:123123");
        adBrowserActivity = Robolectric.buildActivity(AdBrowserActivity.class, intent).create().get();
    }

    @Test
    public void initTest() {
        //verify that activity creates without an Exception
        adBrowserActivity = Robolectric.buildActivity(AdBrowserActivity.class, intent).create().get();

        intent.putExtra(AdBrowserActivity.EXTRA_IS_VIDEO, true);
        adBrowserActivity = Robolectric.buildActivity(AdBrowserActivity.class, intent).create().get();
    }

    @Test
    public void onKeyDownTest() throws IllegalAccessException {
        WebView mockWebView = mock(WebView.class);
        WhiteBox.field(AdBrowserActivity.class, "webView").set(adBrowserActivity, mockWebView);

        adBrowserActivity.onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(0, 0));
        verify(mockWebView).goBack();
    }

    @Test
    public void onPauseTest() throws IllegalAccessException {
        VideoView mockView = mock(VideoView.class);
        WhiteBox.field(AdBrowserActivity.class, "videoView").set(adBrowserActivity, mockView);

        adBrowserActivity.onPause();
        verify(mockView).suspend();
    }

    @Test
    public void onResumeTest() throws IllegalAccessException {
        VideoView mockView = mock(VideoView.class);
        WhiteBox.field(AdBrowserActivity.class, "videoView").set(adBrowserActivity, mockView);

        adBrowserActivity.onResume();
        verify(mockView).resume();
    }

    @Test
    public void onDestroyTest() throws IllegalAccessException {
        intent.putExtra(AdBrowserActivity.EXTRA_IS_VIDEO, true);
        adBrowserActivity = Robolectric.buildActivity(AdBrowserActivity.class, intent).create().get();

        WebView mockWebView = mock(WebView.class);
        VideoView mockVideoView = mock(VideoView.class);
        WhiteBox.field(AdBrowserActivity.class, "webView").set(adBrowserActivity, mockWebView);
        WhiteBox.field(AdBrowserActivity.class, "videoView").set(adBrowserActivity, mockVideoView);

        adBrowserActivity.onDestroy();
        verify(mockWebView).destroy();
        verify(mockVideoView).suspend();
    }

    @Test
    public void browserControlsTest() throws IllegalAccessException {
        WebView mockWebView = mock(WebView.class);
        when(mockWebView.getUrl()).thenReturn("url");
        when(mockWebView.canGoForward()).thenReturn(true);
        when(mockWebView.canGoBack()).thenReturn(true);

        InterstitialManager mockManager = mock(InterstitialManager.class);
        when(mockManager.getInterstitialDisplayDelegate()).thenReturn(null);

        WhiteBox.field(AdBrowserActivity.class, "shouldFireEvents").set(adBrowserActivity, true);
        WhiteBox.field(AdBrowserActivity.class, "webView").set(adBrowserActivity, mockWebView);
        BrowserControlsEventsListener eventsListener = ((BrowserControls) WhiteBox.field(
                AdBrowserActivity.class,
                "browserControls"
        ).get(adBrowserActivity)).getBrowserControlsEventsListener();

        eventsListener.onRelaod();
        verify(mockWebView).reload();

        eventsListener.onGoForward();
        verify(mockWebView).goForward();

        eventsListener.onGoBack();
        verify(mockWebView).goBack();

        assertEquals("url", eventsListener.getCurrentURL());

        assertTrue(eventsListener.canGoBack());

        assertTrue(eventsListener.canGoForward());

        // TODO: 2/10/21 verify broadcast
        eventsListener.closeBrowser();
    }
}