package org.prebid.mobile.rendering.views.webview;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class WebViewInterstitialTest {

    private Context mContext;
    private PreloadManager.PreloadedListener mMockPreloadListener;
    private MraidEventsManager.MraidListener mMockMraidListener;
    private String mAdHTML;

    @Before
    public void setup() throws IOException {
        ManagersResolver mockResolver = mock(ManagersResolver.class);
        when(mockResolver.getDeviceManager()).thenReturn(mock(DeviceInfoManager.class));

        mContext = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(mContext);

        mMockPreloadListener = mock(PreloadManager.PreloadedListener.class);

        mMockMraidListener = mock(MraidEventsManager.MraidListener.class);

        mAdHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");
    }

    @Test
    public void initTest() {
        WebViewInterstitial webViewInterstitial = new WebViewInterstitial(mContext, mAdHTML, 100, 200, mMockPreloadListener, mMockMraidListener);
        assertNotNull(webViewInterstitial.getMRAIDInterface());
    }

    @Test
    public void setJSNameTest(){
        WebViewInterstitial webViewInterstitial = new WebViewInterstitial(mContext, mAdHTML, 100, 200, mMockPreloadListener, mMockMraidListener);
        webViewInterstitial.setJSName("test");
        assertEquals("test", webViewInterstitial.mMRAIDBridgeName);
    }
}