package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NativeImageAsset extends NativeAsset {

    public NativeImageAsset() {
        super(REQUEST_ASSET.IMAGE);
    }

    public enum IMAGE_TYPE {
        ICON(1),
        MAIN(3),
        CUSTOM(500);
        private int id;

        IMAGE_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && !inExistingValue(id)) {
                this.id = id;
            }
        }

        private boolean inExistingValue(int id) {
            IMAGE_TYPE[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (IMAGE_TYPE value : possibleValues) {
                if (!value.equals(IMAGE_TYPE.CUSTOM) && value.getID() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    private IMAGE_TYPE type;

    public void setImageType(IMAGE_TYPE type) {
        this.type = type;
    }

    public IMAGE_TYPE getImageType() {
        return type;
    }

    private int wmin = -1;

    public void setWMin(int wmin) {
        this.wmin = wmin;
    }

    public int getWMin() {
        return wmin;
    }

    private int hmin = -1;

    public int getHMin() {
        return hmin;
    }

    public void setHMin(int hmin) {
        this.hmin = hmin;
    }

    private int w = -1;

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    private int h = -1;

    public void setH(int h) {
        this.h = h;
    }

    public int getH() {
        return h;
    }

    private ArrayList<String> mimes = new ArrayList<>();

    public void addMime(String mime) {
        mimes.add(mime);
    }

    public ArrayList<String> getMimes() {
        return mimes;
    }

    private boolean required = false;

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    private Object assetExt = null;

    public void setAssetExt(Object assetExt) {
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            this.assetExt = assetExt;
        }
    }

    public Object getAssetExt() {
        return assetExt;
    }

    private Object imageExt = null;

    public void setImageExt(Object imageExt) {
        if (imageExt instanceof JSONArray || imageExt instanceof JSONObject) {
            this.imageExt = imageExt;
        }
    }

    public Object getImageExt() {
        return imageExt;
    }
}
