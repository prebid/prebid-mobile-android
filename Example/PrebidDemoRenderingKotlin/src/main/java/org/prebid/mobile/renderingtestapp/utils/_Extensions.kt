package org.prebid.mobile.renderingtestapp.utils

import android.widget.ImageView
import com.bumptech.glide.Glide

fun String.getAdDescription(refreshIntervalSec: Int, impressionCount: Int): String {
    return "Config ID: $this\n" +
            "Refresh Interval: $refreshIntervalSec sec\n" +
            "Impressions Count: $impressionCount"
}

fun loadImage(view: ImageView, url: String) {
    Glide.with(view.context)
            .load(url)
            .into(view)
}