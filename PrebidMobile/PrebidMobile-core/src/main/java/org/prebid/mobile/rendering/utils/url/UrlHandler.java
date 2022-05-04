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

package org.prebid.mobile.rendering.utils.url;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.mraid.methods.network.UrlResolutionTask;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.utils.url.action.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code UrlHandler} facilitates handling user clicks on different URLs, allowing configuration
 * for which kinds of URLs to handle and then responding accordingly for a given URL.
 * <p>
 * This class is designed to be instantiated for a single use by immediately calling its {@link
 * #handleUrl(Context, String, List, boolean)} method upon constructing it.
 */

public class UrlHandler {
    private static final String TAG = UrlHandler.class.getSimpleName();

    /**
     * {@link UrlHandlerResultListener} defines the methods that {@link UrlHandler} calls when handling a
     * certain click succeeds or fails.
     */
    public interface UrlHandlerResultListener {
        void onSuccess(String url, UrlAction urlAction);

        void onFailure(String url);
    }

    /**
     * Empty listener to omit null checks if variable is not defined when building {@link UrlHandler}
     */
    private static final UrlHandlerResultListener EMPTY_LISTENER = new UrlHandlerResultListener() {
        @Override
        public void onSuccess(String url, UrlAction urlAction) { }

        @Override
        public void onFailure(String url) { }
    };

    /**
     * {@link Builder} provides an API to configure {@link UrlHandler} and create it.
     */
    public static class Builder {

        private Set<UrlAction> supportedUrlHandlerList = new HashSet<>();
        private UrlHandlerResultListener urlHandlerResultListener = EMPTY_LISTENER;

        public Builder withDeepLinkPlusAction(@NonNull DeepLinkPlusAction deepLinkPlusAction) {
            supportedUrlHandlerList.add(deepLinkPlusAction);
            return this;
        }

        public Builder withDeepLinkAction(@NonNull DeepLinkAction deepLinkAction) {
            supportedUrlHandlerList.add(deepLinkAction);
            return this;
        }

        public Builder withMraidInternalBrowserAction(@NonNull MraidInternalBrowserAction mraidInternalBrowserAction) {
            supportedUrlHandlerList.add(mraidInternalBrowserAction);
            return this;
        }

        public Builder withBrowserAction(@NonNull BrowserAction browserAction) {
            supportedUrlHandlerList.add(browserAction);
            return this;
        }

        public Builder withResultListener(@NonNull UrlHandlerResultListener urlHandlerResultListener) {
            this.urlHandlerResultListener = urlHandlerResultListener;
            return this;
        }

        public UrlHandler build() {
            return new UrlHandler(supportedUrlHandlerList, urlHandlerResultListener);
        }

    }

    private final Set<UrlAction> supportedUrlActionList;
    private final UrlHandlerResultListener urlHandlerResultListener;

    private boolean alreadySucceeded;
    private boolean taskPending;

    /**
     * Use {@link Builder} to instantiate the {@link UrlHandler}
     */
    private UrlHandler(
            Set<UrlAction> supportedUrlActionList,
            UrlHandlerResultListener urlHandlerResultListener
    ) {
        this.supportedUrlActionList = supportedUrlActionList;
        this.urlHandlerResultListener = urlHandlerResultListener;
        taskPending = false;
        alreadySucceeded = false;
    }

    /**
     * Follows any redirects from {@code destinationUrl} and then handles the URL accordingly.
     *
     * @param context          The activity context.
     * @param url              The URL to handle.
     * @param isFromUserAction Whether this handling was triggered from a user interaction.
     * @param trackingUrls     Optional tracking URLs to trigger on success
     */
    public void handleUrl(Context context, String url, List<String> trackingUrls, boolean isFromUserAction) {
        if (url == null || TextUtils.isEmpty(url.trim())) {
            urlHandlerResultListener.onFailure(url);
            LogUtil.error(TAG, "handleUrl(): Attempted to handle empty url.");
            return;
        }

        UrlResolutionTask.UrlResolutionListener urlResolutionListener = new UrlResolutionTask.UrlResolutionListener() {
            @Override
            public void onSuccess(@NonNull String resolvedUrl) {
                taskPending = false;
                handleResolvedUrl(context, resolvedUrl, trackingUrls, isFromUserAction);
            }

            @Override
            public void onFailure(@NonNull String message, @Nullable Throwable throwable) {
                taskPending = false;
                urlHandlerResultListener.onFailure(url);
                LogUtil.error(TAG, message);
            }
        };

        performUrlResolutionRequest(url, urlResolutionListener);
    }

    /**
     * Performs the actual url handling by verifying that the {@code destinationUrl} is one of
     * the configured supported {@link UrlAction}s and then handling it accordingly.
     *
     * @param context          The activity context.
     * @param url              The URL to handle.
     * @param trackingUrlList  Optional tracking URLs to trigger on success
     * @param isFromUserAction Whether this handling was triggered from a user interaction.
     * @return true if the given URL was successfully handled; false otherwise
     */
    public boolean handleResolvedUrl(@NonNull final Context context,
                                     @NonNull final String url,
                                     @Nullable List<String> trackingUrlList,
                                     final boolean isFromUserAction) {
        if (TextUtils.isEmpty(url)) {
            urlHandlerResultListener.onFailure(url);
            LogUtil.error(TAG, "handleResolvedUrl(): Attempted to handle empty url.");
            return false;
        }

        final Uri destinationUri = Uri.parse(url);

        for (UrlAction urlAction : supportedUrlActionList) {
            if (urlAction.shouldOverrideUrlLoading(destinationUri)) {
                try {
                    handleAction(context, destinationUri, urlAction, isFromUserAction);
                    notifySuccess(url, trackingUrlList, urlAction);
                    return true;
                } catch (ActionNotResolvedException e) {
                    LogUtil.error(
                            TAG,
                            "handleResolvedUrl(): Unable to handle action: " + urlAction + " for given uri: " + destinationUri
                    );
                }
            }
        }
        urlHandlerResultListener.onFailure(url);
        return false;
    }

    @VisibleForTesting
    void handleAction(@NonNull Context context,
                      Uri destinationUri,
                      UrlAction urlAction,
                      boolean isFromUserInteraction)
    throws ActionNotResolvedException {
        if (urlAction.shouldBeTriggeredByUserAction() && !isFromUserInteraction) {
            throw new ActionNotResolvedException("Attempt to handle action without user interaction");
        }
        urlAction.performAction(context, UrlHandler.this, destinationUri);
    }

    @VisibleForTesting
    void performUrlResolutionRequest(String url, UrlResolutionTask.UrlResolutionListener urlResolutionListener) {
        UrlResolutionTask urlResolutionTask = new UrlResolutionTask(urlResolutionListener);
        urlResolutionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        taskPending = true;
    }

    private void notifySuccess(@NonNull String url,
                               @Nullable List<String> trackingUrlList,
                               UrlAction urlAction) {
        if (alreadySucceeded || taskPending) {
            LogUtil.warning(TAG, "notifySuccess(): Action is finished or action is still pending.");
            return;
        }

        TrackingManager.getInstance().fireEventTrackingURLs(trackingUrlList);
        urlHandlerResultListener.onSuccess(url, urlAction);
        alreadySucceeded = true;
    }
}
