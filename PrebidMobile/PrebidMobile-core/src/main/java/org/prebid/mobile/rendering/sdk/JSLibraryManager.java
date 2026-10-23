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

package org.prebid.mobile.rendering.sdk;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptData;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Downloader and fetcher for JS scripts needed for the Prebid SDK (omsdk.js, mraid.js).
 * Top level class for working with JS scripts.
 * <p>
 * Every disk access runs on a background thread, so the methods can be called from the main thread.
 */
public class JSLibraryManager {

    private static final String TAG = "JSLibraryManager";

    private static JSLibraryManager sInstance;

    private volatile String MRAIDscript = "";
    private volatile String OMSDKscirpt = "";
    /**
     * Used only on {@link #diskExecutor}.
     */
    private JsScriptsDownloader scriptsDownloader;
    /**
     * Runs one task at a time, in order.
     */
    private final Executor diskExecutor;

    private JSLibraryManager(Context context) {
        diskExecutor = createDiskExecutor();
        // Creating the downloader reads the disk (Context.getFilesDir()).
        runOnDiskThread(() -> scriptsDownloader = JsScriptsDownloader.createDownloader(context));
    }

    @VisibleForTesting
    JSLibraryManager(
            JsScriptsDownloader scriptsDownloader,
            Executor diskExecutor
    ) {
        this.scriptsDownloader = scriptsDownloader;
        this.diskExecutor = diskExecutor;
    }

    public static JSLibraryManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (JSLibraryManager.class) {
                if (sInstance == null) {
                    sInstance = new JSLibraryManager(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * @return true if the scripts are loaded. Otherwise, starts reading them from the disk,
     * or downloading them if they are not there, and returns false.
     */
    public boolean checkIfScriptsDownloadedAndStartDownloadingIfNot() {
        if (areScriptsLoaded()) {
            return true;
        }

        runOnDiskThread(() -> {
            if (areScriptsLoaded()) {
                return;
            }

            if (scriptsDownloader.areScriptsDownloadedAlready()) {
                readScripts();
                return;
            }

            scriptsDownloader.downloadScripts(
                    (path) -> new JsScriptsDownloader.ScriptDownloadListener(path, scriptsDownloader.storage)
            );
        });
        return false;
    }

    public void startScriptReadingTask() {
        if (areScriptsLoaded()) {
            return;
        }

        runOnDiskThread(() -> {
            if (!areScriptsLoaded() && scriptsDownloader.areScriptsDownloadedAlready()) {
                readScripts();
            }
        });
    }

    public String getMRAIDScript() {
        return MRAIDscript;
    }

    public String getOMSDKScript() {
        return OMSDKscirpt;
    }

    private boolean areScriptsLoaded() {
        return !OMSDKscirpt.isEmpty() && !MRAIDscript.isEmpty();
    }

    private void readScripts() {
        String openMeasurementScript = scriptsDownloader.readFile(JsScriptData.openMeasurementData);
        String mraidScript = scriptsDownloader.readFile(JsScriptData.mraidData);

        OMSDKscirpt = openMeasurementScript;
        MRAIDscript = mraidScript;
    }

    private void runOnDiskThread(Runnable task) {
        diskExecutor.execute(() -> {
            try {
                task.run();
            } catch (Throwable throwable) {
                LogUtil.error(TAG, "Can't load JS scripts", throwable);
            }
        });
    }

    private static Executor createDiskExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

}
