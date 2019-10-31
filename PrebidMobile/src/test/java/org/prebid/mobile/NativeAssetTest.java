package org.prebid.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest=Config.NONE)
public class NativeAssetTest {

    @Test
    public void testNativeAssetTitle() {
        NativeTitleAsset title = new NativeTitleAsset();
        assertEquals(title.getLen(), 0);
        assertEquals(title.isRequired(), false);
        assertNull(title.getTitleExt());
        assertNull(title.getAssetExt());

        title.setLength(25);
        title.setRequired(true);
        JSONObject assetExt = new JSONObject();
        try {
            assetExt.put("key1", "value1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        title.setAssetExt(assetExt);

        JSONObject titleExt = new JSONObject();
        try {
            titleExt.put("key2", "value2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        title.setTitleExt(titleExt);


        assertEquals(title.getLen(), 25);
        assertEquals(title.isRequired(), true);
        String value1 = "";
        try {
            JSONObject data = (JSONObject) title.getAssetExt();
            value1 = data.getString("key1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value1", value1);

        String value2 = "";
        try {
            JSONObject data = (JSONObject) title.getTitleExt();
            value2 = data.getString("key2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value2", value2);
    }

    @Test
    public void testNativeAssetImage() {
        NativeImageAsset image = new NativeImageAsset();
        image.setWMin(20);
        image.setHMin(30);
        image.setW(100);
        image.setH(200);
        image.setRequired(true);
        image.addMime("png");
        JSONObject assetExt = new JSONObject();
        try {
            assetExt.put("key1", "value1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        image.setAssetExt(assetExt);

        JSONObject imageExt = new JSONObject();
        try {
            imageExt.put("key2", "value2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        image.setImageExt(imageExt);

        assertEquals(image.getWMin(), 20);
        assertEquals(image.getHMin(), 30);
        assertEquals(image.getW(), 100);
        assertEquals(image.getH(), 200);
        assertEquals(image.isRequired(), true);
        assertEquals(image.getMimes().get(0), "png");

        String value1 = "";
        try {
            JSONObject data = (JSONObject) image.getAssetExt();
            value1 = data.getString("key1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value1", value1);

        String value2 = "";
        try {
            JSONObject data = (JSONObject) image.getImageExt();
            value2 = data.getString("key2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value2", value2);

        image.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        assertEquals(image.getImageType(), NativeImageAsset.IMAGE_TYPE.ICON);
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        assertEquals(image.getImageType(), NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setImageType(NativeImageAsset.IMAGE_TYPE.CUSTOM);
        assertEquals(image.getImageType(), NativeImageAsset.IMAGE_TYPE.CUSTOM);
    }

    @Test
    public void testNativeAssetData() {
        NativeDataAsset dataAsset = new NativeDataAsset();
        dataAsset.setLen(25);
        dataAsset.setRequired(true);
        JSONObject assetExt = new JSONObject();
        try {
            assetExt.put("key1", "value1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataAsset.setAssetExt(assetExt);
        JSONObject dataExt = new JSONObject();
        try {
            dataExt.put("key2", "value2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataAsset.setDataExt(dataExt);

        assertEquals(dataAsset.getLen(),25);
        assertEquals(dataAsset.isRequired(),true);

        String value1 = "";
        try {
            JSONObject data = (JSONObject) dataAsset.getAssetExt();
            value1 = data.getString("key1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value1", value1);

        String value2 = "";
        try {
            JSONObject data = (JSONObject) dataAsset.getDataExt();
            value2 = data.getString("key2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("value2", value2);

        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.SPONSORED);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.DESC);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.RATING);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.RATING);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.LIKES);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.LIKES);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DOWNLOADS);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.DOWNLOADS);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.PRICE);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.PRICE);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.SALEPRICE);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.SALEPRICE);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.PHONE);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.PHONE);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.ADDRESS);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.ADDRESS);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DESC2);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.DESC2);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DESPLAYURL);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.DESPLAYURL);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.CTATEXT);
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.CUSTOM);
        assertEquals(dataAsset.getDataType(), NativeDataAsset.DATA_TYPE.CUSTOM);

    }


}