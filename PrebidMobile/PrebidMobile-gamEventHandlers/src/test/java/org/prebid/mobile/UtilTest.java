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

package org.prebid.mobile;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;
import okhttp3.mockwebserver.MockWebServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.addendum.AdViewUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.httpclient.FakeHttp;
import org.robolectric.util.Scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class UtilTest extends BaseSetup {

    class TestObject {
        public String testString() {
            return "test";
        }
    }

    @Test
    public void testCallMethodOnObject() throws Exception {
        TestObject object = new TestObject();
        Object result = Util.callMethodOnObject(object, "testString");
        assertEquals("test", result);
    }

    @Test
    public void testGetClassFromString() throws Exception {

        assertEquals(AdManagerAdRequest.class, Util.getClassFromString(Util.AD_MANAGER_REQUEST_CLASS_V20));
        assertEquals(AdManagerAdRequest.Builder.class, Util.getClassFromString(Util.AD_MANAGER_REQUEST_BUILDER_CLASS_V20));
    }



    @Test
    public void testApplyBidsToDFOAdObject() throws Exception {
        AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        builder.addCustomTargeting("Key", "Value");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");

        Util.apply(bids, builder);
        AdManagerAdRequest request = builder.build();
        assertEquals(3, request.getCustomTargeting().size());
        assertEquals("Value", request.getCustomTargeting().get("Key"));
        assertEquals("0.50", request.getCustomTargeting().get("hb_pb"));
        assertEquals("123456", request.getCustomTargeting().get("hb_cache_id"));

        Util.apply(bids, request);
        assertEquals(3, request.getCustomTargeting().size());
        assertEquals("Value", request.getCustomTargeting().get("Key"));
        assertEquals("0.50", request.getCustomTargeting().get("hb_pb"));
        assertEquals("123456", request.getCustomTargeting().get("hb_cache_id"));

        Util.apply(null, request);
        assertEquals(1, request.getCustomTargeting().size());
        assertEquals("Value", request.getCustomTargeting().get("Key"));
    }



    @Test
    public void testGetObjectWithoutEmptyValues() throws JSONException {

        //Test 1
        JSONObject node1111 = new JSONObject();

        JSONObject node111 = new JSONObject();
        node111.put("key111", node1111);

        JSONObject node11 = new JSONObject();
        node11.put("key11", node111);

        JSONObject node1 = new JSONObject();
        node1.put("key1", node11);

        node1.put("emptyObject", "");
        List<String> array = new ArrayList<>();
        array.add("");
        node1.put("emptyArray", new JSONArray(array));

        JSONObject result1 = Util.getObjectWithoutEmptyValues(node1);

        Assert.assertNull(result1);

        //Test 2
        node1111.put("key1111", "value1111");
        JSONObject result2 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertEquals("{\"key1\":{\"key11\":{\"key111\":{\"key1111\":\"value1111\"}}}}", result2.toString());

        //Test 3
        node1111.remove("key1111");
        JSONObject node121 = new JSONObject();
        node121.put("key121", "value121");
        node11.put("key12", node121);

        JSONObject result3 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertEquals("{\"key1\":{\"key12\":{\"key121\":\"value121\"}}}", result3.toString());

        //Test 4
        node11.remove("key12");
        JSONArray node21 = new JSONArray();
        node1.put("key2", node21);
        JSONObject result4 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertNull(result4);

        //Test5
        node21.put("value21");
        JSONObject result5 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertEquals("{\"key2\":[\"value21\"]}", result5.toString());

        //Test6
        node21.remove(0);
        JSONObject node211 = new JSONObject();
        node21.put(node211);
        JSONObject result6 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertNull(result6);

        //Test7
        node211.put("key211", "value211");
        JSONObject result7 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertEquals("{\"key2\":[{\"key211\":\"value211\"}]}", result7.toString());

        //Test8
        node21.remove(0);
        JSONArray node212 = new JSONArray();
        node21.put(node212);
        JSONObject result8 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertNull(result8);

        //Test9
        JSONArray node31 = new JSONArray();
        node1.put("key3", node31);
        JSONObject node311 = new JSONObject();
        node31.put(node311);
        JSONObject node312 = new JSONObject();
        node312.put("key312", "value312");
        node31.put(node312);
        JSONObject result9 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertEquals("{\"key3\":[{\"key312\":\"value312\"}]}", result9.toString());

        //Test10
        JSONArray node313 = new JSONArray();
        JSONObject node3131 = new JSONObject();
        node3131.put("key3131", "value3131");
        node313.put(node3131);
        JSONObject node3132 = new JSONObject();
        node313.put(node3132);
        node31.put(node313);
        JSONObject result10 = Util.getObjectWithoutEmptyValues(node1);
        Assert.assertEquals("{\"key3\":[{\"key312\":\"value312\"},[{\"key3131\":\"value3131\"}]]}", result10.toString());
    }

    @Test
    public void testAddValue() {

        //given
        Map<String, Set<String>> map = new HashMap<>();

        //when
        //add key1/value10
        Util.addValue(map, "key1", "value10");

        //then
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, map.get("key1").size());
        Assert.assertTrue(map.get("key1").contains("value10"));

        //when
        //add key2/value20
        Util.addValue(map, "key2", "value20");

        //then
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(1, map.get("key2").size());
        Assert.assertTrue(map.get("key2").contains("value20"));

        //when
        //add key1/value11
        Util.addValue(map, "key1", "value11");

        //then
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(2, map.get("key1").size());
        Assert.assertTrue(map.get("key1").contains("value10") && map.get("key1").contains("value11"));
        Assert.assertTrue(map.get("key2").contains("value20"));
    }

    @Test
    public void testToJson() throws JSONException {

        //given
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();

        //when
        JSONObject jsonObject = Util.toJson(map);

        //then
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals("{}", jsonObject.toString());

        //given
        map.put("key1", set1);

        //when
        JSONObject jsonObject2 = Util.toJson(map);

        //then
        Assert.assertEquals("{\"key1\":[]}", jsonObject2.toString());

        //given
        set1.add("value11");

        //when
        JSONObject jsonObject3 = Util.toJson(map);

        //then
        Assert.assertEquals("{\"key1\":[\"value11\"]}", jsonObject3.toString());

        //given
        set1.add("value12");

        //when
        JSONObject jsonObject4 = Util.toJson(map);

        //then
        Assert.assertEquals("{\"key1\":[\"value11\",\"value12\"]}", jsonObject4.toString());

        //given
        set2.add("value21");
        map.put("key2", set2);

        //when
        JSONObject jsonObject5 = Util.toJson(map);

        //then
        Assert.assertEquals("{\"key1\":[\"value11\",\"value12\"],\"key2\":[\"value21\"]}", jsonObject5.toString());
    }



    @Test
    public void testConvertJSONArray() throws Exception {
        //given
        List<String> list1 = Arrays.asList("first", "second");
        JSONArray jsonArray1 = new JSONArray(list1);

        List<Integer> list2 = Arrays.asList(1, 2);
        JSONArray jsonArray2 = new JSONArray(list2);

        //when
        List<String> result1 = Util.convertJSONArray(jsonArray1);
        List<Integer> result2 = Util.convertJSONArray(jsonArray2);

        //then
        Assert.assertEquals(list1, result1);
        Assert.assertEquals(list2, result2);
    }

    @Test
    public void testConvertCollection() {
        //given
        List<Signals.Api> list1 = Arrays.asList(new Signals.Api(1), new Signals.Api(2));

        //when
        List<Integer> result1 = Util.convertCollection(list1, new Util.Function1<Integer, Signals.Api>() {
            @Override
            public Integer apply(Signals.Api element) {
                return element.value;
            }
        });

        //then
        Assert.assertEquals(Arrays.asList(1, 2), result1);
    }

    @Test
    public void testGenerateInstreamUriForGam() {
        HashSet<AdSize> sizes = null;
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            assertEquals("sizes should not be empty", e.getMessage());
        }
        sizes = new HashSet<>();
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            assertEquals("sizes should not be empty", e.getMessage());
        }
        sizes.add(new AdSize(1, 300));
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            assertEquals("size should be either 640x480 or 400x300", e.getMessage());
        }
        sizes = new HashSet<>();
        sizes.add(new AdSize(400, 300));
        Exception exception = null;
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            exception = e;
        }
        assertEquals(null, exception);
        sizes = new HashSet<>();
        sizes.add(new AdSize(640, 480));
        exception = null;
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            exception = e;
        }
        assertEquals(null, exception);
        sizes = new HashSet<>();
        sizes.add(new AdSize(640, 480));
        sizes.add(new AdSize(400, 300));
        exception = null;
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            exception = e;
        }
        assertEquals(null, exception);
        sizes = new HashSet<>();
        sizes.add(new AdSize(640, 480));
        sizes.add(new AdSize(300, 300));
        exception = null;
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            exception = e;
        }
        assertEquals("size should be either 640x480 or 400x300", exception.getMessage());
        sizes = new HashSet<>();
        sizes.add(new AdSize(640, 480));
        sizes.add(new AdSize(400, 300));
        sizes.add(new AdSize(1, 1));
        try {
            Util.generateInstreamUriForGam("test", sizes, null);
        } catch (Exception e) {
            assertEquals("size should be either 640x480 or 400x300", e.getMessage());
        }
        sizes = new HashSet<>();
        sizes.add(new AdSize(640, 480));
        String adTagUrl = Util.generateInstreamUriForGam("test", sizes, null);
        assertEquals("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=test&impl=s&gdfp_req=1&env=vp&output=xml_vast4&unviewed_position_start=1", adTagUrl);
        sizes = new HashSet<>();
        sizes.add(new AdSize(640, 480));
        sizes.add(new AdSize(400, 300));
        HashMap<String, String> keywords = new HashMap<>();
        keywords.put("hb_cache_id", "123");
        keywords.put("hb_pb", "1");
        adTagUrl = Util.generateInstreamUriForGam("test", sizes, keywords);
        assertEquals("https://pubads.g.doubleclick.net/gampad/ads?sz=400x300|640x480&iu=test&impl=s&gdfp_req=1&env=vp&output=xml_vast4&unviewed_position_start=1&cust_params=hb_pb%3D1%26hb_cache_id%3D123%26", adTagUrl);
    }

    @Test
    public void testFindNativeLoadedDFP() {
        String mockedResponse = MockPrebidServerResponses.validResponsePrebidNativeNativeBid();
        String cacheId = CacheManager.save(mockedResponse);
        NativeCustomTemplateAd nativeCustomTemplateAd = Mockito.mock(NativeCustomTemplateAd.class);
        Mockito.when(nativeCustomTemplateAd.getText("isPrebid")).thenReturn("1");
        Mockito.when(nativeCustomTemplateAd.getText("hb_cache_id_local")).thenReturn(cacheId);
        AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
            @Override
            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                assertTrue(ad != null);
            }

            @Override
            public void onPrebidNativeNotFound() {
                fail();
            }

            @Override
            public void onPrebidNativeNotValid() {
                fail();
            }
        });
    }

    @Test
    public void testFindNativeNotValidDFP() {
        String mockedResponse = MockPrebidServerResponses.validResponsePrebidNativeNativeBid();
        String cacheId = CacheManager.save(mockedResponse);
        NativeCustomTemplateAd nativeCustomTemplateAd = Mockito.mock(NativeCustomTemplateAd.class);
        Mockito.when(nativeCustomTemplateAd.getText("isPrebid")).thenReturn("1");
        Mockito.when(nativeCustomTemplateAd.getText("hb_cache_id_local")).thenReturn("cacheId");
        AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
            @Override
            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                fail();
            }

            @Override
            public void onPrebidNativeNotFound() {
                fail();
            }

            @Override
            public void onPrebidNativeNotValid() {
                assertTrue(true);
            }
        });
    }

    @Test
    public void testFindNativeNotFoundDFP() {
        String mockedResponse = MockPrebidServerResponses.validResponsePrebidNativeNativeBid();
        String cacheId = CacheManager.save(mockedResponse);
        NativeCustomTemplateAd nativeCustomTemplateAd = Mockito.mock(NativeCustomTemplateAd.class);
        Mockito.when(nativeCustomTemplateAd.getText("isPrebid")).thenReturn("0");
        Mockito.when(nativeCustomTemplateAd.getText("hb_cache_id_local")).thenReturn(cacheId);
        AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
            @Override
            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                fail();
            }

            @Override
            public void onPrebidNativeNotFound() {
                assertTrue(true);
            }

            @Override
            public void onPrebidNativeNotValid() {
                fail();
            }
        });
    }
}


class BaseSetup {
    public static final int testSDK = 21;

    protected MockWebServer server;
    protected Scheduler uiScheduler, bgScheduler;
    protected Activity activity;

    @Before
    public void setup() {
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        activity = Robolectric.buildActivity(MockMainActivity.class).create().get();
        shadowOf(activity).grantPermissions("android.permission.INTERNET");
        shadowOf(activity).grantPermissions("android.permission.CHANGE_NETWORK_STATE");
        shadowOf(activity).grantPermissions("android.permission.MODIFY_PHONE_STATE");
        shadowOf(activity).grantPermissions("android.permission.ACCESS_NETWORK_STATE");
        server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(true);
        FakeHttp.getFakeHttpLayer().interceptResponseContent(true);
        bgScheduler = Robolectric.getBackgroundThreadScheduler();
        uiScheduler = Robolectric.getForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        bgScheduler.pause();
        uiScheduler.pause();
    }

    @After
    public void tearDown() {
        activity.finish();
        try {
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MockMainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

class MockPrebidServerResponses {
    public static String validResponsePrebidNativeNativeBid() {
        InputStream in = MockPrebidServerResponses.class.getClassLoader().getResourceAsStream("PrebidServerValidResponsePrebidNativeNativeBid.json");
        return inputStreamToString(in);
    }

    public static String inputStreamToString(InputStream is) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            is.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
