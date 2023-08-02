package org.prebid.mobile.rendering.sdk.scripts;

import java.io.File;

public interface JsScriptStorage {

    public File getInnerFile(String path);

    public boolean isFileAlreadyDownloaded(File file, String preferencesKey);

    public void createParentFolders(File file);

    public void markFileAsDownloadedCompletely(String path);

    public void fileDownloadingFailed(String path);

}
