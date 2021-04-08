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