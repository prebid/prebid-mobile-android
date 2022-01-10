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

package org.prebid.mobile.rendering.bidding.data.ntv;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import org.prebid.mobile.rendering.bidding.listeners.NativeAdListener;
import org.prebid.mobile.rendering.listeners.OnBrowserActionResultListener;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetImage;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.sdk.JSLibraryManager;
import org.prebid.mobile.rendering.session.manager.NativeOmVerification;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
     * @return if present - first nativeAdImage.url (with {@link NativeAssetImage.ImageType#ICON} {@link NativeAssetImage.ImageType}), else - empty string.
     */
    @NonNull
    public String getIconUrl() {
        List<NativeAdImage> nativeAdIconList = getNativeAdImageList(NativeAssetImage.ImageType.ICON);
        NativeAdImage iconImage = getFirstOrNull(nativeAdIconList);

        return iconImage != null ? iconImage.getUrl() : "";
    }

    /**
     * @return if present - first nativeAdImage.url (with {@link NativeAssetImage.ImageType#MAIN} {@link NativeAssetImage.ImageType}, else - empty string.
     */
    @NonNull
    public String getImageUrl() {
        List<NativeAdImage> nativeMainImageList = getNativeAdImageList(NativeAssetImage.ImageType.MAIN);
        NativeAdImage mainImage = getFirstOrNull(nativeMainImageList);

        return mainImage != null ? mainImage.getUrl() : "";
    }

    /**
     * @return if present - first nativeAdData.value (with {@link NativeAssetData.DataType#CTA_TEXT} {@link NativeAssetData.DataType}, else - empty string.
     */
    @NonNull
    public String getCallToAction() {
        List<NativeAdData> nativeCtaDataList = getNativeAdDataList(NativeAssetData.DataType.CTA_TEXT);
        NativeAdData ctaData = getFirstOrNull(nativeCtaDataList);

        return ctaData != null ? ctaData.getValue() : "";
    }

    /**
     * @return if present - first nativeAdData.value (with {@link NativeAssetData.DataType#DESC} {@link NativeAssetData.DataType}, else - empty string.
     */
    @NonNull
    public String getText() {
        List<NativeAdData> nativeTextDataList = getNativeAdDataList(NativeAssetData.DataType.DESC);
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
    public List<NativeAdImage> getNativeAdImageList(NativeAssetImage.ImageType type) {
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
    public List<NativeAdData> getNativeAdDataList(NativeAssetData.DataType type) {
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
                nativeAdLinks = getNativeAdImageList(NativeAssetImage.ImageType.ICON);
                break;
            case TITLE_VIEW:
                nativeAdLinks = mNativeAdTitleList;
                break;
            case MAIN_IMAGE_VIEW:
                nativeAdLinks = getNativeAdImageList(NativeAssetImage.ImageType.MAIN);
                break;
            case CONTENT_VIEW:
                nativeAdLinks = getNativeAdDataList(NativeAssetData.DataType.DESC);
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
                LogUtil.error(TAG, "Failed to init OmAdSessionManager");
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
                LogUtil.error(TAG, "handleClickAction failed. NativeAdLink is null or url is empty.");
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
                LogUtil.debug(TAG, "handleUrl(): Primary URL failed. Attempting to process fallback URL");
                String fallbackUrl = nativeAdLink.getFallback();
                if (fallbackUrl == null || fallbackUrl.isEmpty()) {
                    LogUtil.debug(TAG, "handleUrl(): No fallback URL was provided.");
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
