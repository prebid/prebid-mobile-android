package org.prebid.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    WeakReference<ImageView> imageRef;

    public DownloadImageTask(ImageView image) {
        this.imageRef = new WeakReference<>(image);
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            LogUtil.e("Error", e.getMessage());
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        ImageView image = this.imageRef.get();
        if (image != null) {
            image.setImageBitmap(result);
        }
    }
}
