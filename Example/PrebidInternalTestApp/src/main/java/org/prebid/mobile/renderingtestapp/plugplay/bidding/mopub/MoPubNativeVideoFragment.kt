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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import android.util.Log
import com.mopub.mediation.MoPubMediationUtils
import com.mopub.nativeads.*
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.events_native_video.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import kotlinx.android.synthetic.main.video_controls.*
import org.prebid.mobile.rendering.bidding.data.ntv.MediaView
import org.prebid.mobile.rendering.bidding.display.MediationNativeAdUnit
import org.prebid.mobile.rendering.errors.AdException
import org.prebid.mobile.rendering.listeners.MediaViewListener
import org.prebid.mobile.renderingtestapp.R

class MoPubNativeVideoFragment : MopubNativeFragment() {
    private val TAG = MoPubNativeVideoFragment::class.java.simpleName

    override val layoutRes: Int = R.layout.fragment_mopub_native_video
    private var mediaView: MediaView? = null

    override var nativeNetworkListener = object : MoPubNative.MoPubNativeNetworkListener {
        override fun onNativeLoad(nativeAd: NativeAd?) {
            btnLoad?.isEnabled = true
            btnAdLoaded?.isEnabled = true
            nativeAd?.setMoPubNativeEventListener(nativeEventListener)
            this@MoPubNativeVideoFragment.nativeAd = nativeAd
            val view = adapterHelper.getAdView(null, viewContainer, nativeAd)
            mediaView = view.findViewById(R.id.mediaView)

            btnPlay?.setOnClickListener { mediaView?.play() }
            btnPause?.setOnClickListener { mediaView?.pause() }
            btnResume?.setOnClickListener { mediaView?.resume() }
            btnMute?.setOnClickListener { mediaView?.mute() }
            btnUnMute?.setOnClickListener { mediaView?.unMute() }

            viewContainer.removeAllViews()
            viewContainer.addView(view)
        }

        override fun onNativeFail(errorCode: NativeErrorCode?) {
            btnAdFailed?.isEnabled = true
        }

    }

    private val mediaViewListener = object : MediaViewListener {
        override fun onMediaPlaybackStarted(mediaView: MediaView?) {
            Log.d(TAG, "onMediaPlaybackStarted() called with: mediaView = $mediaView")
            btnPlaybackStarted?.isEnabled = true
        }

        override fun onMediaPlaybackFinished(mediaView: MediaView?) {
            Log.d(TAG, "onMediaPlaybackFinished() called with: mediaView = $mediaView")
            btnPlaybackFinished?.isEnabled = true
        }

        override fun onMediaPlaybackPaused(mediaView: MediaView?) {
            Log.d(TAG, "onMediaPlaybackPaused() called with: mediaView = $mediaView")
            btnPlaybackPaused?.isEnabled = true
        }

        override fun onMediaPlaybackResumed(mediaView: MediaView?) {
            Log.d(TAG, "onMediaPlaybackResumed() called with: mediaView = $mediaView")
            btnPlaybackResumed?.isEnabled = true
        }

        override fun onMediaPlaybackMuted(mediaView: MediaView?) {
            Log.d(TAG, "onMediaPlaybackMuted() called with: mediaView = $mediaView")
            btnPlaybackMuted?.isEnabled = true
        }

        override fun onMediaPlaybackUnMuted(mediaView: MediaView?) {
            Log.d(TAG, "onMediaPlaybackUnMuted() called with: mediaView = $mediaView")
            btnPlaybackUnMuted?.isEnabled = true
        }

        override fun onVideoLoadingFinished(mediaView: MediaView?) {
            Log.d(TAG, "onVideoLoadingFinished() called with: mediaView = $mediaView")
            btnVideoLoadingFinished?.isEnabled = true
        }

        override fun onFailure(adException: AdException?) {
            Log.d(TAG, "onFailure() called with: adException = $adException")
            btnFailure?.isEnabled = true
        }
    }

    override fun initAd() {
        adapterHelper = AdapterHelper(requireContext(), 0, 3);
        mopubNative = MoPubNative(requireContext(), adUnitId, nativeNetworkListener)
        val viewBinder = ViewBinder.Builder(R.layout.lyt_native_ad_video).build()
        val addRenderer = PrebidNativeAdRenderer(viewBinder)
        addRenderer.setMediaViewResId(R.id.mediaView)
        addRenderer.setMediaViewListener(mediaViewListener)

        mopubNative?.registerAdRenderer(addRenderer)

        mopubNativeAdUnit = MediationNativeAdUnit(
            requireContext(),
            configId,
            getNativeAdConfig(),
            MoPubMediationUtils()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaView?.destroy()
    }
}