package org.prebid.mobile.renderingtestapp.plugplay.bidding.testing

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentEmptyBinding
import org.prebid.mobile.renderingtestapp.databinding.FragmentPucTestingGamBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

/**
 * Example for testing memory leaks with original API ad units.
 * It doesn't use Google ad view because it causes another memory leak after loadAd().
 */
open class PrebidUniversalCreativeTestingGamFragment : AdFragment() {

    override val layoutRes = R.layout.fragment_puc_testing_gam

    private lateinit var adView: AdManagerAdView

    private val binding: FragmentPucTestingGamBinding
        get() = getBinding()

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
    }

    override fun initAd(): Any {
        val adView = AdManagerAdView(requireContext()).apply { adView = this }
        adView.adUnitId = "/21808260008/prebid_puc_testing"
        adView.setAdSizes(AdSize(300, 250))
        adView.adListener = createListener()
        adView.loadAd(AdManagerAdRequest.Builder().build())
        binding.container.addView(adView)
        return adView
    }

    private fun createListener(): AdListener = object : AdListener() {

        override fun onAdLoaded() {
            super.onAdLoaded()

            val webView = WebViewSearcher.findIn(adView)
            if (webView == null) {
                Log.e("TESTV", "WebView is null")
                return
            }

            webView.getSettings().mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            Toast.makeText(requireContext(), "Error loading GAM ad: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun loadAd() {}

    internal object WebViewSearcher {

        fun findIn(root: View): WebView? {
            if (root is WebView) return root

            if (root is ViewGroup) return findRecursively(root)

            return null
        }

        private fun findRecursively(root: ViewGroup): WebView? {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                if (child is WebView) return child

                if (child is ViewGroup) {
                    val result = findRecursively(child)
                    if (result != null) return result
                }
            }

            return null
        }

    }

}