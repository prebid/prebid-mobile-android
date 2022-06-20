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

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidStorePictureTest {

    private MraidStorePicture mraidStorePicture;
    private Context context;
    private WebViewBase mockWebViewBase;
    private BaseJSInterface mockBaseJsInterface;

    private JsExecutor mockJsExecutor;

    @Before
    public void setup() {
        context = spy(Robolectric.buildActivity(Activity.class).create().get());
        ManagersResolver.getInstance().prepare(context);
        mockWebViewBase = Mockito.mock(WebViewBase.class);
        mockJsExecutor = mock(JsExecutor.class);

        when(mockWebViewBase.post(any(Runnable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }
        });
        mockBaseJsInterface = spy(new BaseJSInterface(context, mockWebViewBase, mockJsExecutor));

        mraidStorePicture = new MraidStorePicture(context, mockBaseJsInterface, mockWebViewBase);
    }

    @Test
    public void storePictureTest() {
        mraidStorePicture.storePicture("test_url");
        verify((Activity) context).isFinishing();
    }

    @Test
    public void storePictureErrorPrivateTest() throws Exception {
        Method method = WhiteBox.method(MraidStorePicture.class, "storePicture");

        method.invoke(mraidStorePicture);
        verify(mockBaseJsInterface, timeout(5000)).onError("store_picture", JSInterface.ACTION_STORE_PICTURE);
    }
}