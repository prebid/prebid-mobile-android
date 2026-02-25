package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingBannerRecyclerViewBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents

open class PpmBannerRecyclerViewFragment : AdFragment() {

    companion object {
        const val TYPE_PLACEHOLDER = 1
        const val TYPE_BANNER = 2
    }

    override val layoutRes = R.layout.fragment_bidding_banner_recycler_view

    protected val binding: FragmentBiddingBannerRecyclerViewBinding
        get() = getBinding()

    protected val events by lazy { Events(binding.root) }


    protected var bannerView: BannerView? = null
    private var adapter: FeedAdapter? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        binding.adIdLabel.text = getString(R.string.label_auid, configId)

        val recyclerView = binding.recyclerView
        adapter = FeedAdapter(
            items = buildList {
                repeat(3) { add(FeedItem.Placeholder) }
                add(FeedItem.Banner)
                repeat(6) { add(FeedItem.Placeholder) }
                add(FeedItem.Banner)
                repeat(1) { add(FeedItem.Placeholder) }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(false)
    }

    override fun initAd() = null

    override fun loadAd() {}

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerView?.destroy()
    }


    private fun createBannerView(): BannerView {
        Log.d(TAG, "Creating new banner view")
        val bannerView = BannerView(activity, "prebid-demo-banner-320-50", AdSize(320, 50))
        bannerView.setAutoRefreshDelay(30)
        bannerView.setBannerListener(createListener())
        return bannerView
    }

    private fun createListener(): BannerViewListener {
        return object : BannerViewListener {
            override fun onAdLoaded(bannerView: BannerView?) {
                resetEventButtons()
                events.loaded(true)
            }

            override fun onAdDisplayed(bannerView: BannerView?) {
                events.displayed(true)
            }

            override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
                resetEventButtons()
                events.failed(true)
            }

            override fun onAdClicked(bannerView: BannerView?) {}

            override fun onAdClosed(bannerView: BannerView?) {}
        }
    }


    private inner class FeedAdapter(
        private val items: List<FeedItem>,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int = when (items[position]) {
            is FeedItem.Placeholder -> TYPE_PLACEHOLDER
            is FeedItem.Banner -> TYPE_BANNER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                TYPE_PLACEHOLDER -> {
                    PlaceholderVH(View(parent.context).apply {
                        layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(parent, 500))
                        setBackgroundColor("#e0e0e0".toColorInt())
                    })
                }

                TYPE_BANNER -> {
                    BannerVH(FrameLayout(parent.context).apply {
                        layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(parent, 50))
                        setBackgroundColor(Color.WHITE)
                    })
                }

                else -> error("Unknown viewType=$viewType")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (items[position]) {
                is FeedItem.Placeholder -> Unit
                is FeedItem.Banner -> (holder as BannerVH).bind()
            }
        }

        override fun getItemCount(): Int = items.size

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            if (holder is BannerVH) {
                holder.unbind()
            }
            super.onViewRecycled(holder)
        }

        private inner class PlaceholderVH(view: View) : RecyclerView.ViewHolder(view)

        private inner class BannerVH(private val container: FrameLayout) : RecyclerView.ViewHolder(container) {

            fun bind() {
                val adView = bannerView ?: createBannerView()
                if (bannerView == null) {
                    bannerView = adView
                    adView.loadAd()
                }
                if (adView.parent !== container) {
                    Log.d(TAG, "New parent container")
                    (adView.parent as? ViewGroup)?.removeView(adView)
                    container.removeAllViews()
                    container.addView(
                        adView,
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    )
                }
            }

            fun unbind() {
                Log.d(TAG, "Unbinding ad view")
                container.removeAllViews()
            }
        }

        private fun dp(parent: ViewGroup, valueDp: Int): Int =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                valueDp.toFloat(),
                parent.resources.displayMetrics
            ).toInt()

    }

    protected class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun displayed(b: Boolean) = enable(R.id.btnAdDisplayed, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)

    }

    private sealed interface FeedItem {
        data object Placeholder : FeedItem
        data object Banner : FeedItem
    }

}