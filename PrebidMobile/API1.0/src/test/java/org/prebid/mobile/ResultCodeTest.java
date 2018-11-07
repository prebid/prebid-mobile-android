package org.prebid.mobile;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class ResultCodeTest extends BaseSetup {
    @Test
    public void testInvalidAccountId() throws Exception {
        PrebidMobile.setAccountId("");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, activity, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_ACCOUNT_ID);
    }

    @Test
    public void testInvalidConfigId() throws Exception {
        PrebidMobile.setAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, activity, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_CONFIG_ID);
    }

    @Test
    public void testInvalidHostUrl() throws Exception {
        PrebidMobile.setAccountId("123456");
        Host.CUSTOM.setHostUrl("");
        PrebidMobile.setHost(Host.CUSTOM);
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, activity, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_HOST_URL);
    }

    @Test
    public void testDoNotSupportMultipleSizesForMoPubBanner() throws Exception {
        PrebidMobile.setAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, activity, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_SIZE);
    }

    @Test
    public void testSupportMultipleSizesForDFPBanner() throws Exception {
        PrebidMobile.setAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        adUnit.fetchDemand(builder.build(), activity, mockListener);
        verify(mockListener, never()).onComplete(ResultCode.INVALID_SIZE);
    }
}
