package com.openx.internal_test_app.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import segmented_control.widget.custom.android.com.segmentedcontrol.custom_segment.SegmentAdapterImpl
import segmented_control.widget.custom.android.com.segmentedcontrol.custom_segment.SegmentViewHolderImpl

class SegmentAdapterReImpl : SegmentAdapterImpl() {

    override fun onCreateViewHolder(
            layoutInflater: LayoutInflater, viewGroup: ViewGroup?, i: Int): SegmentViewHolderImpl {
        val segmentViewHolder = super.onCreateViewHolder(layoutInflater, viewGroup, i)
        return AppSegmentViewHolder(segmentViewHolder.sectionView)
    }
}
