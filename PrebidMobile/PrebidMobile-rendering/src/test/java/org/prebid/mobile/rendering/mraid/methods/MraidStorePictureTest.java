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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidStorePictureTest {

    private MraidStorePicture mMraidStorePicture;
    private Context mContext;
    private WebViewBase mMockWebViewBase;
    private BaseJSInterface mMockBaseJsInterface;

    private JsExecutor mMockJsExecutor;

    @Before
    public void setup() {
        mContext = spy(Robolectric.buildActivity(Activity.class).create().get());
        ManagersResolver.getInstance().prepare(mContext);
        mMockWebViewBase = Mockito.mock(WebViewBase.class);
        mMockJsExecutor = mock(JsExecutor.class);

        when(mMockWebViewBase.post(any(Runnable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = invocation.getArgumentAt(0, Runnable.class);
                runnable.run();
                return null;
            }
        });
        mMockBaseJsInterface = spy(new BaseJSInterface(mContext, mMockWebViewBase, mMockJsExecutor));

        mMraidStorePicture = new MraidStorePicture(mContext, mMockBaseJsInterface, mMockWebViewBase);
    }

    @Test
    public void storePictureTest() {
        mMraidStorePicture.storePicture("test_url");
        verify((Activity) mContext).isFinishing();
    }

    @Test
    public void storePictureErrorPrivateTest() throws Exception {
        Method method = WhiteBox.method(MraidStorePicture.class, "storePicture");

        method.invoke(mMraidStorePicture);
        verify(mMockBaseJsInterface, timeout(5000)).onError("store_picture", JSInterface.ACTION_STORE_PICTURE);
    }
}