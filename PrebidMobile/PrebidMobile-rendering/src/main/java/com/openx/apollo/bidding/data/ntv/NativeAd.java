package com.openx.apollo.bidding.data.ntv;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.listeners.NativeAdListener;
import com.openx.apollo.listeners.OnBrowserActionResultListener;
import com.openx.apollo.models.CreativeVisibilityTracker;
import com.openx.apollo.models.internal.VisibilityTrackerOption;
import com.openx.apollo.models.internal.VisibilityTrackerResult;
import com.openx.apollo.models.ntv.NativeEventTracker;
import com.openx.apollo.models.openrtb.bidRequests.Ext;
import com.openx.apollo.networking.tracking.TrackingManager;
import com.openx.apollo.sdk.JSLibraryManager;
import com.openx.apollo.session.manager.NativeOmVerification;
import com.openx.apollo.session.manager.OmAdSessionManager;
import com.openx.apollo.utils.helpers.ExternalViewerUtils;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.utils.url.ActionNotResolvedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetData.DataType;
import static com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetImage.ImageType;

public class NativeAd {
    private static final String TAG = NativeAd.class.getSimpleName();

    @NonNull
    private final String mVersion;

    @Nullable
    private final NativeAdLink mNativeAdLink;
    @Nullable
    private final Ext mExt;

    @NonNull
    private final List<NativeAdTitle> mNativeAdTitleList;
    @NonNull
    private final List<NativeAdImage> mNativeAdImageList;
    @NonNull
    private final List<NativeAdData> mNativeAdDataList;
    @NonNull
    private final List<NativeAdVideo> mNativeAdVideoList;
    @NonNull
    private final List<NativeAdEventTracker> mNativeAdEventTrackerList;

    @Nullable
    private NativeAdListener mNativeAdListener;

    private CreativeVisibilityTracker mVisibilityTracker;
    private final CreativeVisibilityTracker.VisibilityTrackerListener mVisibilityTrackerListener = this::handleVisibilityChange;
    private final OnBrowserActionResultListener mOnBrowserActionResultListener = browserActionResult -> notifyClick();

    private OmAdSessionManager mOmAdSessionManager;

    NativeAd(
        @NonNull
            String version,
        @Nullable
            NativeAdLink nativeAdLink,
        @Nullable
            Ext ext,
        @NonNull
            List<NativeAdTitle> nativeAdTitleList,
        @NonNull
            List<NativeAdImage> nativeAdImageList,
        @NonNull
            List<NativeAdData> nativeAdDataList,
        @NonNull
            List<NativeAdVideo> nativeAdVideoList,
        @NonNull
            List<NativeAdEventTracker> nativeAdEventTrackersList) {
        mVersion = version;
        mNativeAdLink = nativeAdLink;
        mExt = ext;

        mNativeAdTitleList = nativeAdTitleList;
        mNativeAdImageList = nativeAdImageList;
        mNativeAdDataList = nativeAdDataList;
        mNativeAdVideoList = nativeAdVideoList;
        mNativeAdEventTrackerList = nativeAdEventTrackersList;
    }

    /**
     * @return if present - version, else - empty string.
     */
    @NonNull
    public String getVersion() {
        return mVersion;
    }

    /**
     * @return if present - first nativeAdTitle.text, else - empty string.
     */
    @NonNull
    public String getTitle() {
        NativeAdTitle nativeAdTitle = getFirstOrNull(mNativeAdTitleList);
        return nativeAdTitle != null ? nativeAdTitle.getText() : "";
    }

    /**
     * @return if present - first nativeAdImage.url (with {@link ImageType#ICON} {@link ImageType}), else - empty string.
     */
    @NonNull
    public String getIconUrl() {
        List<NativeAdImage> nativeAdIconList = getNativeAdImageList(ImageType.ICON);
        NativeAdImage iconImage = getFirstOrNull(nativeAdIconList);

        return iconImage != null ? iconImage.getUrl() : "";
    }

    /**
     * @return if present - first nativeAdImage.url (with {@link ImageType#MAIN} {@link ImageType}, else - empty string.
     */
    @NonNull
    public String getImageUrl() {
        List<NativeAdImage> nativeMainImageList = getNativeAdImageList(ImageType.MAIN);
        NativeAdImage mainImage = getFirstOrNull(nativeMainImageList);

        return mainImage != null ? mainImage.getUrl() : "";
    }

    /**
     * @return if present - first nativeAdData.value (with {@link DataType#CTA_TEXT} {@link DataType}, else - empty string.
     */
    @NonNull
    public String getCallToAction() {
        List<NativeAdData> nativeCtaDataList = getNativeAdDataList(DataType.CTA_TEXT);
        NativeAdData ctaData = getFirstOrNull(nativeCtaDataList);

        return ctaData != null ? ctaData.getValue() : "";
    }

    /**
     * @return if present - first nativeAdData.value (with {@link DataType#DESC} {@link DataType}, else - empty string.
     */
    @NonNull
    public String getText() {
        List<NativeAdData> nativeTextDataList = getNativeAdDataList(DataType.DESC);
        NativeAdData descData = getFirstOrNull(nativeTextDataList);

        return descData != null ? descData.getValue() : "";
    }

    /**
     * @return if present - first nativeAdVideo, else - nativeAdVideo with empty vastTag.
     */
    @NonNull
    public NativeAdVideo getNativeVideoAd() {
        NativeAdVideo nativeAdVideo = getFirstOrNull(mNativeAdVideoList);
        return nativeAdVideo != null ? nativeAdVideo : new NativeAdVideo();
    }

    /**
     * @return if present - root level ext, else - null.
     */
    @Nullable
    public Ext getExt() {
        return mExt;
    }

    /**
     * @return list containing {@link NativeAdTitle} or empty list.
     */
    @NonNull
    public List<NativeAdTitle> getNativeAdTitleList() {
        return mNativeAdTitleList;
    }

    /**
     * @return list containing {@link NativeAdImage} or empty list.
     */
    @NonNull
    public List<NativeAdImage> getNativeAdImageList() {
        return mNativeAdImageList;
    }

    /**
     * @return list containing {@link NativeAdData} or empty list.
     */
    @NonNull
    public List<NativeAdData> getNativeAdDataList() {
        return mNativeAdDataList;
    }

    /**
     * @return list containing {@link NativeAdVideo} or empty list.
     */
    @NonNull
    public List<NativeAdVideo> getNativeAdVideoList() {
        return mNativeAdVideoList;
    }

    /**
     * @param type - filter parameter.
     * @return list containing filtered {@link NativeAdImage} or empty list.
     */
    @NonNull
    public List<NativeAdImage> getNativeAdImageList(ImageType type) {
        List<NativeAdImage> nativeAdImageList = new ArrayList<>();

        if (type == null || !isNotEmpty(mNativeAdImageList)) {
            return nativeAdImageList;
        }

        for (NativeAdImage nativeAdImage : mNativeAdImageList) {
            if (type.equals(nativeAdImage.getImageType())) {
                nativeAdImageList.add(nativeAdImage);
            }
        }

        return nativeAdImageList;
    }

    /**
     * @param type - filter parameter.
     * @return list containing filtered {@link NativeAdData} or empty list.
     */
    @NonNull
    public List<NativeAdData> getNativeAdDataList(DataType type) {
        List<NativeAdData> nativeAdDataList = new ArrayList<>();

        if (type == null || !isNotEmpty(mNativeAdDataList)) {
            return nativeAdDataList;
        }

        for (NativeAdData nativeAdData : mNativeAdDataList) {
            if (type.equals(nativeAdData.getDataType())) {
                nativeAdDataList.add(nativeAdData);
            }
        }

        return nativeAdDataList;
    }

    public void setNativeAdListener(
        @Nullable
            NativeAdListener nativeAdListener) {
        mNativeAdListener = nativeAdListener;
    }

    public void registerView(
        @NonNull
            View adView,
        @NonNull
            View... clickableViews) {
        startOmSession(adView);
        startVisibilityTracking(adView);

        for (View clickableView : clickableViews) {
            clickableView.setOnClickListener(v -> handleClickAction(v.getContext(), mNativeAdLink));
        }
    }

    public void registerClickView(
        @NonNull
            View clickView,
        @NonNull
            NativeAdElementType type) {
        Context context = clickView.getContext();

        final List<? extends BaseNativeAdElement> nativeAdLinks;
        switch (type) {
            case ICON_VIEW:
                nativeAdLinks = getNativeAdImageList(ImageType.ICON);
                break;
            case TITLE_VIEW:
                nativeAdLinks = mNativeAdTitleList;
                break;
            case MAIN_IMAGE_VIEW:
                nativeAdLinks = getNativeAdImageList(ImageType.MAIN);
                break;
            case CONTENT_VIEW:
                nativeAdLinks = getNativeAdDataList(DataType.DESC);
                break;
            case VIDEO_VIEW:
                nativeAdLinks = mNativeAdVideoList;
                break;
            default:
                nativeAdLinks = new ArrayList<>();
        }

        clickView.setOnClickListener(v -> handleClickAction(context, getFirstOrNullNativeAdLink(nativeAdLinks)));
    }

    public void registerClickView(
        @NonNull
            View clickView,
        @NonNull
            BaseNativeAdElement baseNativeAdElement) {
        final Context context = clickView.getContext();
        clickView.setOnClickListener(view -> handleClickAction(context, baseNativeAdElement.getNativeAdLink()));
    }

    public void destroy() {
        stopVisibilityTracking();
        if (mOmAdSessionManager != null) {
            mOmAdSessionManager.stopAdSession();
            mOmAdSessionManager = null;
        }
    }

    private void startVisibilityTracking(
        @NonNull
            View adView) {
        stopVisibilityTracking();

        mVisibilityTracker = new CreativeVisibilityTracker(adView, extractVisibilityTrackerOptions());
        mVisibilityTracker.setVisibilityTrackerListener(mVisibilityTrackerListener);
        mVisibilityTracker.startVisibilityCheck(adView.getContext());
    }

    private void startOmSession(View adView) {
        NativeOmVerification nativeOmVerification = getOmVerification();
        if (nativeOmVerification == null) {
            return;
        }

        if (mOmAdSessionManager == null) {
            mOmAdSessionManager = OmAdSessionManager.createNewInstance(JSLibraryManager.getInstance(adView.getContext()));
            if (mOmAdSessionManager == null) {
                OXLog.error(TAG, "Failed to init OmAdSessionManager");
                return;
            }
        }
        String contentUrl = null;
        if (mNativeAdLink != null) {
            contentUrl = mNativeAdLink.getUrl();
        }
        mOmAdSessionManager.initNativeDisplayAdSession(adView, nativeOmVerification, contentUrl);
        mOmAdSessionManager.startAdSession();
        fireOmLoaded();
    }

    private void fireOmImpression() {
        if (mOmAdSessionManager != null) {
            mOmAdSessionManager.registerImpression();
        }
    }

    private void fireOmLoaded() {
        if (mOmAdSessionManager != null) {
            mOmAdSessionManager.displayAdLoaded();
        }
    }

    private Set<VisibilityTrackerOption> extractVisibilityTrackerOptions() {
        Set<VisibilityTrackerOption> visibilityTrackerOptionsSet = new HashSet<>();

        for (NativeAdEventTracker eventTracker : getImageTrackingList()) {
            final NativeEventTracker.EventType eventType = eventTracker.getEventType();
            final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(eventType);

            visibilityTrackerOptionsSet.add(visibilityTrackerOption);
        }
        return visibilityTrackerOptionsSet;
    }

    private List<NativeAdEventTracker> getImageTrackingList() {
        List<NativeAdEventTracker> nativeAdEventTrackerList = new ArrayList<>();
        for (NativeAdEventTracker eventTracker : mNativeAdEventTrackerList) {

            if (eventTracker.isSupportedEventMethod()) {
                nativeAdEventTrackerList.add(eventTracker);
            }
        }
        return nativeAdEventTrackerList;
    }

    private void handleVisibilityChange(VisibilityTrackerResult result) {
        if (result.isVisible() && result.shouldFireImpression()) {
            final NativeEventTracker.EventType eventType = result.getEventType();
            final List<String> trackingUrls = getTrackingUrls(eventType);

            if (eventType == NativeEventTracker.EventType.OMID) {
                fireOmImpression();
            }
            else {
                TrackingManager.getInstance().fireEventTrackingURLs(trackingUrls);
            }
            if (mNativeAdListener != null) {
                mNativeAdListener.onAdEvent(this, eventType);
            }
        }
    }

    private void handleClickAction(Context context, NativeAdLink nativeAdLink) {
        if (nativeAdLink == null || nativeAdLink.getUrl().isEmpty()) {
            if (mNativeAdLink == null || mNativeAdLink.getUrl().isEmpty()) {
                OXLog.error(TAG, "handleClickAction failed. NativeAdLink is null or url is empty.");
                return;
            }
            // If asset's link is null, use parent's one
            nativeAdLink = mNativeAdLink;
        }
        handleUrl(context, nativeAdLink);
        TrackingManager.getInstance().fireEventTrackingURLs(nativeAdLink.getClickTrackers());
    }

    private void handleUrl(Context context, NativeAdLink nativeAdLink) {
        if (nativeAdLink.isDeeplink()) {
            try {
                ExternalViewerUtils.launchApplicationUrl(context, Uri.parse(nativeAdLink.getUrl()));
                notifyClick();
            }
            catch (ActionNotResolvedException e) {
                OXLog.debug(TAG, "handleUrl(): Primary URL failed. Attempting to process fallback URL");
                String fallbackUrl = nativeAdLink.getFallback();
                if (fallbackUrl == null || fallbackUrl.isEmpty()) {
                    OXLog.debug(TAG, "handleUrl(): No fallback URL was provided.");
                    return;
                }
                launchUrlInBrowser(context, fallbackUrl);
            }
        }
        else {
            launchUrlInBrowser(context, nativeAdLink.getUrl());
        }
    }

    private void launchUrlInBrowser(Context context, String url) {
        ExternalViewerUtils.startBrowser(context, url, false, mOnBrowserActionResultListener);
    }

    private void notifyClick() {
        if (mNativeAdListener != null) {
            mNativeAdListener.onAdClicked(NativeAd.this);
        }
    }

    private List<String> getTrackingUrls(NativeEventTracker.EventType eventType) {
        List<String> trackingUrls = new ArrayList<>();

        for (NativeAdEventTracker eventTracker : mNativeAdEventTrackerList) {
            if (eventTracker.isSupportedEventMethod() && eventType.equals(eventTracker.getEventType())) {
                trackingUrls.add(eventTracker.getUrl());
            }
        }

        return trackingUrls;
    }

    private void stopVisibilityTracking() {
        if (mVisibilityTracker != null) {
            mVisibilityTracker.stopVisibilityCheck();
        }
    }

    private NativeAdLink getFirstOrNullNativeAdLink(List<? extends BaseNativeAdElement> list) {
        return isNotEmpty(list) ? list.get(0).getNativeAdLink() : null;
    }

    private <T> T getFirstOrNull(List<T> list) {
        return isNotEmpty(list) ? list.get(0) : null;
    }

    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    private NativeOmVerification getOmVerification() {
        for (NativeAdEventTracker eventTracker : mNativeAdEventTrackerList) {
            if (eventTracker.getEventType() == NativeEventTracker.EventType.OMID
                && eventTracker.getEventTrackingMethod() == NativeEventTracker.EventTrackingMethod.JS) {
                return new NativeOmVerification(eventTracker);
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NativeAd nativeAd = (NativeAd) o;

        if (!mVersion.equals(nativeAd.mVersion)) {
            return false;
        }
        if (mNativeAdLink != null
            ? !mNativeAdLink.equals(nativeAd.mNativeAdLink)
            : nativeAd.mNativeAdLink != null) {
            return false;
        }
        if (mExt != null ? !mExt.equals(nativeAd.mExt) : nativeAd.mExt != null) {
            return false;
        }
        if (!mNativeAdTitleList.equals(nativeAd.mNativeAdTitleList)) {
            return false;
        }
        if (!mNativeAdImageList.equals(nativeAd.mNativeAdImageList)) {
            return false;
        }
        if (!mNativeAdDataList.equals(nativeAd.mNativeAdDataList)) {
            return false;
        }
        if (!mNativeAdVideoList.equals(nativeAd.mNativeAdVideoList)) {
            return false;
        }
        return mNativeAdEventTrackerList.equals(nativeAd.mNativeAdEventTrackerList);
    }

    @Override
    public int hashCode() {
        int result = mVersion.hashCode();
        result = 31 * result + (mNativeAdLink != null ? mNativeAdLink.hashCode() : 0);
        result = 31 * result + (mExt != null ? mExt.hashCode() : 0);
        result = 31 * result + mNativeAdTitleList.hashCode();
        result = 31 * result + mNativeAdImageList.hashCode();
        result = 31 * result + mNativeAdDataList.hashCode();
        result = 31 * result + mNativeAdVideoList.hashCode();
        result = 31 * result + mNativeAdEventTrackerList.hashCode();
        return result;
    }
}
