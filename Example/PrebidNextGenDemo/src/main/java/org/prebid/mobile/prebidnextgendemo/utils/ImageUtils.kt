package org.prebid.mobile.prebidnextgendemo.utils

import android.widget.ImageView
import com.bumptech.glide.Glide

object ImageUtils {

    fun download(url: String, imageView: ImageView) {
        Glide.with(imageView)
            .load(url)
            .into(imageView)
    }

}