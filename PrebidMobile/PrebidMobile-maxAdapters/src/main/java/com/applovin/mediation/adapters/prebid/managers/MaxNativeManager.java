package com.applovin.mediation.adapters.prebid.managers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.prebid.ParametersChecker;
import com.applovin.mediation.adapters.prebid.PrebidMaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAd;

import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.rendering.bidding.events.EventsNotifier;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MaxNativeManager {

    private static final String TAG = MaxNativeManager.class.getSimpleName();

    @Nullable
    private MaxNativeAdAdapterListener maxListener;
    private HttpURLConnection connection;
    private InputStream inputStream;

    public void loadAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxNativeAdAdapterListener maxListener
    ) {
        this.maxListener = maxListener;

        PrebidNativeAd prebidNativeAd = ParametersChecker.getNativeAd(parameters, this::onError);
        if (prebidNativeAd == null) {
            return;
        }

        EventsNotifier.notify(prebidNativeAd.getWinEvent());

        MaxNativeAd maxNativeAd = createMaxNativeAd(prebidNativeAd, activity, maxListener);
        maxListener.onNativeAdLoaded(maxNativeAd, new Bundle());
    }

    public void destroy() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception ignored) {}
    }

    private MaxNativeAd createMaxNativeAd(
            PrebidNativeAd ad,
            Activity activity,
            MaxNativeAdAdapterListener maxListener
    ) {
        MaxNativeAd.Builder builder = new MaxNativeAd.Builder();
        builder.setTitle(ad.getTitle())
               .setAdvertiser(ad.getSponsoredBy())
               .setBody(ad.getDescription())
               .setCallToAction(ad.getCallToAction());

        Bitmap mainBitmap = downloadImage(ad.getImageUrl());
        if (mainBitmap != null) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageBitmap(mainBitmap);
            builder.setMediaView(imageView);
        }

        Bitmap iconBitmap = downloadImage(ad.getIconUrl());
        if (iconBitmap != null) {
            Drawable drawable = new BitmapDrawable(activity.getResources(), iconBitmap);
            builder.setIcon(new MaxNativeAd.MaxNativeAdImage(drawable));
        }

        return new PrebidMaxNativeAd(builder, ad, maxListener);
    }

    @Nullable
    private Bitmap downloadImage(String url) {
        Bitmap result = null;
        try {
            URL mainImageUrl = new URL(url);
            connection = (HttpURLConnection) mainImageUrl.openConnection();
            connection.connect();
            inputStream = connection.getInputStream();
            result = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            connection.disconnect();
        } catch (Exception exception) {
            Log.e(TAG, "Can't download image: (" + url + ")");
        }
        return result;
    }

    private void onError(
            int code,
            String error
    ) {
        if (maxListener != null) {
            maxListener.onNativeAdLoadFailed(new MaxAdapterError(code, error));
        } else {
            Log.e(TAG, "Max native listener is null: " + error);
        }
    }

}
