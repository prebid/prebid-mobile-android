package org.prebid.mobile.renderingtestapp.utils

import android.view.View
import androidx.annotation.IdRes
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

abstract class BaseEvents(private val parentView: View) {

    protected fun enable(@IdRes idRes: Int, value: Boolean) {
        val event = parentView.findViewById<EventCounterView>(idRes)
        event?.isEnabled = value
    }

}