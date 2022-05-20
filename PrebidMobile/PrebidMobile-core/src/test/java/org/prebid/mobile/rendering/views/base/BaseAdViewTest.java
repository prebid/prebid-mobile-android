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

package org.prebid.mobile.rendering.views.base;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BaseAdViewTest {

    private BaseAdView baseAdView;
    private Context mockContext;
    private AdViewManager mockAdViewManager;

    @Before
    public void setUp() throws Exception {
        mockContext = spy(Robolectric.buildActivity(Activity.class).create().get());

        baseAdView = new BaseAdView(mockContext) {
            @Override
            protected void notifyErrorListeners(AdException adException) {

            }
        };
        mockAdViewManager = Mockito.mock(AdViewManager.class);
        when(mockAdViewManager.getAdConfiguration()).thenReturn(new AdUnitConfiguration());
        Field field = WhiteBox.field(BaseAdView.class, "adViewManager");
        field.set(baseAdView, mockAdViewManager);
    }

    @Test
    public void getMediaDurationTest() {
        when(mockAdViewManager.getMediaDuration()).thenReturn(0L);
        assertEquals(0, baseAdView.getMediaDuration());
    }

    @Test
    public void whenGetMediaOffsetEmpty_ReturnDefault() {
        when(mockAdViewManager.getSkipOffset()).thenReturn(0L);
        assertEquals(0, baseAdView.getMediaOffset());
    }

    @Test
    public void onWindowFocusChangedTest() throws IllegalAccessException {
        WhiteBox.field(BaseAdView.class, "screenVisibility").set(baseAdView, -1);

        baseAdView.onWindowFocusChanged(true);
        verify(mockAdViewManager).setAdVisibility(eq(View.VISIBLE));
    }

}