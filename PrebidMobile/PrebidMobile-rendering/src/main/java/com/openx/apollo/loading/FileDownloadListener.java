package com.openx.apollo.loading;

import com.openx.apollo.networking.BaseResponseHandler;

public interface FileDownloadListener extends BaseResponseHandler {
    void onFileDownloaded(String path);

    void onFileDownloadError(String error);
}
