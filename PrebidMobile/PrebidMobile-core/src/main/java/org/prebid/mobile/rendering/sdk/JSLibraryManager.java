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

import org.prebid.mobile.rendering.sdk.scripts.JsScriptData;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Downloader and fetcher for JS scripts needed for the Prebid SDK (omsdk.js, mraid.js).
 * Top level class for working with JS scripts.
 */
public class JSLibraryManager {

    private static JSLibraryManager sInstance;

    private String MRAIDscript = "";
    private String OMSDKscirpt = "";
    private JsScriptsDownloader scriptsDownloader;

    private JSLibraryManager(Context context) {
        this.scriptsDownloader = JsScriptsDownloader.createDownloader(context);
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

    public boolean checkIfScriptsDownloadedAndStartDownloadingIfNot() {
        if (scriptsDownloader.areScriptsDownloadedAlready()) {
            if (!OMSDKscirpt.isEmpty() && !MRAIDscript.isEmpty()) {
                return true;
            }

            startScriptReadingTask();
            return false;
        }

        scriptsDownloader.downloadScripts(
                (path) -> new JsScriptsDownloader.ScriptDownloadListener(path, scriptsDownloader.storage)
        );
        return false;
    }

    public void startScriptReadingTask() {
        if (scriptsDownloader.areScriptsDownloadedAlready()) {
            if (OMSDKscirpt.isEmpty() || MRAIDscript.isEmpty()) {

                boolean isNotRunning = BackgroundScriptReader.alreadyRunning.compareAndSet(false, true);
                if (isNotRunning) {
                    Thread thread = new Thread(new BackgroundScriptReader(scriptsDownloader, this));
                    thread.start();
                }
            }
        }
    }

    public String getMRAIDScript() {
        return MRAIDscript;
    }

    public String getOMSDKScript() {
        return OMSDKscirpt;
    }

    private static class BackgroundScriptReader implements Runnable {

        private static final AtomicBoolean alreadyRunning = new AtomicBoolean(false);

        private final JsScriptsDownloader scriptsDownloader;
        private final JSLibraryManager jsLibraryManager;

        public BackgroundScriptReader(
                JsScriptsDownloader scriptsDownloader,
                JSLibraryManager jsLibraryManager
        ) {
            this.scriptsDownloader = scriptsDownloader;
            this.jsLibraryManager = jsLibraryManager;
        }

        @Override
        public void run() {
            String openMeasurementScript = scriptsDownloader.readFile(JsScriptData.openMeasurementData);
            String mraidScript = scriptsDownloader.readFile(JsScriptData.mraidData);

            jsLibraryManager.OMSDKscirpt = openMeasurementScript;
            jsLibraryManager.MRAIDscript = mraidScript;
            alreadyRunning.set(false);
        }

    }

}
