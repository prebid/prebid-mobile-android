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

package org.prebid.mobile.rendering.mraid.methods;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
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
            RedirectUrlListener listener = invocation.getArgument(1);
            listener.onSuccess(invocation.getArgument(0), "html");

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
