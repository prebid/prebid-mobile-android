package com.openx.apollo.video;

import android.content.Context;
import android.util.LruCache;

import androidx.annotation.NonNull;

import com.openx.apollo.utils.logger.OXLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class LruController {
    private final static String TAG = LruController.class.getSimpleName();

    private final static char EXTENSION_SEPARATOR = '.';
    private final static String CACHE_POSTFIX = "_cache";

    private static final int MAX_VIDEO_ENTRIES = 30;
    private final static LruCache<String, byte[]> mLruCache = new LruCache<>(MAX_VIDEO_ENTRIES);

    public static void putVideoCache(String videoPath, byte[] data) {
        if (getVideoCache(videoPath) == null) {
            mLruCache.put(videoPath, data);
        }
    }

    public static byte[] getVideoCache(String videoPath) {
        return mLruCache.get(videoPath);
    }

    public static boolean isAlreadyCached(String videoPath) {
        return mLruCache.get(videoPath) != null;
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
                mLruCache.remove(videoPath);
                OXLog.debug(TAG, "Cache saved to file");
                return true;
            }

            catch (Exception e) {
                OXLog.error(TAG, "Failed to save cache to file: " + e.getMessage());
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
