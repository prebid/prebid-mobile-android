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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class WebViewBaseTest {

    private WebViewBase mWebViewBase;
    private Context mContext;
    private PreloadManager.PreloadedListener mMockPreloadListener;
    private MraidEventsManager.MraidListener mMockMraidListener;
    private String mAdHTML;

    @Before
    public void setup() throws IOException {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(mContext);

        mMockPreloadListener = mock(PreloadManager.PreloadedListener.class);

        mMockMraidListener = mock(MraidEventsManager.MraidListener.class);

        mAdHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");
        mWebViewBase = new WebViewBase(mContext, mAdHTML, 100, 200, mMockPreloadListener, mMockMraidListener);
    }

    @Test
    public void testCheckIFrameHtmlTagContainsIFrame() throws IOException {
        mWebViewBase.initContainsIFrame(ResourceUtils.convertResourceToString("ad_contains_iframe"));

        Assert.assertTrue(mWebViewBase.containsIFrame());
    }

    @Test
    public void testCheckIFrameHtmlTagNotContainsIFrame() throws IOException {
        mWebViewBase.initContainsIFrame(ResourceUtils.convertResourceToString("ad_no_iframe"));

        assertFalse(mWebViewBase.containsIFrame());
    }

    @Test
    public void setJSNameTest() {
        mWebViewBase.setJSName("test");
        assertEquals("test", mWebViewBase.mMRAIDBridgeName);
    }

    @Test
    public void isClickedTest() throws IllegalAccessException {
        Field field = WhiteBox.field(WebViewBase.class, "mIsClicked");
        field.set(mWebViewBase, true);
        assertEquals(field.get(mWebViewBase), mWebViewBase.isClicked());
    }

    @Test
    public void setIsClickedTest() throws IllegalAccessException {
        Field field = WhiteBox.field(WebViewBase.class, "mIsClicked");
        assertFalse((Boolean) field.get(mWebViewBase));
        mWebViewBase.setIsClicked(true);
        assertTrue((Boolean) field.get(mWebViewBase));
    }

    @Test
    public void initLoadTest() throws IllegalAccessException, IOException {
        mWebViewBase.initLoad();
        String newAdHtml = (String) WhiteBox.field(WebViewBase.class, "mAdHTML").get(mWebViewBase);
        assertNotEquals(mAdHTML, newAdHtml);

        mAdHTML = ResourceUtils.convertResourceToString("ad_mraid_html.txt");
        mWebViewBase = new WebViewBase(mContext, mAdHTML, 100, 200, mMockPreloadListener, mMockMraidListener);
        mWebViewBase.initLoad();
        newAdHtml = (String) WhiteBox.field(WebViewBase.class, "mAdHTML").get(mWebViewBase);
        assertNotEquals(mAdHTML, newAdHtml);
    }

    @Test
    public void setGetAdHeightTest() {
        assertEquals(200, mWebViewBase.getAdHeight());
        mWebViewBase.setAdHeight(100);
        assertEquals(100, mWebViewBase.getAdHeight());
    }

    @Test
    public void setGetAdWidthTest() {
        assertEquals(100, mWebViewBase.getAdWidth());
        mWebViewBase.setAdWidth(200);
        assertEquals(200, mWebViewBase.getAdWidth());
    }

    @Test
    public void getPreloadedListenerTest() {
        assertEquals(mMockPreloadListener, mWebViewBase.getPreloadedListener());
    }

    @Test
    public void adAssetsLoadedTest() {
        BaseJSInterface mockBaseJsInterface = mock(BaseJSInterface.class);
        mWebViewBase.setBaseJSInterface(mockBaseJsInterface);

        mWebViewBase.mIsMRAID = false;
        mWebViewBase.adAssetsLoaded();
        verify(mockBaseJsInterface, times(0)).prepareAndSendReady();
        verify(mMockPreloadListener, times(1)).preloaded(eq(mWebViewBase));

        mWebViewBase.mIsMRAID = true;
        mWebViewBase.adAssetsLoaded();
        verify(mockBaseJsInterface, times(1)).prepareAndSendReady();
        verify(mMockPreloadListener, times(2)).preloaded(eq(mWebViewBase));
    }

    @Test
    public void setGetDialogTest() {
        AdBaseDialog mockDialog = mock(AdBaseDialog.class);
        assertNull(mWebViewBase.getDialog());

        mWebViewBase.setDialog(mockDialog);
        assertEquals(mockDialog, mWebViewBase.getDialog());
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

        mWebViewBase.loadAd();
        assertTrue(mWebViewBase.dispatchTouchEvent(motionEvent));
        assertTrue(mWebViewBase.isClicked());
    }

    @Test
    public void getParentContainerTest(){
        FrameLayout parentLayout = new FrameLayout(mContext);
        parentLayout.addView(mWebViewBase);
        assertEquals(parentLayout, mWebViewBase.getParentContainer());
    }

    @Test
    public void detatchFromParentTest(){
        FrameLayout parentLayout = new FrameLayout(mContext);
        parentLayout.addView(mWebViewBase);
        mWebViewBase.detachFromParent();
        assertNull(mWebViewBase.getParentContainer());
    }

    @Test
    public void isMRAIDTest() throws IllegalAccessException {
        assertFalse(mWebViewBase.isMRAID());
        WhiteBox.field(WebViewBase.class, "mIsMRAID").set(mWebViewBase, true);
        assertTrue(mWebViewBase.isMRAID());
    }

    @Test
    public void sendClickCallbackTest(){
        mWebViewBase.sendClickCallBack("test");
        verify(mMockMraidListener).openMraidExternalLink("test");
    }

    @Test
    public void startLoadingAssetsTest() throws IllegalAccessException {
        BaseJSInterface mockInterface = mock(BaseJSInterface.class);
        WhiteBox.field(WebViewBase.class, "mMraidInterface").set(mWebViewBase, mockInterface);
        mWebViewBase.startLoadingAssets();
        verify(mockInterface).loading();
    }

    @Test
    public void onWindowFocusChanged_NotifyMRaidListener() {
        mWebViewBase.onWindowFocusChanged(false);
        verify(mMockMraidListener).onAdWebViewWindowFocusChanged(false);

        mWebViewBase.onWindowFocusChanged(true);
        verify(mMockMraidListener).onAdWebViewWindowFocusChanged(true);
    }

    @Test
    public void destroyTest() throws IllegalAccessException {
        BaseJSInterface mockInterface = mock(BaseJSInterface.class);
        WhiteBox.field(WebViewBase.class, "mMraidInterface").set(mWebViewBase, mockInterface);
        mWebViewBase.destroy();
        verify(mockInterface).destroy();
    }
}
