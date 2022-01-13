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

package org.prebid.mobile.rendering.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import org.prebid.mobile.rendering.loading.FileDownloadListener;
import org.prebid.mobile.rendering.loading.FileDownloadTask;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@SuppressLint("StaticFieldLeak")
public class VideoDownloadTask extends FileDownloadTask {

    private static final String TAG = VideoDownloadTask.class.getSimpleName();
    private Context mApplicationContext;
    private AdConfiguration mAdConfiguration;

    public VideoDownloadTask(Context context, File file, FileDownloadListener fileDownloadListener,
                             AdConfiguration adConfiguration) {
        super(fileDownloadListener, file);
        if (context == null) {
            String contextIsNull = "Context is null";
            fileDownloadListener.onFileDownloadError(contextIsNull);
            throw new NullPointerException(contextIsNull);
        }
        mAdConfiguration = adConfiguration;
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public GetUrlResult sendRequest(GetUrlParams param) throws Exception {
        LogUtil.debug(TAG, "url: " + param.url);
        LogUtil.debug(TAG, "queryParams: " + param.queryParams);

        return createResult(param);
    }

    private String getShortenedPath() {
        String path = mFile.getPath();
        int beginIndex = path.lastIndexOf("/");
        return beginIndex != -1 ? path.substring(beginIndex) : path;
    }

    @Override
    protected void processData(URLConnection connection, GetUrlResult result) throws IOException {
        String shortenedPath = getShortenedPath();
        if (mFile.exists() && !LruController.isAlreadyCached(shortenedPath)) {
            LogUtil.debug(TAG, "Video saved to cache");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            readAndWriteData(connection, result, outputStream, false);
            LruController.putVideoCache(shortenedPath, outputStream.toByteArray());
        }
        else {
            LogUtil.debug(TAG, "Video saved to file: " + shortenedPath);
            readAndWriteData(connection, result, new FileOutputStream(mFile), true);
        }
    }

    private void readAndWriteData(URLConnection in, GetUrlResult result, OutputStream out,
                                  boolean deleteOnAbort) throws IOException {
        int length = in.getContentLength();
        InputStream is = in.getInputStream();
        byte[] data = new byte[16384];
        long total = 0;
        int count;
        try {
            while ((count = is.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    if (deleteOnAbort) {
                        if (mFile.exists()) {
                            mFile.delete();
                        }
                    }
                    result.setException(null);
                    return;
                }
                total += count;
                // publishing the progress....
                if (length > 0) // only if total length is known
                {
                    publishProgress((int) (total * 100 / length));
                }
                out.write(data, 0, count);
            }
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            try {

                if (is != null) {
                    is.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            catch (Exception ignored) {
            }
        }
    }

    private GetUrlResult createResult(GetUrlParams param)
    throws Exception {
        mResult = new GetUrlResult();
        String shortenedPath = getShortenedPath();
        if (mFile.exists()) {
            LogUtil.debug(TAG, "File exists: " + shortenedPath);
            if (isVideoFileExpired(mFile) || !isVideoFileValid(mApplicationContext, mFile)) {
                LogUtil.debug(TAG, "File " + shortenedPath + " is expired or broken. Downloading a new one");
                mFile.delete();
                mResult = super.sendRequest(param);
            }
            else if (!LruController.isAlreadyCached(shortenedPath)) {
                mResult = super.sendRequest(param);
            }
        }
        else {
            mResult = super.sendRequest(param);
        }
        return mResult;
    }

    private boolean isVideoFileExpired(File file) {
        Date lastModDate = new Date(file.lastModified());
        Date currentDate = Calendar.getInstance().getTime();
        long diff = currentDate.getTime() - lastModDate.getTime();
        return diff > TimeUnit.HOURS.toMillis(1);
    }

    private boolean isVideoFileValid(Context context, File file) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.fromFile(file));
            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
            return hasVideo.equals("yes");
        }
        catch (Exception e) {
            return false;
        }
    }
}