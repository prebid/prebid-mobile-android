/*
 * © 2024 SMARTYADS. LDA doing business as “TEQBLAZE”.
 * All rights reserved. You may not use this file except in  compliance with the applicable  license granted to
 * you  by SMARTYADS,  LDA doing business as “TEQBLAZE”  (the "License"). Unless required by applicable law or
 * agreed to in writing, software distributed under the  License is distributed on  an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either  express or implied. Specific authorizations and restrictions
 * shall be provided for in the License.
 */

package org.prebid.mobile.rendering.interstitial.rewarded;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Reward for rewarded ad.
 * Bid response JSON object: {@code seatbid.bid[].ext.rwdd.reward}.
 */
public class Reward {

    private int count = 0;

    @NonNull
    private String type;

    @Nullable
    private JSONObject ext;

    public Reward(@NonNull String type, int count, @Nullable JSONObject ext) {
        this.type = type;
        this.count = count;
        this.ext = ext;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    @Nullable
    public JSONObject getExt() {
        return ext;
    }

    @Override
    public String toString() {
        return "Reward {" + "count=" + count + ", type='" + type + '\'' + ", ext=" + ext + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reward reward = (Reward) o;
        return count == reward.count && Objects.equals(type, reward.type) && Objects.equals(ext, reward.ext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, type, ext);
    }

}
