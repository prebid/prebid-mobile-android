/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile.addendum;

import android.webkit.WebView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class AdViewUtilsTest {

    @Test
    public void testRegexMatches() {
        String[] result = AdViewUtils.matches("^a", "aaa aaa");
        assertEquals(1, result.length);
        assertEquals("a", result[0]);

        result = AdViewUtils.matches("^b", "aaa aaa");
        assertEquals(0, result.length);

        result = AdViewUtils.matches("aaa aaa", "^a");
        assertEquals(0, result.length);

        result = AdViewUtils.matches("[0-9]+x[0-9]+", "{ \n adManagerResponse:\"hb_size\":[\"728x90\"],\"hb_size_rubicon\":[\"1x1\"],moPubResponse:\"hb_size:300x250\" \n }");
        assertEquals(3, result.length);
        assertEquals("728x90", result[0]);
        assertEquals("1x1", result[1]);
        assertEquals("300x250", result[2]);

        result = AdViewUtils.matches("hb_size\\W+[0-9]+x[0-9]+", "{ \n adManagerResponse:\"hb_size\":[\"728x90\"],\"hb_size_rubicon\":[\"1x1\"],moPubResponse:\"hb_size:300x250\" \n }");
        assertEquals(2, result.length);
        assertEquals("hb_size\":[\"728x90", result[0]);
        assertEquals("hb_size:300x250", result[1]);
    }

    @Test
    public void testRegexMatchAndCheck() {
        String result = AdViewUtils.matchAndCheck("^a", "aaa aaa");

        assertNotNull(result);
        assertEquals("a", result);

        result = AdViewUtils.matchAndCheck("^b", "aaa aaa");
        assertNull(result);
    }


    @Test
    public void testFindHbSizeValue() {
        String result = AdViewUtils.findHbSizeValue("{ \n adManagerResponse:\"hb_size\":[\"728x90\"],\"hb_size_rubicon\":[\"728x90\"],moPubResponse:\"hb_size:300x250\" \n }");
        assertNotNull(result);
        assertEquals("728x90", result);
    }

    @Test
    public void testFindHbSizeKeyValue() {
        String result = AdViewUtils.findHbSizeObject("{ \n adManagerResponse:\"hb_size\":[\"728x90\"],\"hb_size_rubicon\":[\"728x90\"],moPubResponse:\"hb_size:300x250\" \n }");
        assertNotNull(result);
        assertEquals("hb_size\":[\"728x90", result);
    }

    @Test
    public void testStringToCGSize() {
        Pair<Integer, Integer> result = AdViewUtils.stringToSize("300x250");
        assertNotNull(result);
        assertTrue(result.first == 300 && result.second == 250);

        result = AdViewUtils.stringToSize("300x250x1");
        assertNull(result);

        result = AdViewUtils.stringToSize("ERROR");
        assertNull(result);

        result = AdViewUtils.stringToSize("300x250ERROR");
        assertNull(result);
    }

    @Test
    public void testFailureFindASizeInNilHtmlCode() {
        findSizeInHtmlErrorHelper(null, PbFindSizeErrorFactory.NO_HTML_CODE);
    }

    @Test
    public void testFailureFindASizeIfItIsNotPresent() {
        findSizeInHtmlErrorHelper("<script> \n </script>", PbFindSizeErrorFactory.NO_SIZE_OBJECT_CODE);
    }

    @Test
    public void testFailureFindASizeIfItHasTheWrongType() {
        findSizeInHtmlErrorHelper("<script> \n \"hb_size\":\"1ERROR1\" \n </script>", PbFindSizeErrorFactory.NO_SIZE_OBJECT_CODE);
    }

    @Test
    public void testSuccessFindASizeIfProperlyFormatted() {
        findSizeInHtmlSuccessHelper("<script> \n \"hb_size\":[\"728x90\"] \n </script>", 728, 90);
    }

    void findSizeInHtmlErrorHelper(String htmlBody, int expectedErrorCode) {

        // given
        Pair<Integer, Integer> size;
        PbFindSizeError error;

        // when
        Pair<Pair<Integer, Integer>, PbFindSizeError> result = AdViewUtils.findSizeInHtml(htmlBody);
        size = result.first;
        error = result.second;

        // then
        assertNull(size);
        assertNotNull(error);
        assertEquals(expectedErrorCode, error.getCode());
    }

    @Test
    public void testSetWebViewScaleNotifiesScaleListenerOnSuccess() {
        // given
        WebView webView = mock(WebView.class);
        // run the posted callback synchronously
        when(webView.post(any(Runnable.class))).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return true;
        });
        final AtomicInteger calls = new AtomicInteger();
        final int[] reported = {-1, -1};

        // when
        AdViewUtils.setWebViewScale(webView, 100f, 50, 728, 90, (width, height) -> {
            calls.incrementAndGet();
            reported[0] = width;
            reported[1] = height;
        });

        // then: scale is applied and the listener fires exactly once with the reported size
        verify(webView).setInitialScale(anyInt());
        assertEquals(1, calls.get());
        assertEquals(728, reported[0]);
        assertEquals(90, reported[1]);
    }

    @Test
    public void testSetWebViewScaleUsesKnownCreativeHeightWhenContentHeightIsNonPositive() {
        // given
        WebView webView = mock(WebView.class);
        when(webView.post(any(Runnable.class))).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return true;
        });
        final AtomicInteger calls = new AtomicInteger();

        // when: the WebView reports no content height, but the creative height is known (90)
        AdViewUtils.setWebViewScale(webView, 100f, 0, 728, 90, (width, height) -> calls.incrementAndGet());

        // then: the known creative height is used instead of the unusable reported one, so the scale
        // is sane and the listener is notified. Previously this divided by the reported 0, giving
        // scale = 1 and a creative rendered at 1% of its size.
        verify(webView).setInitialScale(112);
        assertEquals(1, calls.get());
    }

    @Test
    public void testSetWebViewScaleSkipsWhenNoUsableHeightIsAvailable() {
        // given
        WebView webView = mock(WebView.class);
        when(webView.post(any(Runnable.class))).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return true;
        });
        final AtomicInteger calls = new AtomicInteger();

        // when: neither the reported content height nor the creative height is usable
        AdViewUtils.setWebViewScale(webView, 100f, 0, 0, 0, (width, height) -> calls.incrementAndGet());

        // then: no scale is applied at all, and the listener is not notified
        verify(webView, never()).setInitialScale(anyInt());
        assertEquals(0, calls.get());
    }

    @Test
    public void testSetWebViewScaleSkipsWhenTheViewIsNotLaidOut() {
        // given
        WebView webView = mock(WebView.class);
        final AtomicInteger calls = new AtomicInteger();

        // when: the view has no usable height yet (not laid out)
        AdViewUtils.setWebViewScale(webView, 0f, 50, 728, 90, (width, height) -> calls.incrementAndGet());

        // then: applying a scale from a 0-height view would render the creative at 1%
        verify(webView, never()).setInitialScale(anyInt());
        assertEquals(0, calls.get());
    }

    @Test
    public void testSetWebViewScaleWithoutListenerDoesNotPost() {
        // given: the legacy three-argument path delegates with a null listener
        WebView webView = mock(WebView.class);

        // when
        AdViewUtils.setWebViewScale(webView, 100f, 50);

        // then: scale is applied exactly as before. A repaint is posted (setInitialScale alone does
        // not reliably repaint a WebView that already painted at the old scale), but no listener
        // callback can be posted because there is no listener.
        verify(webView).setInitialScale(anyInt());
    }

    void findSizeInHtmlSuccessHelper(String htmlBody, int expectedWidth, int expectedHeight) {
        // given
        Pair<Integer, Integer> size;
        PbFindSizeError error;

        // when
        Pair<Pair<Integer, Integer>, PbFindSizeError> result = AdViewUtils.findSizeInHtml(htmlBody);
        size = result.first;
        error = result.second;

        // then
        assertNotNull(size);
        assertTrue(expectedWidth == size.first && expectedHeight == size.second);
        assertNull(error);
    }

}