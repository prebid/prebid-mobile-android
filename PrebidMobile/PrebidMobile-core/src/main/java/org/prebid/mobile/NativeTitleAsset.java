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

import org.json.JSONArray;
import org.json.JSONObject;

public class NativeTitleAsset extends NativeAsset {
    private int len;
    private boolean required;
    private Object titleExt;
    private Object assetExt;

    public NativeTitleAsset() {
        super(REQUEST_ASSET.TITLE);
    }

    public void setLength(int len) {
        this.len = len;
    }

    public int getLen() {
        return len;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public void setTitleExt(Object ext) {
        if (ext instanceof JSONArray || ext instanceof JSONObject) {
            this.titleExt = ext;
        }
    }

    public Object getTitleExt() {
        return titleExt;
    }

    public Object getAssetExt() {
        return assetExt;
    }

    public void setAssetExt(Object assetExt) {
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            this.assetExt = assetExt;
        }
    }
}
