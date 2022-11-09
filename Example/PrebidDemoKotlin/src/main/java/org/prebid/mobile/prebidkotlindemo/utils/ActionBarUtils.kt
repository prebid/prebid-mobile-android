package org.prebid.mobile.prebidkotlindemo.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

object ActionBarUtils {

    fun setTitle(text: String, activity: AppCompatActivity) {
        activity.supportActionBar?.title = HtmlCompat.fromHtml(
            "<font color=\"#ffffff\">$text</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

}