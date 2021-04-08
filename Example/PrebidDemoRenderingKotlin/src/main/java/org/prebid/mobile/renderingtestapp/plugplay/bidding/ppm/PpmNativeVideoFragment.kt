package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.events_native_video.*
import kotlinx.android.synthetic.main.fragment_native_video.*
import kotlinx.android.synthetic.main.video_controls.*
import org.prebid.mobile.rendering.bidding.data.ntv.MediaView
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd
import org.prebid.mobile.rendering.errors.AdException
import org.prebid.mobile.rendering.listeners.MediaViewListener
import org.prebid.mobile.renderingtestapp.R


class PpmNativeVideoFragment : PpmNativeFragment() {
    private val TAG = PpmNativeFragment::class.java.simpleName

    override val layoutRes: Int = R.layout.fragment_native_video

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaView?.setMediaViewListener(mediaViewListener)

        btnPlay?.setOnClickListener { mediaView?.play() }
        btnPause?.setOnClickListener { mediaView?.pause() }
        btnResume?.setOnClickListener { mediaView?.resume() }
        btnMute?.setOnClickListener { mediaView?.mute() }
        btnUnMute?.setOnClickListener { mediaView?.unMute() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaView?.destroy()
    }

    override fun inflateViewContent(nativeAd: NativeAd?) {
        this.nativeAd = nativeAd
        val mediaData = nativeAd?.nativeVideoAd?.mediaData

        if (mediaData == null) {
            Log.e(TAG, "inflateViewContent: failed. Media data is null.")
            return
        }
        nativeAd.setNativeAdListener(this)

        nativeAd.registerView(mediaView, mediaView)
        mediaView?.loadMedia(mediaData)
    }
}