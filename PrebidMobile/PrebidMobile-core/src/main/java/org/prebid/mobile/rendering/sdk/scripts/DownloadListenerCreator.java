package org.prebid.mobile.rendering.sdk.scripts;

import org.prebid.mobile.rendering.loading.FileDownloadListener;

public interface DownloadListenerCreator {

    FileDownloadListener create(String filePath);

}