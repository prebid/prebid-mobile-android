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
import static org.mockito.Mockito.doAnswer;
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

        // then: scale is applied and the listener fires exactly once with the reported size.
        // getWidth() defaults to 0 on the mock, so the width term is skipped and byHeight stands.
        verify(webView).setInitialScale(201);
        assertEquals(1, calls.get());
        assertEquals(728, reported[0]);
        assertEquals(90, reported[1]);
    }

    @Test
    public void testSetWebViewScaleSkipsWhenContentHeightIsUnreported() {
        // given
        WebView webView = mock(WebView.class);
        final AtomicInteger calls = new AtomicInteger();

        // when: the WebView never reported a content height
        AdViewUtils.setWebViewScale(webView, 140.6f, 0, 320, 50, (width, height) -> calls.incrementAndGet());

        // then: no scale is applied
        verify(webView, never()).setInitialScale(anyInt());
        assertEquals(0, calls.get());
    }

    @Test
    public void testSetWebViewScaleUsesHeightRatioWhenItIsTheSmaller() {
        // given: a 320x50 creative in a 900px-wide view reporting contentHeight 85
        WebView webView = mock(WebView.class);
        when(webView.getWidth()).thenReturn(900);

        // when
        AdViewUtils.setWebViewScale(webView, 140f, 85, 320, 50, null);

        // then: min(byWidth 281, byHeight 165) = 165
        verify(webView).setInitialScale(165);
    }

    @Test
    public void testSetWebViewScaleIgnoresAWidthFromAnUnmeasuredView() {
        // given: a view reporting 5px against a declared 100 -- the ratio is a non-zero 5%, so only
        // the minimum-measured-width gate can reject it. Without that gate the creative would be
        // scaled to 5% and rendered as a sliver.
        WebView webView = mock(WebView.class);
        when(webView.getWidth()).thenReturn(5);

        // when
        AdViewUtils.setWebViewScale(webView, 140f, 50, 100, 50, null);

        // then: the width term is discarded and the height ratio stands
        verify(webView).setInitialScale(281);
    }

    @Test
    public void testSetWebViewScaleIgnoresAWidthRatioThatTruncatesToZero() {
        // given: a view genuinely wider than the mid-layout threshold, but narrow relative to a
        // large declared width, so viewWidth/declaredWidth truncates to 0. The gate above passes
        // here, so only the explicit zero-ratio check can reject it.
        WebView webView = mock(WebView.class);
        when(webView.getWidth()).thenReturn(15);

        // when
        AdViewUtils.setWebViewScale(webView, 140f, 50, 2000, 50, null);

        // then: the width term is discarded rather than applied. setInitialScale(0) means
        // "use the default scale" to Android, which would silently drop the correction.
        verify(webView).setInitialScale(281);
    }

    @Test
    public void testFixZoomInAppliesNoScaleWhenTheViewHasNoUsableHeight() {
        // given: a WebView that is not laid out when fixZoomIn runs
        WebView webView = mock(WebView.class);
        when(webView.getHeight()).thenReturn(0);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return true;
        }).when(webView).post(any(Runnable.class));

        // when
        AdViewUtils.fixZoomIn(webView, 320, 50);

        // then: nothing is derived from the unusable geometry
        verify(webView, never()).setInitialScale(anyInt());
    }

    @Test
    public void testSetWebViewScaleUsesWidthRatioWhenItIsTheSmaller() {
        // given: a 320x50 creative whose view was measured twice as tall as the creative, so the
        // height ratio alone would render it wider than the view and crop it horizontally
        WebView webView = mock(WebView.class);
        when(webView.getWidth()).thenReturn(900);

        // when: byHeight computes 563
        AdViewUtils.setWebViewScale(webView, 281.25f, 50, 320, 50, null);

        // then: the width-derived scale wins, so the creative fits inside the view
        verify(webView).setInitialScale(281);
    }

    @Test
    public void testSetWebViewScaleFallsBackToHeightRatioWhenWidthIsUnknown() {
        // given: getWidth() is 0, so no width-derived scale can be computed
        WebView webView = mock(WebView.class);

        // when
        AdViewUtils.setWebViewScale(webView, 140.6f, 50, 320, 50, null);

        // then: the height ratio is applied unchanged, as before
        verify(webView).setInitialScale(282);
    }

    @Test
    public void testSetWebViewScaleFallsBackToTheHeightRatioWhenNoWidthWasDeclared() {
        // given: a measured view but no declared width (the legacy three-argument path)
        WebView webView = mock(WebView.class);
        when(webView.getWidth()).thenReturn(900);

        // when
        AdViewUtils.setWebViewScale(webView, 140f, 50, 0, 0, null);

        // then: the height ratio stands. This documents the outcome rather than pinning the
        // width > 0 guard -- that guard is redundant by construction (removing it still yields
        // byHeight via Infinity -> MAX_VALUE -> min()), so no assertion on this method can kill it.
        verify(webView).setInitialScale(281);
    }

    @Test
    public void testSetWebViewScaleTrimsTheHeightRatioByOneOnAWellFormedAd() {
        // given: a healthy 320x50 creative on a 3x-density screen, both axes in agreement
        WebView webView = mock(WebView.class);
        when(webView.getWidth()).thenReturn(960);

        // when
        AdViewUtils.setWebViewScale(webView, 150f, 50, 320, 50, null);

        // then: 300, not the 301 the height ratio alone gives. byHeight rounds up (+1) while
        // byWidth truncates, so min() picks byWidth by one on every well-formed ad. 301 renders
        // 320 CSS px at 963.2px inside a 960px view -- the reported overflow in miniature.
        verify(webView).setInitialScale(300);
    }

    @Test
    public void testSetWebViewScaleWithoutListenerDoesNotPost() {
        // given: the legacy three-argument path delegates with a null listener
        WebView webView = mock(WebView.class);

        // when
        AdViewUtils.setWebViewScale(webView, 100f, 50);

        // then: the legacy path applies exactly the value it always has -- this pins the
        // no-behaviour-change contract for existing callers -- and posts nothing.
        verify(webView).setInitialScale(201);
        verify(webView, never()).post(any(Runnable.class));
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