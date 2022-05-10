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

package org.prebid.mobile.rendering.models;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.video.ExoPlayerView;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBanner;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewInterstitial;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class ViewPoolTest {

    private ViewPool viewPool;
    private Context context;
    private View occupiedView;
    private View unoccupiedView;
    private FrameLayout container;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        viewPool = ViewPool.getInstance();
        viewPool.clear();
        occupiedView = new View(context);
        unoccupiedView = new View(context);
        container = new FrameLayout(context);
        container.addView(occupiedView);

        viewPool.addToOccupied(occupiedView);
        viewPool.addToUnoccupied(unoccupiedView);
    }

    @After
    public void clearInstance() throws IllegalAccessException {
        WhiteBox.setInternalState(viewPool, "sInstance", null);
    }

    @Test
    public void sizeOfOccupiedTest() {
        assertEquals(1, viewPool.sizeOfOccupied());
    }

    @Test
    public void sizeOfUnoccupiedTest() {
        assertEquals(1, viewPool.sizeOfUnoccupied());
    }

    @Test
    public void addToOccupiedTest() {
        viewPool.addToOccupied(new View(context));
        assertEquals(2, viewPool.sizeOfOccupied());
    }

    @Test
    public void addToUnoccupiedTest() {
        viewPool.addToUnoccupied(new View(context));
        assertEquals(2, viewPool.sizeOfUnoccupied());
    }

    @Test
    public void swapToUnoccupiedTest() {
        viewPool.swapToUnoccupied(occupiedView);
        assertNull(occupiedView.getParent());
        assertEquals(0, viewPool.sizeOfOccupied());
        assertEquals(2, viewPool.sizeOfUnoccupied());
    }

    @Test
    public void clearTest() {
        viewPool.clear();
        assertEquals(0, viewPool.sizeOfUnoccupied());
        assertEquals(0, viewPool.sizeOfOccupied());
    }

    @Test
    public void getUnoccupiedViewTest() throws AdException {
        VideoCreativeViewListener mockVideoCreativeViewListener = mock(VideoCreativeViewListener.class);
        AdFormat adType = AdFormat.BANNER;
        InterstitialManager mockInterstitialManager = mock(InterstitialManager.class);

        View result = viewPool.getUnoccupiedView(
                context,
                mockVideoCreativeViewListener,
                adType,
                mockInterstitialManager
        );
        assertEquals(unoccupiedView, result);
        assertEquals(2, viewPool.sizeOfOccupied());
        assertEquals(0, viewPool.sizeOfUnoccupied());

        result = viewPool.getUnoccupiedView(context, mockVideoCreativeViewListener, adType, mockInterstitialManager);
        assertThat(result, instanceOf(PrebidWebViewBanner.class));
        assertEquals(3, viewPool.sizeOfOccupied());
        assertEquals(0, viewPool.sizeOfUnoccupied());

        adType = AdFormat.VAST;
        result = viewPool.getUnoccupiedView(context, mock(VideoCreative.class), adType, mockInterstitialManager);
        assertThat(result, instanceOf(ExoPlayerView.class));
        assertEquals(4, viewPool.sizeOfOccupied());
        assertEquals(0, viewPool.sizeOfUnoccupied());

        adType = AdFormat.INTERSTITIAL;
        result = viewPool.getUnoccupiedView(context, mockVideoCreativeViewListener, adType, mockInterstitialManager);
        assertThat(result, instanceOf(PrebidWebViewInterstitial.class));
        assertEquals(5, viewPool.sizeOfOccupied());
        assertEquals(0, viewPool.sizeOfUnoccupied());
    }
}