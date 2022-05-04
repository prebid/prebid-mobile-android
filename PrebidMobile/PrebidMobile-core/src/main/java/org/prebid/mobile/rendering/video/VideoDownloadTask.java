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
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.loading.FileDownloadListener;
import org.prebid.mobile.rendering.loading.FileDownloadTask;

import java.io.*;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@SuppressLint("StaticFieldLeak")
public class VideoDownloadTask extends FileDownloadTask {

    private static final String TAG = VideoDownloadTask.class.getSimpleName();
    private Context applicationContext;
    private AdUnitConfiguration adConfiguration;

    public VideoDownloadTask(
            Context context,
            File file,
            FileDownloadListener fileDownloadListener,
            AdUnitConfiguration adConfiguration
    ) {
        super(fileDownloadListener, file);
        if (context == null) {
            String contextIsNull = "Context is null";
            fileDownloadListener.onFileDownloadError(contextIsNull);
            throw new NullPointerException(contextIsNull);
        }
        this.adConfiguration = adConfiguration;
        applicationContext = context.getApplicationContext();
    }

    @Override
    public GetUrlResult sendRequest(GetUrlParams param) throws Exception {
        LogUtil.debug(TAG, "url: " + param.url);
        LogUtil.debug(TAG, "queryParams: " + param.queryParams);

        return createResult(param);
    }

    private String getShortenedPath() {
        String path = file.getPath();
        int beginIndex = path.lastIndexOf("/");
        return beginIndex != -1 ? path.substring(beginIndex) : path;
    }

    @Override
    protected void processData(URLConnection connection, GetUrlResult result) throws IOException {
        String shortenedPath = getShortenedPath();
        if (file.exists() && !LruController.isAlreadyCached(shortenedPath)) {
            LogUtil.debug(TAG, "Video saved to cache");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            readAndWriteData(connection, result, outputStream, false);
            LruController.putVideoCache(shortenedPath, outputStream.toByteArray());
        } else {
            LogUtil.debug(TAG, "Video saved to file: " + shortenedPath);
            readAndWriteData(connection, result, new FileOutputStream(file), true);
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
                        if (file.exists()) {
                            file.delete();
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
        result = new GetUrlResult();
        String shortenedPath = getShortenedPath();
        if (file.exists()) {
            LogUtil.debug(TAG, "File exists: " + shortenedPath);
            if (isVideoFileExpired(file) || !isVideoFileValid(applicationContext, file)) {
                LogUtil.debug(TAG, "File " + shortenedPath + " is expired or broken. Downloading a new one");
                file.delete();
                result = super.sendRequest(param);
            } else if (!LruController.isAlreadyCached(shortenedPath)) {
                result = super.sendRequest(param);
            }
        }
        else {
            result = super.sendRequest(param);
        }
        return result;
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