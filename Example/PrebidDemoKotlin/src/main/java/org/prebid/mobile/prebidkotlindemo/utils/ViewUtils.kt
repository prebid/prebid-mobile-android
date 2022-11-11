package org.prebid.mobile.prebidkotlindemo.utils

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import org.prebid.mobile.prebidkotlindemo.R

object ViewUtils {

    fun setActionBarTitle(text: String, activity: AppCompatActivity) {
        activity.supportActionBar?.title = HtmlCompat.fromHtml(
            "<font color=\"#ffffff\">$text</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        );
    }

    fun setTestCaseName(text: String, activity: AppCompatActivity) {
        activity.findViewById<TextView?>(R.id.tvTestCaseName)?.text = text
    }

}