package org.prebid.mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.json.JSONObject;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.NativeAdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Original API native ad unit.
 * For details of the configuration of native imps, please check this documentation:
 * https://www.iab.com/wp-content/uploads/2018/03/OpenRTB-Native-Ads-Specification-Final-1.2.pdf
 */
public class NativeAdUnit extends AdUnit {

    /**
     * Internal key for caching native ad.
     */
    public static final String BUNDLE_KEY_CACHE_ID = "NativeAdUnitCacheId";

    private final NativeAdUnitConfiguration nativeConfiguration;

    /**
     * Default constructor.
     */
    public NativeAdUnit(@NonNull String configId) {
        super(configId, EnumSet.of(AdFormat.NATIVE));
        nativeConfiguration = configuration.getNativeConfiguration();
    }

    @Override
    protected BidRequesterListener createBidListener(OnCompleteListener originalListener) {
        return new BidRequesterListener() {
            @Override
            public void onFetchCompleted(BidResponse response) {
                bidResponse = response;

                HashMap<String, String> keywords = response.getTargeting();
                Util.apply(keywords, adObject);

                String cacheId = CacheManager.save(response.getWinningBidJson());
                Util.saveCacheId(cacheId, adObject);

                notifyWinEvent(response);
                originalListener.onComplete(ResultCode.SUCCESS);
            }

            @Override
            public void onError(AdException exception) {
                bidResponse = null;

                Util.apply(null, adObject);
                originalListener.onComplete(convertToResultCode(exception));
            }
        };
    }

    /**
     * Context type for native request.
     */
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
        nativeConfiguration.setContextType(type);
    }

    /**
     * Context subtype for native request.
     */
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
        nativeConfiguration.setContextSubtype(type);
    }

    /**
     * Placement type for native request.
     */
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
        nativeConfiguration.setPlacementType(placementType);
    }

    public void setPlacementCount(int placementCount) {
        nativeConfiguration.setPlacementCount(placementCount);
    }

    public void setSeq(int seq) {
        nativeConfiguration.setSeq(seq);
    }

    public void setAUrlSupport(boolean support) {
        nativeConfiguration.setAUrlSupport(support);
    }

    public void setDUrlSupport(boolean support) {
        nativeConfiguration.setDUrlSupport(support);
    }

    public void setPrivacy(boolean privacy) {
        nativeConfiguration.setPrivacy(privacy);
    }

    public void setExt(Object jsonObject) {
        if (jsonObject instanceof JSONObject) {
            nativeConfiguration.setExt((JSONObject) jsonObject);
        }
    }

    public void addEventTracker(NativeEventTracker tracker) {
        nativeConfiguration.addEventTracker(tracker);
    }

    public void addAsset(NativeAsset asset) {
        nativeConfiguration.addAsset(asset);
    }

    @VisibleForTesting
    public NativeAdUnitConfiguration getNativeConfiguration() {
        return nativeConfiguration;
    }

    @Nullable
    public String getImpOrtbConfig() {return configuration.getImpOrtbConfig();}

    public void setImpOrtbConfig(@Nullable String ortbConfig) {configuration.setImpOrtbConfig(ortbConfig);}

    private void notifyWinEvent(BidResponse response) {
        if (response == null) return;

        Bid winningBid = response.getWinningBid();
        if (winningBid == null) return;

        ServerConnection.fireAndForget(winningBid.getNurl());
    }

}
