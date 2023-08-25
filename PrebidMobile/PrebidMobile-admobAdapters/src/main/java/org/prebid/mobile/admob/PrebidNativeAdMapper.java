package org.prebid.mobile.admob;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationNativeAdCallback;
import com.google.android.gms.ads.mediation.MediationNativeAdConfiguration;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;

import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class PrebidNativeAdMapper extends UnifiedNativeAdMapper {

    private final PrebidNativeAd prebidAd;
    @Nullable
    private PrebidNativeAdEventListener prebidListener;

    public PrebidNativeAdMapper(PrebidNativeAd prebidAd) {
        super();
        this.prebidAd = prebidAd;
    }

    @Override
    public void trackViews(@NonNull View view, @NonNull Map<String, View> map, @NonNull Map<String, View> map1) {
        super.trackViews(view, map, map1);
        prebidAd.registerView(view, new ArrayList<>(map.values()), prebidListener);
    }

    @Override
    public void recordImpression() {
        if (prebidListener != null) {
            prebidListener.onAdImpression();
        }
    }

    @Override
    public void handleClick(@NonNull View view) {
        if (prebidListener != null) {
            prebidListener.onAdClicked();
        }
    }

    public void configure(
            MediationNativeAdConfiguration configuration,
            MediationAdLoadCallback<UnifiedNativeAdMapper, MediationNativeAdCallback> adMobLoadListener
    ) {
        ArrayList<NativeAd.Image> images = new ArrayList<>();
        images.add(new PrebidImage(prebidAd.getImageUrl(), configuration.getContext()));
        setImages(images);

        setHeadline(prebidAd.getTitle());
        setBody(prebidAd.getDescription());
        setCallToAction(prebidAd.getCallToAction());
        setIcon(new PrebidImage(prebidAd.getIconUrl(), configuration.getContext()));
        setAdvertiser(prebidAd.getSponsoredBy());
        setOverrideClickHandling(true);
        setOverrideImpressionRecording(false);

        MediationNativeAdCallback adMobListener = adMobLoadListener.onSuccess(this);
        prebidListener = new SafeNativeListener(adMobListener);
    }

    private static class SafeNativeListener implements PrebidNativeAdEventListener {

        private final WeakReference<MediationNativeAdCallback> listenerReference;

        public SafeNativeListener(MediationNativeAdCallback adMobNativeListener) {
            listenerReference = new WeakReference<>(adMobNativeListener);
        }

        @Override
        public void onAdClicked() {
            MediationNativeAdCallback listener = listenerReference.get();
            if (listener != null) {
                listener.reportAdClicked();
                listener.onAdOpened();
            }
        }

        @Override
        public void onAdImpression() {
            MediationNativeAdCallback listener = listenerReference.get();
            if (listener != null) {
                Runnable reportImpression = listener::reportAdImpression;
                new Handler(Looper.getMainLooper()).post(reportImpression);
            }
        }

        @Override
        public void onAdExpired() {
        }
    }

    private static class PrebidImage extends NativeAd.Image {

        private final String url;
        private final double scale;
        private Drawable downloadedImage = null;

        public PrebidImage(String url, Context context) {
            this.url = url;
            this.scale = context.getResources().getDisplayMetrics().density;
        }

        @NonNull
        @Override
        public Drawable getDrawable() {
            if (downloadedImage != null) {
                return downloadedImage;
            }

            try {
                Bitmap x;

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();

                x = BitmapFactory.decodeStream(input);
                downloadedImage = new BitmapDrawable(Resources.getSystem(), x);
                return downloadedImage;
            } catch (Exception exception) {
                return new ShapeDrawable(new RectShape());
            }
        }

        @NonNull
        @Override
        public Uri getUri() {
            return Uri.parse(url);
        }

        @Override
        public double getScale() {
            return scale;
        }

    }

}
