package com.openx.apollo.mraid.methods;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.openx.apollo.mraid.methods.network.RedirectUrlListener;
import com.openx.apollo.utils.url.UrlHandler;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidUrlHandlerTest {

    private MraidUrlHandler mMraidUrlHandler;

    private BaseJSInterface mMockBaseJsInterface;
    private Context mMockContext;
    private WebViewBase mMockWebViewBase;
    private UrlHandler mMockUrlHandler;

    @Before
    public void setup() {
        mMockContext = mock(Context.class);
        mMockBaseJsInterface = mock(BaseJSInterface.class);
        mMockWebViewBase = mock(WebViewBase.class);
        mMockUrlHandler = mock(UrlHandler.class);

        mMraidUrlHandler = spy(new MraidUrlHandler(mMockContext, mMockBaseJsInterface));

        when(mMraidUrlHandler.createUrlHandler(anyInt())).thenReturn(mMockUrlHandler);
    }

    @Test
    public void openTest() {

        doAnswer(invocation -> {
            RedirectUrlListener listener = invocation.getArgumentAt(1, RedirectUrlListener.class);
            listener.onSuccess(invocation.getArgumentAt(0, String.class), "html");

            return null;
        }).when(mMockBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);
        when(mMockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockPackageManager.queryIntentActivities(any(Intent.class), eq(PackageManager.MATCH_DEFAULT_ONLY))).thenReturn(new ArrayList<>());

        mMraidUrlHandler.open("http:", -1);
        verify(mMockUrlHandler).handleUrl(mMockContext, "http:", null, true);
    }

    @Test
    public void destroyTest() {
        mMraidUrlHandler.destroy();
        verify(mMockBaseJsInterface).destroy();
    }
}
