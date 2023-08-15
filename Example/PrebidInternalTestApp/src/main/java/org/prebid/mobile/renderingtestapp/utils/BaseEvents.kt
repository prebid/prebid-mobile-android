package org.prebid.mobile.renderingtestapp.utils

import android.view.View
import androidx.annotation.IdRes
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView
import java.lang.ref.WeakReference

abstract class BaseEvents(parentView: View) {

    private val parentViewReference = WeakReference(parentView)

    protected fun enable(@IdRes idRes: Int, value: Boolean) {
        val view = parentViewReference.get() ?: return
        val event = view.findViewById<EventCounterView>(idRes)
        event?.isEnabled = value
    }

}