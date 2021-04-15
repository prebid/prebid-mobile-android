/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.renderingtestapp.utils

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
