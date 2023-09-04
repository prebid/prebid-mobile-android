package org.prebid.mobile.rendering.sdk;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.loading.FileDownloadListener;
import org.prebid.mobile.rendering.sdk.scripts.DownloadListenerCreator;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptData;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptRequester;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptRequesterImpl;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptStorage;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptStorageImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Downloader and fetcher for JS scripts needed for the Prebid SDK (omsdk.js, mraid.js).
 * If you need to work with JS scripts from the SDK use {@link JSLibraryManager},
 * because this class contains internal implementation.
 */
public class JsScriptsDownloader {

    public static JsScriptsDownloader createDownloader(Context context) {
        JsScriptStorageImpl storage = new JsScriptStorageImpl(context);
        JsScriptRequesterImpl downloader = new JsScriptRequesterImpl();

        return new JsScriptsDownloader(storage, downloader);
    }

    private final static String TAG = "JsScriptsDownloader";

    public final JsScriptStorage storage;
    private final JsScriptRequester downloader;
    private final static SortedSet<String> inProgressKeys = Collections.synchronizedSortedSet(new TreeSet<>());


    @VisibleForTesting
    public JsScriptsDownloader(JsScriptStorage storage, JsScriptRequester downloader) {
        this.storage = storage;
        this.downloader = downloader;
    }


    public boolean areScriptsDownloadedAlready() {
        return isFileAlreadyDownloaded(JsScriptData.openMeasurementData) &&
                isFileAlreadyDownloaded(JsScriptData.mraidData);
    }

    public void downloadScripts(DownloadListenerCreator listener) {
        try {
            downloadFile(JsScriptData.openMeasurementData, listener);
            downloadFile(JsScriptData.mraidData, listener);
        } catch (Throwable throwable) {
            LogUtil.error(TAG, "Can't download scripts", throwable);
        }
    }

    @Nullable
    public String readFile(JsScriptData fileData) {
        try {
            File file = storage.getInnerFile(fileData.getPath());
            return convertFileToString(file);
        } catch (Throwable throwable) {
            LogUtil.error(TAG, "Can't read file: " + fileData.getPath());
        }
        return null;
    }


    private static String convertFileToString(File file) throws Exception {
        FileInputStream is = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private void downloadFile(JsScriptData jsScriptData, DownloadListenerCreator listener) {
        boolean keyAddedFirstTime = inProgressKeys.add(jsScriptData.getPath());
        boolean downloadingIsInProgress = !keyAddedFirstTime;
        if (downloadingIsInProgress) return;

        if (isFileAlreadyDownloaded(jsScriptData)) {
            inProgressKeys.remove(jsScriptData.getPath());
            return;
        }

        File file = storage.getInnerFile(jsScriptData.getPath());
        storage.createParentFolders(file);
        downloader.download(file, jsScriptData, listener);
    }

    private boolean isFileAlreadyDownloaded(JsScriptData fileData) {
        File file = storage.getInnerFile(fileData.getPath());

        return storage.isFileAlreadyDownloaded(file, fileData.getPath());
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

            Context context = PrebidContextHolder.getContext();
            if (context != null) {
                JSLibraryManager.getInstance(context).startScriptReadingTask();
            }

            inProgressKeys.remove(path);
        }

        @Override
        public void onFileDownloadError(String error) {
            LogUtil.error(TAG, "Can't download script " + path + "(" + error + ")");
            storage.fileDownloadingFailed(path);
            inProgressKeys.remove(path);
        }
    }

}
