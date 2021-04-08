package org.prebid.mobile.rendering.loading;

import org.prebid.mobile.rendering.networking.BaseResponseHandler;

public interface FileDownloadListener extends BaseResponseHandler {
    void onFileDownloaded(String path);

    void onFileDownloadError(String error);
}
