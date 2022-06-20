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
import org.prebid.mobile.core.R;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BrowserControlsTest {

    private BrowserControls browserControls;
    private Context context;
    private BrowserControlsEventsListener mockListener;

    @Before
    public void setUp() throws Exception {
        context = spy(Robolectric.buildActivity(Activity.class).create().get());
        mockListener = mock(BrowserControlsEventsListener.class);

        browserControls = new BrowserControls(context, mockListener);
    }

    @Test
    public void updateNavigationButtonsStateTest() throws IllegalAccessException {
        Field backBtnField = WhiteBox.field(BrowserControls.class, "backBtn");
        Field forthBtnField = WhiteBox.field(BrowserControls.class, "forthBtn");
        Button backBtn = spy((Button) backBtnField.get(browserControls));
        Button forthBtn = spy((Button) forthBtnField.get(browserControls));
        backBtnField.set(browserControls, backBtn);
        forthBtnField.set(browserControls, forthBtn);

        browserControls.updateNavigationButtonsState();
        verify(backBtn).setBackgroundResource(eq(R.drawable.prebid_ic_back_inactive));
        verify(forthBtn).setBackgroundResource(eq(R.drawable.prebid_ic_forth_inactive));

        when(mockListener.canGoBack()).thenReturn(true);
        when(mockListener.canGoForward()).thenReturn(true);
        browserControls.updateNavigationButtonsState();
        verify(backBtn).setBackgroundResource(eq(R.drawable.prebid_ic_back_active));
        verify(forthBtn).setBackgroundResource(eq(R.drawable.prebid_ic_forth_active));
    }

    @Test
    public void openURLInExternalBrowserTest() {
        browserControls.openURLInExternalBrowser("tel:");
        verify(context).startActivity(any(Intent.class));
    }

    @Test
    public void showHideNavigationControlsTest() throws IllegalAccessException {
        Field leftPartField = WhiteBox.field(BrowserControls.class, "leftPart");

        browserControls.showNavigationControls();
        Assert.assertEquals(View.VISIBLE, ((LinearLayout) leftPartField.get(browserControls)).getVisibility());

        browserControls.hideNavigationControls();
        Assert.assertEquals(View.GONE, ((LinearLayout) leftPartField.get(browserControls)).getVisibility());
    }

    @Test
    public void clickListenersTest() throws IllegalAccessException {
        Button closeBtn = (Button) WhiteBox.field(BrowserControls.class, "closeBtn").get(browserControls);
        Button backBtn = (Button) WhiteBox.field(BrowserControls.class, "backBtn").get(browserControls);
        Button forthBtn = (Button) WhiteBox.field(BrowserControls.class, "forthBtn").get(browserControls);
        Button refreshBtn = (Button) WhiteBox.field(BrowserControls.class, "refreshBtn").get(browserControls);
        Button openInExternalBtn = (Button) WhiteBox.field(BrowserControls.class, "openInExternalBrowserBtn")
                                                    .get(browserControls);

        closeBtn.callOnClick();
        verify(mockListener).closeBrowser();

        backBtn.callOnClick();
        verify(mockListener).onGoBack();

        forthBtn.callOnClick();
        verify(mockListener).onGoForward();

        refreshBtn.callOnClick();
        verify(mockListener).onRelaod();

        openInExternalBtn.callOnClick();
        verify(mockListener).getCurrentURL();
        verify(context, times(0)).startActivity(any(Intent.class));

        reset(mockListener);
        when(mockListener.getCurrentURL()).thenReturn("url");
        openInExternalBtn.callOnClick();
        verify(mockListener).getCurrentURL();
        verify(context).startActivity(any(Intent.class));
    }
}