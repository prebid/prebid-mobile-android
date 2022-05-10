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

package org.prebid.mobile.rendering.views.video;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoDialogTest {

    @Mock
    InterstitialManager mockInterstitialManager;

    private VideoDialog videoDialog;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();
        videoDialog = spy(new VideoDialog(context, mock(VideoCreativeView.class), mock(AdViewManager.class),
                mockInterstitialManager, mock(FrameLayout.class)));
    }

    @Test
    public void handleCloseClick_DismissDialog() {
        videoDialog.handleCloseClick();
        verify(videoDialog).dismiss();
    }

    // @Test
    // public void whenClose_NotifyVideoAdManager() {
    //     videoDialog.close();
    //     verify(mockInterstitialManager).videoAdViewInterstitialAdClosed();
    // }

    @Test
    public void showBannerCreative_NullAdView() throws IllegalAccessException {
        InterstitialDisplayPropertiesInternal propertiesInternal = new InterstitialDisplayPropertiesInternal();
        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(propertiesInternal);
        videoDialog.showBannerCreative(mock(View.class));
        Object adView = WhiteBox.field(VideoDialog.class, "adView").get(videoDialog);
        Assert.assertNull(adView);
    }
}