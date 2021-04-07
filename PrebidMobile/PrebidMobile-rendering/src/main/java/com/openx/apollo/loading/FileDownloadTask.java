package com.openx.apollo.loading;

import android.util.Log;

import com.openx.apollo.errors.ServerWrongStatusCode;
import com.openx.apollo.networking.BaseNetworkTask;
import com.openx.apollo.utils.logger.OXLog;

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
    protected FileDownloadListener mListener;
    protected File mFile;

    /**
     * Creates a network object
     *
     * @param handler instance of a class handling ad server responses (like , InterstitialSwitchActivity)
     */
    public FileDownloadTask(FileDownloadListener handler, File file) {
        super(handler);
        if (file == null) {
            String nullFileMessage = "File is null";
            if (handler != null) {
                handler.onFileDownloadError(nullFileMessage);
            }
            throw new NullPointerException(nullFileMessage);
        }
        mFile = file;
        if (!mFile.exists()) {
            try {
                mFile.createNewFile();
            }
            catch (IOException e) {
                String errorCreatingFile = "Error creating file";
                handler.onFileDownloadError(errorCreatingFile);
                throw new IllegalStateException(errorCreatingFile);
            }
        }
        mListener = handler;
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
            int contentLength = urlConnection.getContentLength();
            if (contentLength > getMaxFileSize()) {
                result.setException(new Exception("FileDownloader encountered a file larger than SDK cap of " + getMaxFileSize()));
                return result;
            }
            if (contentLength <= 0) {
                result.setException(new Exception("FileDownloader encountered file with " + contentLength + " content length"));
                return result;
            }
            processData(urlConnection, result);
        }
        catch (IOException e) {
            OXLog.error(TAG, "download of media failed: " + Log.getStackTraceString(e));
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
            outputStream = new FileOutputStream(mFile);
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
            OXLog.debug(TAG, "download of media failed" + urlResult.getException());
            if (mListener != null) {
                mListener.onFileDownloadError((urlResult.getException().getMessage()));
            }
            return;
        }
        if (mListener != null) {
            String path = mFile.getPath();
            int beginIndex = path.lastIndexOf("/");
            mListener.onFileDownloaded(beginIndex != -1 ? path.substring(beginIndex) : path);
        }
    }
}
