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

package org.prebid.mobile.rendering.utils.helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.listeners.OnBrowserActionResultListener;
import org.prebid.mobile.rendering.listeners.OnBrowserActionResultListener.BrowserActionResult;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.views.browser.AdBrowserActivity;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ExternalViewerUtils {

    public static final String TAG = ExternalViewerUtils.class.getSimpleName();

    /**
     * Starts new activity and checks if current context can run new activity.
     * If it can't run, it adds flag FLAG_ACTIVITY_NEW_TASK.
     */
    public static void startActivity(
        @Nullable Context context,
        @Nullable Intent intent
    ) {
        if (context == null || intent == null) {
            Log.e(TAG, "Can't start activity!");
            return;
        }

        boolean contextCanNotRunNewActivity = !(context instanceof Activity);
        if (contextCanNotRunNewActivity) {
            Log.d(TAG, "Context is not Activity type. Intent flag FLAG_ACTIVITY_NEW_TASK added.");
            boolean isRelease = !BuildConfig.DEBUG;
            if (isRelease) {
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            }
        }

        context.startActivity(intent);
    }

    public static boolean isBrowserActivityCallable(Context context) {
        if (context == null) {
            LogUtil.debug(TAG, "isBrowserActivityCallable(): returning false. Context is null");
            return false;
        }

        Intent browserActivityIntent = new Intent(context, AdBrowserActivity.class);
        return isActivityCallable(context, browserActivityIntent);
    }

    /**
     * Checks if the intent's activity is declared in the manifest
     */
    public static boolean isActivityCallable(Context context, Intent intent) {
        if (context == null || intent == null) {
            LogUtil.debug(TAG, "isActivityCallable(): returning false. Intent or context is null");
            return false;
        }

        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void startExternalVideoPlayer(Context context, String url) {
        if (context != null && url != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            startActivity(context, intent);
        }
    }

    public static void launchApplicationUrl(Context context, Uri uri)
    throws ActionNotResolvedException {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (!isActivityCallable(context, intent)) {
            throw new ActionNotResolvedException("launchApplicationUrl: Failure. No activity was found to handle action for " + uri);
        }

        launchApplicationIntent(context, intent);
    }

    public static void startBrowser(Context context, String url,
                                    boolean shouldFireEvents, @Nullable
                                        OnBrowserActionResultListener onBrowserActionResultListener) {
        startBrowser(context, url, -1, shouldFireEvents,  onBrowserActionResultListener);
    }

    public static void startBrowser(Context context, String url, int broadcastId,
                                    boolean shouldFireEvents, @Nullable
                                        OnBrowserActionResultListener onBrowserActionResultListener) {

        Intent intent = new Intent(context, AdBrowserActivity.class);
        intent.putExtra(AdBrowserActivity.EXTRA_URL, url);
        intent.putExtra(AdBrowserActivity.EXTRA_DENSITY_SCALING_ENABLED, false);
        intent.putExtra(AdBrowserActivity.EXTRA_ALLOW_ORIENTATION_CHANGES, true);
        intent.putExtra(AdBrowserActivity.EXTRA_SHOULD_FIRE_EVENTS, shouldFireEvents);
        intent.putExtra(AdBrowserActivity.EXTRA_BROADCAST_ID, broadcastId);

        if (!(context instanceof Activity)) {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }

        if (!PrebidMobile.useExternalBrowser && isActivityCallable(context, intent)) {
            startActivity(context, intent);
            notifyBrowserActionSuccess(BrowserActionResult.INTERNAL_BROWSER, onBrowserActionResultListener);
        } else {
            startExternalBrowser(context, url);
            notifyBrowserActionSuccess(BrowserActionResult.EXTERNAL_BROWSER, onBrowserActionResultListener);
        }
    }

    private static void startExternalBrowser(Context context, String url) {
        if (context == null || url == null) {
            LogUtil.error(TAG, "startExternalBrowser: Failure. Context or URL is null");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (URLUtil.isValidUrl(url) || isActivityCallable(context, intent)) {
            startActivity(context, intent);
        }
        else {
            LogUtil.error(TAG, "No activity available to handle action " + intent.toString());
        }
    }

    @VisibleForTesting
    static void launchApplicationIntent(
        @NonNull
        final Context context,
        @NonNull
        final Intent intent)
    throws ActionNotResolvedException {
        if (!(context instanceof Activity)) {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            startActivity(context, intent);
        }
        catch (ActivityNotFoundException e) {
            throw new ActionNotResolvedException(e);
        }
    }

    private static void notifyBrowserActionSuccess(BrowserActionResult browserActionResult,
                                                   @Nullable
                                                       OnBrowserActionResultListener onBrowserActionResultListener) {

        if (onBrowserActionResultListener == null) {
            LogUtil.debug(TAG, "notifyBrowserActionSuccess(): Failed. BrowserActionResultListener is null.");
            return;
        }

        onBrowserActionResultListener.onSuccess(browserActionResult);
    }
}
