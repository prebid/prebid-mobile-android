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

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
        assertEquals(MoPubView.class, Util.getClassFromString(Util.MOPUB_BANNER_VIEW_CLASS));
        assertEquals(MoPubInterstitial.class, Util.getClassFromString(Util.MOPUB_INTERSTITIAL_CLASS));
        assertEquals(PublisherAdRequest.class, Util.getClassFromString(Util.DFP_AD_REQUEST_CLASS));
    }

    @Test
    public void testApplyBidsToMoPubAdobject() throws Exception {
        MoPubView adView = new MoPubView(activity);
        adView.setKeywords("key1:value1,key2:value2");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        Util.apply(bids, adView);
        String adViewKeywords = adView.getKeywords();
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", adViewKeywords);
        Util.apply(null, adView);
        assertEquals("key1:value1,key2:value2", adView.getKeywords());
        MoPubInterstitial instl = new MoPubInterstitial(activity, "123456");
        instl.setKeywords("key1:value1,key2:value2");
        Util.apply(bids, instl);
        assertEquals("hb_pb:0.50,hb_cache_id:123456,key1:value1,key2:value2", instl.getKeywords());
        Util.apply(null, instl);
        assertEquals("key1:value1,key2:value2", instl.getKeywords());
    }

    @Test
    public void testApplyBidsToDFOAdObject() throws Exception {
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addCustomTargeting("Key", "Value");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        PublisherAdRequest request = builder.build();
        Util.apply(bids, request);
        assertEquals(3, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        assertEquals("Value", request.getCustomTargeting().get("Key"));
        assertTrue(request.getCustomTargeting().containsKey("hb_pb"));
        assertEquals("0.50", request.getCustomTargeting().get("hb_pb"));
        assertTrue(request.getCustomTargeting().containsKey("hb_cache_id"));
        assertEquals("123456", request.getCustomTargeting().get("hb_cache_id"));
        Util.apply(null, request);
        assertEquals(1, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        assertEquals("Value", request.getCustomTargeting().get("Key"));
    }

    @Test
    public void testSupportedAdObject() throws Exception {
        MoPubView testView = new MoPubView(activity);
        assertTrue(Util.supportedAdObject(testView));
        assertFalse(Util.supportedAdObject(null));
        MoPubInterstitial interstitial = new MoPubInterstitial(activity, "");
        assertTrue(Util.supportedAdObject(interstitial));
        PublisherAdRequest request = new PublisherAdRequest.Builder().build();
        assertTrue(Util.supportedAdObject(request));
        Object object = new Object();
        assertFalse(Util.supportedAdObject(object));
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

}
