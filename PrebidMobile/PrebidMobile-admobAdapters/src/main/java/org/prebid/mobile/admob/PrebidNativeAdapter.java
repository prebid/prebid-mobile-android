package org.prebid.mobile.admob;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventNative;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.rendering.bidding.events.EventsNotifier;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PrebidNativeAdapter extends PrebidBaseAdapter implements CustomEventNative {

    private static final String TAG = "PrebidNative";

    @Override
    public void requestNativeAd(
            @NonNull Context context,
            @NonNull CustomEventNativeListener adMobListener,
            @Nullable String serverParameter,
            @NonNull NativeMediationAdRequest mediationAdRequest,
            @Nullable Bundle extras
    ) {
        if (extras == null) {
            String error = "Extras are empty! Check if you add custom event extras bundle to  " + TAG;
            Log.e(TAG, error);
            adMobListener.onAdFailedToLoad(new AdError(1001, error, "prebid"));
            return;
        }

        String cacheId = extras.getString(NativeAdUnit.BUNDLE_KEY_CACHE_ID);
        if (cacheId == null) {
            String error = "Cache id is null";
            adMobListener.onAdFailedToLoad(new AdError(1002, error, "prebid"));
            return;
        }

        Bundle extrasCopy = new Bundle(extras);
        extrasCopy.remove(NativeAdUnit.BUNDLE_KEY_CACHE_ID);
        HashMap<String, String> prebidParameters = convertToMap(extrasCopy);


        if (!ParametersMatcher.doParametersMatch(serverParameter, prebidParameters)) {
            String error = "Parameters are different";
            adMobListener.onAdFailedToLoad(new AdError(1003, error, "prebid"));
            return;
        }
        LogUtil.verbose(TAG, "Parameters are matched! (" + serverParameter + ")");

        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);
        if (nativeAd == null) {
            String error = "PrebidNativeAd is null";
            adMobListener.onAdFailedToLoad(new AdError(1004, error, "prebid"));
            return;
        }
        EventsNotifier.notify(nativeAd.getWinEvent());

        PrebidNativeAdMapper mapper = new PrebidNativeAdMapper(nativeAd, adMobListener);
        configureMapper(mapper, nativeAd, context);
        adMobListener.onAdLoaded(mapper);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    private HashMap<String, String> convertToMap(Bundle bundle) {
        HashMap<String, String> result = new HashMap<>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            String value = bundle.getString(key);
            result.put(key, value);
        }
        return result;
    }

    private void configureMapper(PrebidNativeAdMapper mapper, PrebidNativeAd prebidAd, Context context) {
        ArrayList<NativeAd.Image> images = new ArrayList<>();
        images.add(new PrebidImage(prebidAd.getImageUrl(), context));

        mapper.setImages(images);
        mapper.setHeadline(prebidAd.getTitle());
        mapper.setBody(prebidAd.getDescription());
        mapper.setCallToAction(prebidAd.getCallToAction());
        mapper.setIcon(new PrebidImage(prebidAd.getIconUrl(), context));
        mapper.setAdvertiser(prebidAd.getSponsoredBy());
        mapper.setOverrideClickHandling(true);
        mapper.setOverrideImpressionRecording(false);
    }

    private static class PrebidImage extends NativeAd.Image {

        private final String url;
        private final double scale;
        private Drawable downloadedImage = null;

        public PrebidImage(String url, Context context) {
            this.url = url;
            this.scale = context.getResources().getDisplayMetrics().density;
        }

        @Nullable
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
                return null;
            }
        }

        @Nullable
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
