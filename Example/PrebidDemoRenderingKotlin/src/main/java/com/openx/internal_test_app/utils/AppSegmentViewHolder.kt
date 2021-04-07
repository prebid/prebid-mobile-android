package com.openx.internal_test_app.utils

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import segmented_control.widget.custom.android.com.segmentedcontrol.custom_segment.SegmentViewHolderImpl

class AppSegmentViewHolder(
        sectionView: View) : SegmentViewHolderImpl(sectionView) {
    init {
        if (sectionView is ViewGroup) {
            for (i in 0 until sectionView.childCount) {
                val childAt = sectionView.getChildAt(i)
                if (childAt is TextView) {
                    childAt.ellipsize = TextUtils.TruncateAt.END
                    childAt.maxLines = 1
                }
            }
        }
    }
}
