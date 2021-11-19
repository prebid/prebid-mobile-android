package org.prebid.mobile.prebidkotlindemo

import android.app.Activity
import android.view.ViewGroup

data class AdType(
    var name: String,
    var onCreate: (activity: Activity, wrapper: ViewGroup, autoRefreshTime: Int) -> Unit,
    var onDestroy: (() -> Unit)? = null
)
