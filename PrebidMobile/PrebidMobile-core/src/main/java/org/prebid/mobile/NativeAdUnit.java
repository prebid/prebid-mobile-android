package org.prebid.mobile;

import android.support.annotation.NonNull;

/**
 * For details of the configuration of native imps, please check this documentation:
 * https://www.iab.com/wp-content/uploads/2018/03/OpenRTB-Native-Ads-Specification-Final-1.2.pdf
 */
public class NativeAdUnit extends AdUnit {
    NativeRequestParams params;


    public NativeAdUnit(@NonNull String configId) {
        super(configId, AdType.NATIVE);
        params = new NativeRequestParams();
    }


    public enum CONTEXT_TYPE {
        CONTENT_CENTRIC(1),
        SOCIAL_CENTRIC(2),
        PRODUCT(3),
        CUSTOM(500);
        private int id;

        CONTEXT_TYPE(final int id) {
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
            CONTEXT_TYPE[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (CONTEXT_TYPE value : possibleValues) {
                if (!value.equals(CONTEXT_TYPE.CUSTOM) && value.getID() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setContextType(CONTEXT_TYPE type) {
        params.setContextType(type);
    }

    public enum CONTEXTSUBTYPE {
        GENERAL(10),
        ARTICAL(11),
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
        private int id;

        CONTEXTSUBTYPE(final int id) {
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
            CONTEXTSUBTYPE[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (CONTEXTSUBTYPE value : possibleValues) {
                if (!value.equals(CONTEXTSUBTYPE.CUSTOM) && value.getID() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setContextSubType(CONTEXTSUBTYPE type) {
        params.setContextSubType(type);
    }

    public enum PLACEMENTTYPE {
        CONTENT_FEED(1),
        CONTENT_ATOMIC_UNIT(2),
        OUTSIDE_CORE_CONTENT(3),
        RECOMMENDATION_WIDGET(4),
        CUSTOM(500);
        private int id;

        PLACEMENTTYPE(final int id) {
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
            PLACEMENTTYPE[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (PLACEMENTTYPE value : possibleValues) {
                if (!value.equals(PLACEMENTTYPE.CUSTOM) && value.getID() == id) {
                    return true;
                }
            }
            return false;
        }

    }

    public void setPlacementType(PLACEMENTTYPE placementType) {
        params.setPlacementType(placementType);
    }

    public void setPlacementCount(int placementCount) {
        params.setPlacementCount(placementCount);
    }

    public void setSeq(int seq) {
        params.setSeq(seq);
    }

    public void setAUrlSupport(boolean support) {
        params.setAUrlSupport(support);
    }

    public void setDUrlSupport(boolean support) {
        params.setDUrlSupport(support);
    }

    public void setPrivacy(boolean privacy) {
        params.setPrivacy(privacy);
    }

    public void setExt(Object jsonObject) {
        params.setExt(jsonObject);
    }

    public void addEventTracker(NativeEventTracker tracker) {
        params.addEventTracker(tracker);
    }

    public void addAsset(NativeAsset asset) {
        params.addAsset(asset);
    }

}
