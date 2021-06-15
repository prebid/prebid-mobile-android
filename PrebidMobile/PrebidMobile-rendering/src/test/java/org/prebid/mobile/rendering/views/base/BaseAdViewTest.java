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
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BaseAdViewTest {

    private BaseAdView mBaseAdView;
    private Context mMockContext;
    private AdViewManager mMockAdViewManager;

    @Before
    public void setUp() throws Exception {
        mMockContext = spy(Robolectric.buildActivity(Activity.class).create().get());

        mBaseAdView = new BaseAdView(mMockContext) {
            @Override
            protected void notifyErrorListeners(AdException adException) {

            }
        };
        mMockAdViewManager = Mockito.mock(AdViewManager.class);
        when(mMockAdViewManager.getAdConfiguration()).thenReturn(new AdConfiguration());
        Field field = WhiteBox.field(BaseAdView.class, "mAdViewManager");
        field.set(mBaseAdView, mMockAdViewManager);
    }

    @Test
    public void getMediaDurationTest() {
        when(mMockAdViewManager.getMediaDuration()).thenReturn(0L);
        assertEquals(0, mBaseAdView.getMediaDuration());
    }

    @Test
    public void whenGetMediaOffsetEmpty_ReturnDefault() {
        when(mMockAdViewManager.getSkipOffset()).thenReturn(0L);
        assertEquals(0, mBaseAdView.getMediaOffset());
    }

    @Test
    public void onWindowFocusChangedTest() throws IllegalAccessException {
        WhiteBox.field(BaseAdView.class, "mScreenVisibility").set(mBaseAdView, -1);

        mBaseAdView.onWindowFocusChanged(true);
        verify(mMockAdViewManager).setAdVisibility(eq(View.VISIBLE));
    }

}