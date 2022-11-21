package org.prebid.mobile.javademo.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageUtils {

    public static void download(String url, ImageView imageView) {
        Glide.with(imageView)
            .load(url)
            .into(imageView);
    }

}
