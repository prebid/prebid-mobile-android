package org.prebid.mobile.rendering.utils.helpers;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.test.filters.Suppress;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UtilsTest extends TestCase {

    @Test
    public void testIsRecognizedUrl() {
        String url = "tel:911";
        String wrongUrl = "wrong:911";

        Assert.assertTrue(Utils.isMraidActionUrl(url));

        Assert.assertFalse(Utils.isMraidActionUrl(wrongUrl));

        assertFalse(Utils.isMraidActionUrl(""));
        assertFalse(Utils.isMraidActionUrl(null));
    }

    @Suppress
    public void testIsVideoContent() {
        String url = "right.mp4";
        String wrongUrl = "wrong:mp3";

        Assert.assertTrue(Utils.isVideoContent(url));

        Assert.assertFalse(Utils.isVideoContent(wrongUrl));

        assertFalse(Utils.isMraidActionUrl(""));
        assertFalse(Utils.isMraidActionUrl(null));
    }

    @Test
    public void testSubJsonArray() {
        JSONArray inputArray = new JSONArray();
        for (int i = 0; i < 10; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(String.valueOf(i), i);
                inputArray.put(jsonObject);
            }
            catch (JSONException e) {
                e.printStackTrace();
                fail("Failed to initialize test");
            }
        }

        JSONArray resultArray;
        JSONObject resultObject;
        JSONObject expectedObject;
        int start;
        int length;

        // Zero-length subArray
        start = 0;
        length = 0;
        resultArray = Utils.subJsonArray(inputArray, start, length);
        assertEquals(0, resultArray.length());

        // One-length subArray
        try {
            start = 0;
            length = 1;
            resultArray = Utils.subJsonArray(inputArray, start, length);
            expectedObject = new JSONObject();
            expectedObject.put(String.valueOf(start), start);
            resultObject = (JSONObject) resultArray.opt(0);
            assertEquals(expectedObject.toString(), resultObject.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
            fail("One length subArray test failed");
        }

        // Middle subArray
        try {
            start = 5;
            length = 2;
            resultArray = Utils.subJsonArray(inputArray, start, length);
            for (int i = 0; i < resultArray.length(); i++) {
                expectedObject = new JSONObject();
                expectedObject.put(String.valueOf(i + start), i + start);
                resultObject = (JSONObject) resultArray.opt(i);
                assertEquals(expectedObject.toString(), resultObject.toString());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            fail("Middle subArray test failed");
        }
    }

    @Test
    public void testParseUrl() {
        String url = "";
        BaseNetworkTask.GetUrlParams params = Utils.parseUrl(url);
        assertEquals(null, params);

        url = "http://foo.com/path/1/doc.html";
        params = Utils.parseUrl(url);
        assertEquals(null, params.queryParams);

        url = "http://foo.com/path/1/doc.html?a=1";
        params = Utils.parseUrl(url);
        assertEquals("a=1", params.queryParams);

        url = "http://foo.com/path/1/doc.html?a=1&b=2";
        params = Utils.parseUrl(url);
        assertEquals("http://foo.com/path/1/doc.html", params.url);
        assertEquals("a=1&b=2", params.queryParams);
    }

    @Test
    public void testAtLeastKitKat() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 19);
        assertEquals(true, Utils.atLeastKitKat());
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 18);
        assertEquals(false, Utils.atLeastKitKat());
    }

    @Test
    public void testAtLeastICS() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 14);
        assertEquals(true, Utils.atLeastICS());
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    @Test
    public void testGetQueryMap() {
        String query = "p1=a&p2=b&p3=c&p4=d";
        Map<String, String> expected = new HashMap<>();
        expected.put("p1", "a");
        expected.put("p2", "b");
        expected.put("p3", "c");
        expected.put("p4", "d");
        assertEquals(expected, Utils.getQueryMap(query));

        Map<String, String> expectedNotEquals = new HashMap<>();
        expectedNotEquals.putAll(expected);
        expectedNotEquals.put("p5", "e");
        assertNotSame(expectedNotEquals, Utils.getQueryMap(query));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetQueryMapWithError() {
        String queryWithSyntaxError = "p1:a&p2:b&p3:c&p4:d";
        Map<String, String> expectedWithSyntaxErrorInQuery = new HashMap<>();
        assertEquals(expectedWithSyntaxErrorInQuery, Utils.getQueryMap(queryWithSyntaxError));
    }

    @Test
    public void testGetScreenWidthWithSdkIntLess17() throws Exception {
        WindowManager mockWindowManager = mock(WindowManager.class);
        Display mockDisplay = mock(Display.class);
        Context mockContext = spy(RuntimeEnvironment.application);

        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager);
        when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                DisplayMetrics metrics = (DisplayMetrics) invocation.getArguments()[0];
                metrics.scaledDensity = 4.2f;
                metrics.widthPixels = 1100;
                metrics.heightPixels = 2001;
                return null;
            }
        }).when(mockDisplay).getMetrics(any(DisplayMetrics.class));

        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 16);

        assertThat(Utils.getScreenWidth(mockWindowManager), equalTo(1100));
    }

    @Test
    public void testGetScreenWidthWithSdkIntEquals17() throws Exception {
        WindowManager mockWindowManager = mock(WindowManager.class);
        Display mockDisplay = mock(Display.class);
        Context mockContext = spy(RuntimeEnvironment.application);

        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager);
        when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Point size = (Point) invocation.getArguments()[0];
                size.x = 1100;
                size.y = 2001;
                return null;
            }
        }).when(mockDisplay).getRealSize(any(Point.class));

        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 17);

        assertThat(Utils.getScreenWidth(mockWindowManager), equalTo(1100));
    }

    @Test
    public void testGetScreenHeightWithSdkIntLess17() throws Exception {
        WindowManager mockWindowManager = mock(WindowManager.class);
        Display mockDisplay = mock(Display.class);
        Context mockContext = spy(RuntimeEnvironment.application);

        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager);
        when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                DisplayMetrics metrics = (DisplayMetrics) invocation.getArguments()[0];
                metrics.scaledDensity = 4.2f;
                metrics.widthPixels = 1100;
                metrics.heightPixels = 2001;
                return null;
            }
        }).when(mockDisplay).getMetrics(any(DisplayMetrics.class));

        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 16);

        assertThat(Utils.getScreenHeight(mockWindowManager), equalTo(2001));
    }

    @Test
    public void testGetScreenHeightWithSdkIntEquals17() throws Exception {
        WindowManager mockWindowManager = mock(WindowManager.class);
        Display mockDisplay = mock(Display.class);
        Context mockContext = spy(RuntimeEnvironment.application);

        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager);
        when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Point size = (Point) invocation.getArguments()[0];
                size.x = 1100;
                size.y = 2001;
                return null;
            }
        }).when(mockDisplay).getRealSize(any(Point.class));

        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 17);

        assertThat(Utils.getScreenHeight(mockWindowManager), equalTo(2001));
    }

    @Test
    public void generateSHA1ByteTest() {
        byte[] array = {1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3};
        assertNotEquals(array, Utils.generateSHA1(array));
    }

    @Test
    public void generateSHA1StringTest() {
        String test = "long_test_string";
        assertNotEquals(test, Utils.generateSHA1(test));
    }

    @Test
    public void byteArrayToHexStringTest() {
        String test = "TEST";
        assertEquals("54455354", Utils.byteArrayToHexString(test.getBytes()));
    }

    @Test
    public void md5Test() {
        assertEquals("", Utils.md5(""));
        assertNotEquals("", Utils.md5("test"));
        assertNotEquals("test", Utils.md5("test"));
    }

    @Test
    public void isVastWithVastData_ReturnTrue() throws IOException {
        String vast = ResourceUtils.convertResourceToString("ad_inline_sample.xml");
        assertTrue(Utils.isVast(vast));
    }

    @Test
    public void isVastWithHtmlData_ReturnFalse() throws IOException {
        String html = ResourceUtils.convertResourceToString("ad_contains_iframe");
        assertFalse(Utils.isVast(html));
    }

    @Test
    public void isVastWithEmptyData_ReturnFalse() {
        assertFalse(Utils.isVast(""));
    }

    @Test
    public void getFileExtensionWithNullOrEmptyString_ReturnEmptyString() {
        assertTrue(Utils.getFileExtension(null).isEmpty());
        assertTrue(Utils.getFileExtension("").isEmpty());
    }

    @Test
    public void getFileExtensionWithEmptyLastPathSegment_ReturnEmptyString() {
        assertTrue(Utils.getFileExtension("https://prebid.com/").isEmpty());
    }

    @Test
    public void getFileExtensionWithoutExtension_ReturnEmptyString() {
        assertTrue(Utils.getFileExtension("http://prebid.com/prebidlogo").isEmpty());
    }

    @Test
    public void getFileExtensionWithExtension_ReturnExtensionString() {
        final String sampleUrl = "http://sample.com/logo.gif?someparam=param";

        assertEquals(".gif", Utils.getFileExtension(sampleUrl));
    }

    @Test
    public void clampAutoRefresh_UserValueGraterThanMax_ReturnMaxValue() {
        int refreshDelaySec = 130;
        int clampAutoRefresh = Utils.clampAutoRefresh(refreshDelaySec);

        assertEquals(PrebidRenderingSettings.AUTO_REFRESH_DELAY_MAX, clampAutoRefresh);
    }

    @Test
    public void clampAutoRefresh_UserValueSmallerThanMin_ReturnMinValue() {
        int refreshDelaySec = 14;
        int clampAutoRefresh = Utils.clampAutoRefresh(refreshDelaySec);

        assertEquals(PrebidRenderingSettings.AUTO_REFRESH_DELAY_MIN, clampAutoRefresh);
    }

    @Test
    public void clampAutoRefresh_UserValueInRange_ReturnUserValue() {
        int refreshDelaySec = 50;
        int clampAutoRefresh = Utils.clampAutoRefresh(refreshDelaySec);

        assertEquals(refreshDelaySec * 1000, clampAutoRefresh);
    }
}
