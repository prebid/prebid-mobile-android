package org.prebid.mobile.rendering.sdk.scripts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.prebid.mobile.LogUtil;

import java.io.File;

public class JsScriptStorageImpl implements JsScriptStorage {

    private final static String TAG = "JsScriptsStorage";

    private final SharedPreferences preferences;
    private final File innerFolder;

    public JsScriptStorageImpl(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        innerFolder = context.getFilesDir();
    }

    public File getInnerFile(String path) {
        return new File(innerFolder, path);
    }

    public boolean isFileAlreadyDownloaded(File file, String preferencesKey) {
        return file.exists() && preferences.contains(preferencesKey);
    }

    public void createParentFolders(File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            boolean foldersCreated = parentFile.mkdirs();
            if (foldersCreated) {
                LogUtil.info(TAG, "Subfolders created");
            }
        }
    }

    public void markFileAsDownloadedCompletely(String path) {
        preferences.edit().putBoolean(path, true).apply();
    }

    public void fileDownloadingFailed(String path) {
        preferences.edit().remove(path).apply();
        removeFile(new File(innerFolder, path));
    }

    private void removeFile(File file) {
        try {
            boolean isFileRemoved = file.delete();
            if (isFileRemoved) {
                LogUtil.info(TAG, "Not fully downloaded file removed.");
            }
        } catch (Throwable ignore) {
        }
    }

}