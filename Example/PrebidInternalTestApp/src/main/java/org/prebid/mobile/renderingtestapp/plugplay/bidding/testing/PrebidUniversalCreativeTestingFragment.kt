package org.prebid.mobile.renderingtestapp.plugplay.bidding.testing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentPucTestingBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

/**
 * Example for testing memory leaks with original API ad units.
 * It doesn't use Google ad view because it causes another memory leak after loadAd().
 */
open class PrebidUniversalCreativeTestingFragment : AdFragment() {

    override val layoutRes = R.layout.fragment_puc_testing

    private val binding: FragmentPucTestingBinding
        get() = getBinding()

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER

    @Suppress("HttpUrlsUsage")
    @SuppressLint("SetJavaScriptEnabled")
    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        binding.btnLoadUrl.setOnClickListener {
            val ipAddress = binding.ipAddress.text
            if (ipAddress.isNotBlank()) {
                binding.webViewContainer.removeAllViews()

                val webView = WebView(requireContext())
                webView.settings.javaScriptEnabled = true
                webView.loadUrl("http://$ipAddress")
                binding.webViewContainer.addView(
                    webView,
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                )
            }
        }
    }

    override fun initAd(): Any {
        return ""
    }

    override fun loadAd() {}

}