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

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidUrlHandlerTest {

    private MraidUrlHandler mraidUrlHandler;

    private BaseJSInterface mockBaseJsInterface;
    private Context mockContext;
    private WebViewBase mockWebViewBase;
    private UrlHandler mockUrlHandler;

    @Before
    public void setup() {
        mockContext = mock(Context.class);
        mockBaseJsInterface = mock(BaseJSInterface.class);
        mockWebViewBase = mock(WebViewBase.class);
        mockUrlHandler = mock(UrlHandler.class);

        mraidUrlHandler = spy(new MraidUrlHandler(mockContext, mockBaseJsInterface));

        when(mraidUrlHandler.createUrlHandler(anyInt())).thenReturn(mockUrlHandler);
    }

    @Test
    public void openTest() {

        doAnswer(invocation -> {
            RedirectUrlListener listener = invocation.getArgument(1);
            listener.onSuccess(invocation.getArgument(0), "html");

            return null;
        }).when(mockBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockPackageManager.queryIntentActivities(
                any(Intent.class),
                eq(PackageManager.MATCH_DEFAULT_ONLY)
        )).thenReturn(new ArrayList<>());

        mraidUrlHandler.open("http:", -1);
        verify(mockUrlHandler).handleUrl(mockContext, "http:", null, true);
    }

    @Test
    public void destroyTest() {
        mraidUrlHandler.destroy();
        verify(mockBaseJsInterface).destroy();
    }
}
