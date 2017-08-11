package org.prebid.mobile.demoapp.dummyfragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.demoapp.R;


public class DummyFragment extends Fragment implements FBRequest.FBListener, AdListener, InterstitialAdListener {
    private View root;
    private AdView adView;
    private String bid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_banner, null);
        Button btnLoad = (Button) root.findViewById(R.id.loadBanner);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FBRequest rq = new FBRequest(DummyFragment.this, DummyFragment.this.getActivity());
                rq.execute();
            }
        });
        return root;
    }

    private void loadBanner(JSONObject jsonObject) {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        if (adView != null) {
            adView.destroy();
        }
//        adView = new AdView(this.getActivity(), "1959066997713356_1959836684303054", AdSize.RECTANGLE_HEIGHT_250); //new AdSize(300, 250)
//        adView.setAdListener(this);
//        adFrame.addView(adView);
        final InterstitialAd ad = new InterstitialAd(this.getActivity(), "1959066997713356_1960406244246098");
        ad.setAdListener(this);
        try {
            JSONArray seat = jsonObject.getJSONArray("seatbid");
            JSONArray b = seat.getJSONObject(0).getJSONArray("bid");
            JSONObject r = b.getJSONObject(0);
            bid = r.getString("adm");
        } catch (JSONException e) {
            bid = "";
        }
        ad.loadAdFromBid(bid);
//        adView.loadAdFromBid(bid);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ad.isAdLoaded()) {
                    ad.show();
                }
            }
        }, 2000);
    }

    @Override
    public void onError(Ad ad, AdError adError) {

    }

    @Override
    public void onAdLoaded(Ad ad) {

    }

    @Override
    public void onAdClicked(Ad ad) {

    }

    @Override
    public void onLoggingImpression(Ad ad) {

    }

    @Override
    public void onFBResponded(JSONObject jsonObject) {
        loadBanner(jsonObject);
    }

    @Override
    public void onInterstitialDisplayed(Ad ad) {

    }

    @Override
    public void onInterstitialDismissed(Ad ad) {

    }
}
