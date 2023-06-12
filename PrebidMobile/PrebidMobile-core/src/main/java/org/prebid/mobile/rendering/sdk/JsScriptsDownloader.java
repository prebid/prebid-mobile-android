package org.prebid.mobile.rendering.sdk;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.loading.FileDownloadListener;
import org.prebid.mobile.rendering.sdk.scripts.DownloadListenerCreator;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptData;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptRequester;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptRequesterImpl;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptStorage;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptStorageImpl;

import java.io.File;

/**
 * Downloader for JS scripts needed for the Prebid SDK (omsdk.js, mraid.js).
 */
public class JsScriptsDownloader {

    public static void runDownloading(Context context) {
        JsScriptStorageImpl storage = new JsScriptStorageImpl(context);
        JsScriptRequesterImpl downloader = new JsScriptRequesterImpl();

        JsScriptsDownloader jsScriptsDownloader = new JsScriptsDownloader(storage, downloader);
        jsScriptsDownloader.downloadScripts((path) -> new ScriptDownloadListener(path, storage));
    }


    private final static String TAG = "JsScriptsDownloader";

    private final JsScriptStorage storage;
    private final JsScriptRequester downloader;

    @VisibleForTesting
    public JsScriptsDownloader(JsScriptStorage storage, JsScriptRequester downloader) {
        this.storage = storage;
        this.downloader = downloader;
    }


    public void downloadScripts(DownloadListenerCreator listener) {
        try {
            downloadFile(JsScriptData.openMeasurementData, listener);
            downloadFile(JsScriptData.mraidData, listener);
        } catch (Throwable throwable) {
            LogUtil.error(TAG, "Can't download scripts", throwable);
        }
    }

    private void downloadFile(JsScriptData jsScriptData, DownloadListenerCreator listener) {
        File file = storage.getInnerFile(jsScriptData.getPath());

        if (storage.isFileAlreadyDownloaded(file, jsScriptData.getPath())) {
            return;
        }

        storage.createParentFolders(file);
        downloader.download(file, jsScriptData, listener);
    }

    public static class ScriptDownloadListener implements FileDownloadListener {

        private String path;
        private JsScriptStorage storage;

        public ScriptDownloadListener(String path, JsScriptStorage storage) {
            this.path = path;
            this.storage = storage;
        }

        @Override
        public void onFileDownloaded(String string) {
            LogUtil.info(TAG, "JS scripts saved: " + path);
            storage.markFileAsDownloadedCompletely(this.path);
        }

        @Override
        public void onFileDownloadError(String error) {
            LogUtil.error(TAG, "Can't download script " + path + "(" + error + ")");
            storage.fileDownloadingFailed(path);
        }
    }

}
