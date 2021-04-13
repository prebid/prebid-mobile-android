package org.prebid.mobile.rendering.views.webview;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class WebViewBannerTest {

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
    }

    @Test
    public void initTest(){
        WebViewBanner webViewBanner = new WebViewBanner(mContext, mAdHTML, 100, 200,  mMockPreloadListener, mMockMraidListener);
        assertNotNull(webViewBanner.getMRAIDInterface());

        webViewBanner = new WebViewBanner(mContext, mock(PrebidWebViewBase.class), mMockMraidListener);
        assertNotNull(webViewBanner.getMRAIDInterface());
    }
}