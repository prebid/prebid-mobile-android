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

package org.prebid.mobile.rendering.loading;

import android.util.Log;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.errors.ServerWrongStatusCode;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * More of a general task for downloading files w/o feedback on progress
 */
public class FileDownloadTask extends BaseNetworkTask {

    private static final String TAG = "LibraryDownloadTask";
    protected FileDownloadListener listener;
    protected File file;

    private boolean ignoreContentLength = false;

    /**
     * Creates a network object
     *
     * @param handler instance of a class handling ad server responses (like , InterstitialSwitchActivity)
     */
    public FileDownloadTask(
            FileDownloadListener handler,
            File file
    ) {
        super(handler);
        if (file == null) {
            String nullFileMessage = "File is null";
            if (handler != null) {
                handler.onFileDownloadError(nullFileMessage);
            }
            throw new NullPointerException(nullFileMessage);
        }
        this.file = file;
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                String errorCreatingFile = "Error creating file";
                handler.onFileDownloadError(errorCreatingFile);
                throw new IllegalStateException(errorCreatingFile);
            }
        }
        listener = handler;
    }

    protected long getMaxFileSize() {
        return 25 * 1024 * 1024; // 25 MiB
    }

    @Override
    public GetUrlResult customParser(int code, URLConnection urlConnection) {
        GetUrlResult result = new GetUrlResult();
        if (code != HttpURLConnection.HTTP_OK) {
            ServerWrongStatusCode wrongCode = new ServerWrongStatusCode(code);
            result.setException(wrongCode);
            return result;
        }
        try {
            if (!ignoreContentLength) {
                int contentLength = urlConnection.getContentLength();
                if (contentLength > getMaxFileSize()) {
                    result.setException(new Exception("FileDownloader encountered a file larger than SDK cap of " + getMaxFileSize()));
                    return result;
                }
                if (contentLength <= 0) {
                    result.setException(new Exception("FileDownloader encountered file with " + contentLength + " content length"));
                    return result;
                }
            }
            processData(urlConnection, result);
        }
        catch (IOException e) {
            LogUtil.error(TAG, "download of media failed: " + Log.getStackTraceString(e));
            result.setException(new Exception("download of media failed " + e.getMessage()));
        }
        finally {
            if (urlConnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlConnection).disconnect();
            }
        }
        return result;
    }

    protected void processData(URLConnection connection, GetUrlResult result) throws IOException {
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            inputStream = connection.getInputStream();
            byte[] data = new byte[16384];
            int count;
            while ((count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Override
    protected void onPostExecute(GetUrlResult urlResult) {
        if (urlResult.getException() != null) {
            LogUtil.debug(TAG, "download of media failed" + urlResult.getException());
            if (listener != null) {
                listener.onFileDownloadError((urlResult.getException().getMessage()));
            }
            return;
        }
        if (listener != null) {
            String path = file.getPath();
            int beginIndex = path.lastIndexOf("/");
            listener.onFileDownloaded(beginIndex != -1 ? path.substring(beginIndex) : path);
        }
    }

    public void setIgnoreContentLength(boolean value) {
        ignoreContentLength = value;
    }

}
