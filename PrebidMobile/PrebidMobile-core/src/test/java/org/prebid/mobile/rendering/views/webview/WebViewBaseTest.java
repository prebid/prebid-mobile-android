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

package org.prebid.mobile.rendering.views.webview;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class WebViewBaseTest {

    private WebViewBase webViewBase;
    private Context context;
    private PreloadManager.PreloadedListener mockPreloadListener;
    private MraidEventsManager.MraidListener mockMraidListener;
    private String adHTML;

    @Before
    public void setup() throws IOException {
        context = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(context);

        mockPreloadListener = mock(PreloadManager.PreloadedListener.class);

        mockMraidListener = mock(MraidEventsManager.MraidListener.class);

        adHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");
        webViewBase = new WebViewBase(context, adHTML, 100, 200, mockPreloadListener, mockMraidListener);
    }

    @Test
    public void testCheckIFrameHtmlTagContainsIFrame() throws IOException {
        webViewBase.initContainsIFrame(ResourceUtils.convertResourceToString("ad_contains_iframe"));

        Assert.assertTrue(webViewBase.containsIFrame());
    }

    @Test
    public void testCheckIFrameHtmlTagNotContainsIFrame() throws IOException {
        webViewBase.initContainsIFrame(ResourceUtils.convertResourceToString("ad_no_iframe"));

        assertFalse(webViewBase.containsIFrame());
    }

    @Test
    public void setJSNameTest() {
        webViewBase.setJSName("test");
        assertEquals("test", webViewBase.MRAIDBridgeName);
    }

    @Test
    public void isClickedTest() throws IllegalAccessException {
        Field field = WhiteBox.field(WebViewBase.class, "isClicked");
        field.set(webViewBase, true);
        assertEquals(field.get(webViewBase), webViewBase.isClicked());
    }

    @Test
    public void setIsClickedTest() throws IllegalAccessException {
        Field field = WhiteBox.field(WebViewBase.class, "isClicked");
        assertFalse((Boolean) field.get(webViewBase));
        webViewBase.setIsClicked(true);
        assertTrue((Boolean) field.get(webViewBase));
    }

    @Test
    public void initLoadTest() throws IllegalAccessException, IOException {
        webViewBase.initLoad();
        String newAdHtml = (String) WhiteBox.field(WebViewBase.class, "adHTML").get(webViewBase);
        assertNotEquals(adHTML, newAdHtml);

        adHTML = ResourceUtils.convertResourceToString("ad_mraid_html.txt");
        webViewBase = new WebViewBase(context, adHTML, 100, 200, mockPreloadListener, mockMraidListener);
        webViewBase.initLoad();
        newAdHtml = (String) WhiteBox.field(WebViewBase.class, "adHTML").get(webViewBase);
        assertNotEquals(adHTML, newAdHtml);
    }

    @Test
    public void setGetAdHeightTest() {
        assertEquals(200, webViewBase.getAdHeight());
        webViewBase.setAdHeight(100);
        assertEquals(100, webViewBase.getAdHeight());
    }

    @Test
    public void setGetAdWidthTest() {
        assertEquals(100, webViewBase.getAdWidth());
        webViewBase.setAdWidth(200);
        assertEquals(200, webViewBase.getAdWidth());
    }

    @Test
    public void getPreloadedListenerTest() {
        assertEquals(mockPreloadListener, webViewBase.getPreloadedListener());
    }

    @Test
    public void adAssetsLoadedTest() {
        BaseJSInterface mockBaseJsInterface = mock(BaseJSInterface.class);
        webViewBase.setBaseJSInterface(mockBaseJsInterface);

        webViewBase.isMRAID = false;
        webViewBase.adAssetsLoaded();
        verify(mockBaseJsInterface, times(0)).prepareAndSendReady();
        verify(mockPreloadListener, times(1)).preloaded(eq(webViewBase));

        webViewBase.isMRAID = true;
        webViewBase.adAssetsLoaded();
        verify(mockBaseJsInterface, times(1)).prepareAndSendReady();
        verify(mockPreloadListener, times(2)).preloaded(eq(webViewBase));
    }

    @Test
    public void setGetDialogTest() {
        AdBaseDialog mockDialog = mock(AdBaseDialog.class);
        assertNull(webViewBase.getDialog());

        webViewBase.setDialog(mockDialog);
        assertEquals(mockDialog, webViewBase.getDialog());
    }

    @Test
    public void loadAdTest(){
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;

        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            x,
            y,
            metaState
        );

        webViewBase.loadAd();
        assertTrue(webViewBase.dispatchTouchEvent(motionEvent));
        assertTrue(webViewBase.isClicked());
    }

    @Test
    public void getParentContainerTest(){
        FrameLayout parentLayout = new FrameLayout(context);
        parentLayout.addView(webViewBase);
        assertEquals(parentLayout, webViewBase.getParentContainer());
    }

    @Test
    public void detatchFromParentTest(){
        FrameLayout parentLayout = new FrameLayout(context);
        parentLayout.addView(webViewBase);
        webViewBase.detachFromParent();
        assertNull(webViewBase.getParentContainer());
    }

    @Test
    public void isMRAIDTest() throws IllegalAccessException {
        assertFalse(webViewBase.isMRAID());
        WhiteBox.field(WebViewBase.class, "isMRAID").set(webViewBase, true);
        assertTrue(webViewBase.isMRAID());
    }

    @Test
    public void sendClickCallbackTest(){
        webViewBase.sendClickCallBack("test");
        verify(mockMraidListener).openMraidExternalLink("test");
    }

    @Test
    public void startLoadingAssetsTest() throws IllegalAccessException {
        BaseJSInterface mockInterface = mock(BaseJSInterface.class);
        WhiteBox.field(WebViewBase.class, "mraidInterface").set(webViewBase, mockInterface);
        webViewBase.startLoadingAssets();
        verify(mockInterface).loading();
    }

    @Test
    public void onWindowFocusChanged_NotifyMRaidListener() {
        webViewBase.onWindowFocusChanged(false);
        verify(mockMraidListener).onAdWebViewWindowFocusChanged(false);

        webViewBase.onWindowFocusChanged(true);
        verify(mockMraidListener).onAdWebViewWindowFocusChanged(true);
    }

    @Test
    public void destroyTest() throws IllegalAccessException {
        BaseJSInterface mockInterface = mock(BaseJSInterface.class);
        WhiteBox.field(WebViewBase.class, "mraidInterface").set(webViewBase, mockInterface);
        webViewBase.destroy();
        verify(mockInterface).destroy();
    }
}
