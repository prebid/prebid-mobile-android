package org.prebid.mobile.javademo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.widget.ImageView;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.tasksmanager.TasksManager;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class DownloadImageTask {

    WeakReference<ImageView> imageRef;

    public DownloadImageTask(ImageView image) {
        this.imageRef = new WeakReference<>(image);
    }

    public void execute(final String url) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            TasksManager.getInstance().executeOnBackgroundThread(() -> fetchAndProcessImage(url));
        } else {
            fetchAndProcessImage(url);
        }
    }

    private void fetchAndProcessImage(String url) {
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            LogUtil.error("Error", e.getMessage());
        }
        processImage(bitmap);
    }

    private void processImage(final Bitmap result) {
        TasksManager.getInstance().executeOnMainThread(() -> {
            ImageView image = imageRef.get();
            if (image != null) {
                image.setImageBitmap(result);
            }
        });
    }

}
