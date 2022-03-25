package org.prebid.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.widget.ImageView;
import org.prebid.mobile.tasksmanager.TasksManager;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class DownloadImageTask {

    WeakReference<ImageView> imageRef;

    protected DownloadImageTask(ImageView image) {
        this.imageRef = new WeakReference<>(image);
    }

    protected void execute(final String url) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            TasksManager.getInstance().executeOnBackgroundThread(new Runnable() {
                @Override
                public void run() {
                    fetchAndProcessImage(url);
                }
            });
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
        TasksManager.getInstance().executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                ImageView image = imageRef.get();
                if (image != null) {
                    image.setImageBitmap(result);
                }
            }
        });
    }
}
