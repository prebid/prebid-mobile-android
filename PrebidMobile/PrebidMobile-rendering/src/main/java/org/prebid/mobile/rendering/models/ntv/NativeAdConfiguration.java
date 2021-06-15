/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.models.ntv;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAsset;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for Native ads
 */
public class NativeAdConfiguration {

    private ContextType mContextType;
    private ContextSubType mContextSubType;
    private PlacementType mPlacementType;
    private boolean mPrivacy;
    private Integer mSeq;

    private final List<NativeAsset> mAssets = new ArrayList<>();
    private final List<NativeEventTracker> mTrackers = new ArrayList<>();

    private String mNativeStylesCreative;
    private Ext mExt;

    /**
     * Adds NativeAsset to assets list
     *
     * @param asset of class extending NativeAsset
     */
    public void addAsset(NativeAsset asset) {
        mAssets.add(asset);
    }

    public List<NativeAsset> getAssets() {
        return mAssets;
    }

    /**
     * Adds NativeEventTracker to event trackers list
     *
     * @param tracker
     */
    public void addTracker(NativeEventTracker tracker) {
        mTrackers.add(tracker);
    }

    public List<NativeEventTracker> getTrackers() {
        return mTrackers;
    }

    public ContextType getContextType() {
        return mContextType;
    }

    /**
     * Sets context in which the ad appears
     *
     * @param contextType
     */
    public void setContextType(ContextType contextType) {
        mContextType = contextType;
    }

    public ContextSubType getContextSubType() {
        return mContextSubType;
    }

    /**
     * Sets context sub type in which the ad appears
     *
     * @param contextSubType
     */
    public void setContextSubType(ContextSubType contextSubType) {
        mContextSubType = contextSubType;
    }

    public PlacementType getPlacementType() {
        return mPlacementType;
    }

    /**
     * Sets the design/format/layout of	the	ad unit being offered
     *
     * @return
     */
    public void setPlacementType(PlacementType placementType) {
        mPlacementType = placementType;
    }

    public Ext getExt() {
        return mExt;
    }

    /**
     * Sets the extension object
     *
     * @param ext
     */
    public void setExt(Ext ext) {
        mExt = ext;
    }

    public boolean getPrivacy() {
        return mPrivacy;
    }

    /**
     * Set to true when	the	native ad supports buyer-specific privacy notice
     *
     * @param privacy
     */
    public void setPrivacy(boolean privacy) {
        mPrivacy = privacy;
    }

    @Nullable
    public Integer getSeq() {
        return mSeq;
    }

    /**
     * 0 for the first ad, 1 for the second	ad,	and	so on
     *
     * @param seq
     */
    public void setSeq(int seq) {
        if (seq < 0) {
            return;
        }
        mSeq = seq;
    }

    /**
     * Set creative HTML for integration without primary ad server
     *
     * @param nativeStylesCreative - HTML string
     */
    public void setNativeStylesCreative(String nativeStylesCreative) {
        mNativeStylesCreative = nativeStylesCreative;
    }

    public String getNativeStylesCreative() {
        return mNativeStylesCreative;
    }

    public enum ContextType {
        CONTENT_CENTRIC(1),
        SOCIAL_CENTRIC(2),
        PRODUCT(3),
        CUSTOM(500);

        private int mId;

        ContextType(final int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }

        public void setId(int id) {
            if (equals(CUSTOM) && !inExistingValue(id)) {
                mId = id;
            }
        }

        private boolean inExistingValue(int id) {
            ContextType[] possibleValues = getDeclaringClass().getEnumConstants();
            for (ContextType value : possibleValues) {
                if (!value.equals(ContextType.CUSTOM) && value.getId() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum ContextSubType {
        GENERAL(10),
        ARTICLE(11),
        VIDEO(12),
        AUDIO(13),
        IMAGE(14),
        USER_GENERATED(15),
        GENERAL_SOCIAL(20),
        EMAIL(21),
        CHAT_IM(22),
        SELLING(30),
        APPLICATION_STORE(31),
        PRODUCT_REVIEW_SITES(32),
        CUSTOM(500);

        private int mId;

        ContextSubType(final int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }

        public void setId(int id) {
            if (equals(CUSTOM) && !inExistingValue(id)) {
                mId = id;
            }
        }

        private boolean inExistingValue(int id) {
            ContextSubType[] possibleValues = getDeclaringClass().getEnumConstants();
            for (ContextSubType value : possibleValues) {
                if (!value.equals(ContextSubType.CUSTOM) && value.getId() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum PlacementType {
        CONTENT_FEED(1),
        CONTENT_ATOMIC_UNIT(2),
        OUTSIDE_CORE_CONTENT(3),
        RECOMMENDATION_WIDGET(4),
        CUSTOM(500);

        private int mId;

        PlacementType(final int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }

        public void setId(int id) {
            if (equals(CUSTOM) && !inExistingValue(id)) {
                mId = id;
            }
        }

        private boolean inExistingValue(int id) {
            PlacementType[] possibleValues = getDeclaringClass().getEnumConstants();
            for (PlacementType value : possibleValues) {
                if (!value.equals(PlacementType.CUSTOM) && value.getId() == id) {
                    return true;
                }
            }
            return false;
        }
    }
}
