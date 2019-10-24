package org.prebid.mobile;

public class NativeDataAsset extends NativeAsset {
    public NativeDataAsset() {
        super(REQUEST_ASSET.DATA);
    }

    public enum DATA_TYPE {
        SPONSORED(1),
        DESC(2),
        RATING(3),
        LIKES(4),
        DOWNLOADS(5),
        PRICE(6),
        SALEPRICE(7),
        PHONE(8),
        ADDRESS(9),
        DESC2(10),
        DESPLAYURL(11),
        CTATEXT(12),
        CUSTOM(500);
        private int id;

        DATA_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }
    }


    //    public void addData(DATA_TYPE type, Integer len, Boolean required, Object assetExt, Object dataExt) {
//        HashMap<NativeAdUnit.REQUEST_ASSET, ArrayList<HashMap<String, Object>>> assets = (HashMap<NativeAdUnit.REQUEST_ASSET, ArrayList<HashMap<String, Object>>>) requestConfig.get("assets");
//        if (assets == null) {
//            assets = new HashMap<>();
//        }
//        HashMap<String, Object> params = new HashMap<>();
//        params.put(LENGTH, len);
//        params.put(REQUIRED, required);
//        params.put(TYPE, type.getID());
//
//        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
//            params.put(ASSETS_EXT, assetExt);
//        }
//        if (dataExt instanceof JSONArray || assetExt instanceof JSONObject) {
//            params.put(EXT, dataExt);
//        }
//        ArrayList<HashMap<String, Object>> assetParams = assets.get(NativeAdUnit.REQUEST_ASSET.DATA);
//        if (assetParams == null) {
//            assetParams = new ArrayList<>();
//        }
//        assetParams.add(params);
//        assets.put(NativeAdUnit.REQUEST_ASSET.DATA, assetParams);
//        requestConfig.put(ASSETS, assets);
//    }
}
