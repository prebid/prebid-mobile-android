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

import android.content.Context;
import android.util.LruCache;
import androidx.annotation.NonNull;
import org.prebid.mobile.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class LruController {
    private final static String TAG = LruController.class.getSimpleName();

    private final static char EXTENSION_SEPARATOR = '.';
    private final static String CACHE_POSTFIX = "_cache";

    private static final int MAX_VIDEO_ENTRIES = 30;
    private final static LruCache<String, byte[]> lruCache = new LruCache<>(MAX_VIDEO_ENTRIES);

    public static void putVideoCache(String videoPath, byte[] data) {
        if (getVideoCache(videoPath) == null) {
            lruCache.put(videoPath, data);
        }
    }

    public static byte[] getVideoCache(String videoPath) {
        return lruCache.get(videoPath);
    }

    public static boolean isAlreadyCached(String videoPath) {
        return lruCache.get(videoPath) != null;
    }

    public static boolean saveCacheToFile(
        @NonNull
            Context context,
        @NonNull
            String videoPath) {

        File file = new File(context.getFilesDir(), videoPath);
        if (!file.exists() && getVideoCache(videoPath) != null) {
            try {
                byte[] data = getVideoCache(videoPath);
                OutputStream os = new FileOutputStream(file);
                os.write(data);
                os.close();
                lruCache.remove(videoPath);
                LogUtil.debug(TAG, "Cache saved to file");
                return true;
            }

            catch (Exception e) {
                LogUtil.error(TAG, "Failed to save cache to file: " + e.getMessage());
            }
        }
        return false;
    }

    public static String getShortenedPath(String url) {
        String shortenedPath = url.substring(url.lastIndexOf("/"));
        StringBuilder builder = new StringBuilder();

        int extensionIndex = shortenedPath.lastIndexOf(EXTENSION_SEPARATOR);
        if (extensionIndex != -1) {
            builder.append(shortenedPath.substring(0, extensionIndex));
        }
        else {
            builder.append(shortenedPath);
        }

        return builder.toString();
    }
}
