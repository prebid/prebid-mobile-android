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

package org.prebid.mobile.mopub.adapters;

import android.app.Activity;
import android.content.Context;

import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.MoPubNative;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.display.MoPubBannerAdUnit;
import org.prebid.mobile.rendering.bidding.display.MoPubInterstitialAdUnit;
import org.prebid.mobile.rendering.bidding.display.MoPubNativeAdUnit;
import org.prebid.mobile.rendering.bidding.display.MoPubRewardedVideoAdUnit;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class MoPubAdUnitTests {

    private Context mContext;

    private static final AdSize AD_SIZE = new AdSize(1, 1);
    private static final String ID = "configId";

    private MoPubView mMoPubView;
    private MoPubInterstitial mMoPubInterstitial;

    private MoPubBannerAdUnit mMoPubBannerAdUnit;
    private MoPubInterstitialAdUnit mMoPubInterstitialAdUnit;
    private MoPubNativeAdUnit mMoPubNativeAdUnit;
    private MoPubRewardedVideoAdUnit mMoPubRewardedAdUnit;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mMoPubView = new MoPubView(mContext);
        mMoPubInterstitial = new MoPubInterstitial((Activity) mContext, ID);

        mMoPubNativeAdUnit = new MoPubNativeAdUnit(mContext, ID, mock(NativeAdConfiguration.class));
        mMoPubBannerAdUnit = new MoPubBannerAdUnit(mContext, ID, AD_SIZE);
        mMoPubInterstitialAdUnit = new MoPubInterstitialAdUnit(mContext, ID, AD_SIZE);
        mMoPubRewardedAdUnit = new MoPubRewardedVideoAdUnit(mContext, ID, ID);
    }

    @Test
    public void whenIsMopubNativeViewAndMoPubNativeViewPassed_ReturnTrue() {
        MoPubNative moPubNative = new MoPubNative(mContext, "", mock(MoPubNative.MoPubNativeNetworkListener.class));

        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubNativeAdUnit.class, mMoPubNativeAdUnit, moPubNative);

        assertTrue(isAdObjectSupported);
    }

    @Test
    public void whenIsMopubNativeViewAndAnyObjectPassed_ReturnFalse() {
        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubNativeAdUnit.class, mMoPubNativeAdUnit, mMoPubInterstitial);

        assertFalse(isAdObjectSupported);
    }

    @Test
    public void whenIsMopubBannerViewAndMoPubBannerViewPassed_ReturnTrue() {
        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubBannerAdUnit.class, mMoPubBannerAdUnit, mMoPubView);

        assertTrue(isAdObjectSupported);
    }

    @Test
    public void whenIsMopubBannerViewAndAnyObjectPassed_ReturnFalse() {
        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubBannerAdUnit.class, mMoPubBannerAdUnit, mMoPubInterstitial);

        assertFalse(isAdObjectSupported);
    }

    @Test
    public void whenIsMopubInterstitialViewAndMoPubInterstitialPassed_ReturnTrue() {
        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubInterstitialAdUnit.class, mMoPubInterstitialAdUnit, mMoPubInterstitial);

        assertTrue(isAdObjectSupported);
    }

    @Test
    public void whenIsMopubInterstitialViewAndAnyObjectPassed_ReturnFalse() {
        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubInterstitialAdUnit.class, mMoPubInterstitialAdUnit, mMoPubView);

        assertFalse(isAdObjectSupported);
    }

    @Test
    public void whenIsMopubRewardedViewAndHashMapPassed_ReturnTrue() {
        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubRewardedVideoAdUnit.class, mMoPubRewardedAdUnit, new HashMap<String, String>());

        assertTrue(isAdObjectSupported);
    }

    @Test
    public void whenIsMopubRewardedViewAndAnyObjectPassed_ReturnFalse() {
        final boolean isAdObjectSupported =
            invokeIsAdObjectSupported(MoPubRewardedVideoAdUnit.class, mMoPubRewardedAdUnit, mMoPubView);

        assertFalse(isAdObjectSupported);
    }

    private Boolean invokeIsAdObjectSupported(Class<?> declaringClass, Object objectToInvoke, Object parameter) {
        try {
            return ((Boolean) WhiteBox.method(declaringClass, "isAdObjectSupported", Object.class)
                                      .invoke(objectToInvoke, parameter));
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return false;
    }
}
