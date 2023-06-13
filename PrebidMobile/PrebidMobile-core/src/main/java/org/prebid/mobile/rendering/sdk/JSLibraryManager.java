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

/**
 * Downloads and provides JS scripts in the SDK.
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
            initScriptVariables();
            return true;
        }

        scriptsDownloader.downloadScripts(
                (path) -> new JsScriptsDownloader.ScriptDownloadListener(path, scriptsDownloader.storage)
        );
        return false;
    }

    public void initScriptVariables() {
        if (scriptsDownloader.areScriptsDownloadedAlready()) {
            if (OMSDKscirpt.isEmpty()) {
                OMSDKscirpt = scriptsDownloader.readFile(JsScriptData.openMeasurementData);
            }
            if (MRAIDscript.isEmpty()) {
                MRAIDscript = scriptsDownloader.readFile(JsScriptData.mraidData);
            }
        }
    }

    public String getMRAIDScript() {
        return MRAIDscript;
    }

    public String getOMSDKScript() {
        return OMSDKscirpt;
    }

}
