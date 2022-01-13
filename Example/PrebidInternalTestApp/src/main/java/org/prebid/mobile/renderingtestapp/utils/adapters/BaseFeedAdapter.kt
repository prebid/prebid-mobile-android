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

package org.prebid.mobile.renderingtestapp.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.TextView
import org.prebid.mobile.rendering.views.webview.mraid.Views
import org.prebid.mobile.renderingtestapp.R

private const val TAG = "FeedAdapter"

abstract class BaseFeedAdapter(context: Context) : BaseAdapter() {

    protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val data = MutableList(10) { it }
    private var adView: View? = null

    abstract fun destroy()

    protected abstract fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View?

    override fun getCount() = Integer.MAX_VALUE

    override fun getItem(position: Int) = data[position % data.size]

    override fun getItemId(position: Int) = getItem(position).toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var temp = convertView
        if (temp == null) {
            temp = layoutInflater.inflate(R.layout.item_feed, parent, false)
        }
        val container = (temp as FrameLayout)
        val textView = container.getChildAt(0) as TextView
        textView.text = container.context.getString(R.string.app_name)
        textView.visibility = View.GONE

        if (position % 5 == 0) {
            adView = initAndLoadAdView(parent, container) ?: return container
            Views.removeFromParent(adView)
            container.addView(adView)
        }
        else {
            textView.visibility = View.VISIBLE
            if (container.childCount > 1) {
                container.removeViewAt(1)
            }
        }
        return container
    }
}