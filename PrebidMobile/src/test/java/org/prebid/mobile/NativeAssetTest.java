package org.prebid.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class NativeAssetTest {

    @Test
    public void testNativeAssetTitle() {
        NativeTitleAsset title = new NativeTitleAsset();
        assertEquals(0, title.getLen());
        assertEquals(false, title.isRequired());
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


        assertEquals(25, title.getLen());
        assertEquals(true, title.isRequired());
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

        assertEquals(20, image.getWMin());
        assertEquals(30, image.getHMin());
        assertEquals(100, image.getW());
        assertEquals(200, image.getH());
        assertEquals(true, image.isRequired());
        assertEquals("png", image.getMimes().get(0));

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
        assertEquals(NativeImageAsset.IMAGE_TYPE.ICON, image.getImageType());
        assertEquals(1, image.getImageType().getID());
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        assertEquals(NativeImageAsset.IMAGE_TYPE.MAIN, image.getImageType());
        assertEquals(3, image.getImageType().getID());
        image.setImageType(NativeImageAsset.IMAGE_TYPE.CUSTOM);
        assertEquals(NativeImageAsset.IMAGE_TYPE.CUSTOM, image.getImageType());
        NativeImageAsset.IMAGE_TYPE.CUSTOM.setID(500);
        assertEquals(500, image.getImageType().getID());
        NativeImageAsset.IMAGE_TYPE.CUSTOM.setID(600);
        assertEquals(600, image.getImageType().getID());
        NativeImageAsset.IMAGE_TYPE.CUSTOM.setID(1);
        assertEquals(600, image.getImageType().getID());
        assertFalse("Invalid CustomId", 1 == image.getImageType().getID());
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

        assertEquals(25, dataAsset.getLen());
        assertEquals(true, dataAsset.isRequired());

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
        assertEquals(NativeDataAsset.DATA_TYPE.SPONSORED, dataAsset.getDataType());
        assertEquals(1, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        assertEquals(NativeDataAsset.DATA_TYPE.DESC, dataAsset.getDataType());
        assertEquals(2, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.RATING);
        assertEquals(NativeDataAsset.DATA_TYPE.RATING, dataAsset.getDataType());
        assertEquals(3, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.LIKES);
        assertEquals(NativeDataAsset.DATA_TYPE.LIKES, dataAsset.getDataType());
        assertEquals(4, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DOWNLOADS);
        assertEquals(NativeDataAsset.DATA_TYPE.DOWNLOADS, dataAsset.getDataType());
        assertEquals(5, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.PRICE);
        assertEquals(NativeDataAsset.DATA_TYPE.PRICE, dataAsset.getDataType());
        assertEquals(6, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.SALEPRICE);
        assertEquals(NativeDataAsset.DATA_TYPE.SALEPRICE, dataAsset.getDataType());
        assertEquals(7, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.PHONE);
        assertEquals(NativeDataAsset.DATA_TYPE.PHONE, dataAsset.getDataType());
        assertEquals(8, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.ADDRESS);
        assertEquals(NativeDataAsset.DATA_TYPE.ADDRESS, dataAsset.getDataType());
        assertEquals(9, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DESC2);
        assertEquals(NativeDataAsset.DATA_TYPE.DESC2, dataAsset.getDataType());
        assertEquals(10, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.DESPLAYURL);
        assertEquals(NativeDataAsset.DATA_TYPE.DESPLAYURL, dataAsset.getDataType());
        assertEquals(11, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        assertEquals(NativeDataAsset.DATA_TYPE.CTATEXT, dataAsset.getDataType());
        assertEquals(12, dataAsset.getDataType().getID());
        dataAsset.setDataType(NativeDataAsset.DATA_TYPE.CUSTOM);
        assertEquals(NativeDataAsset.DATA_TYPE.CUSTOM, dataAsset.getDataType());
        NativeDataAsset.DATA_TYPE.CUSTOM.setID(500);
        assertEquals(500, dataAsset.getDataType().getID());
        NativeDataAsset.DATA_TYPE.CUSTOM.setID(600);
        assertEquals(600, dataAsset.getDataType().getID());
        NativeDataAsset.DATA_TYPE.CUSTOM.setID(1);
        assertEquals(600, dataAsset.getDataType().getID());
        assertFalse("Invalid CustomId", 1 == dataAsset.getDataType().getID());

    }


}