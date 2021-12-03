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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.R;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BrowserControlsTest {

    private BrowserControls mBrowserControls;
    private Context mContext;
    private BrowserControlsEventsListener mMockListener;

    @Before
    public void setUp() throws Exception {
        mContext = spy(Robolectric.buildActivity(Activity.class).create().get());
        mMockListener = mock(BrowserControlsEventsListener.class);

        mBrowserControls = new BrowserControls(mContext, mMockListener);
    }

    @Test
    public void updateNavigationButtonsStateTest() throws IllegalAccessException {
        Field backBtnField = WhiteBox.field(BrowserControls.class, "mBackBtn");
        Field forthBtnField = WhiteBox.field(BrowserControls.class, "mForthBtn");
        Button backBtn = spy((Button) backBtnField.get(mBrowserControls));
        Button forthBtn = spy((Button) forthBtnField.get(mBrowserControls));
        backBtnField.set(mBrowserControls, backBtn);
        forthBtnField.set(mBrowserControls, forthBtn);

        mBrowserControls.updateNavigationButtonsState();
        verify(backBtn).setBackgroundResource(eq(R.drawable.prebid_ic_back_inactive));
        verify(forthBtn).setBackgroundResource(eq(R.drawable.prebid_ic_forth_inactive));

        when(mMockListener.canGoBack()).thenReturn(true);
        when(mMockListener.canGoForward()).thenReturn(true);
        mBrowserControls.updateNavigationButtonsState();
        verify(backBtn).setBackgroundResource(eq(R.drawable.prebid_ic_back_active));
        verify(forthBtn).setBackgroundResource(eq(R.drawable.prebid_ic_forth_active));
    }

    @Test
    public void openURLInExternalBrowserTest() {
        mBrowserControls.openURLInExternalBrowser("tel:");
        verify(mContext).startActivity(any(Intent.class));
    }

    @Test
    public void showHideNavigationControlsTest() throws IllegalAccessException {
        Field leftPartField = WhiteBox.field(BrowserControls.class, "mLeftPart");

        mBrowserControls.showNavigationControls();
        Assert.assertEquals(View.VISIBLE, ((LinearLayout) leftPartField.get(mBrowserControls)).getVisibility());

        mBrowserControls.hideNavigationControls();
        Assert.assertEquals(View.GONE, ((LinearLayout) leftPartField.get(mBrowserControls)).getVisibility());
    }

    @Test
    public void clickListenersTest() throws IllegalAccessException {
        Button closeBtn = (Button) WhiteBox.field(BrowserControls.class, "mCloseBtn").get(mBrowserControls);
        Button backBtn = (Button) WhiteBox.field(BrowserControls.class, "mBackBtn").get(mBrowserControls);
        Button forthBtn = (Button) WhiteBox.field(BrowserControls.class, "mForthBtn").get(mBrowserControls);
        Button refreshBtn = (Button) WhiteBox.field(BrowserControls.class, "mRefreshBtn").get(mBrowserControls);
        Button openInExternalBtn = (Button) WhiteBox.field(BrowserControls.class, "mOpenInExternalBrowserBtn").get(mBrowserControls);

        closeBtn.callOnClick();
        verify(mMockListener).closeBrowser();

        backBtn.callOnClick();
        verify(mMockListener).onGoBack();

        forthBtn.callOnClick();
        verify(mMockListener).onGoForward();

        refreshBtn.callOnClick();
        verify(mMockListener).onRelaod();

        openInExternalBtn.callOnClick();
        verify(mMockListener).getCurrentURL();
        verify(mContext, times(0)).startActivity(any(Intent.class));

        reset(mMockListener);
        when(mMockListener.getCurrentURL()).thenReturn("url");
        openInExternalBtn.callOnClick();
        verify(mMockListener).getCurrentURL();
        verify(mContext).startActivity(any(Intent.class));
    }
}