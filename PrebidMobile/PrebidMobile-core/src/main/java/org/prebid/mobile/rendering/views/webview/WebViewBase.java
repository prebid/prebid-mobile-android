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

package org.prebid.mobile.rendering.views.webview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.sdk.JSLibraryManager;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.prebid.mobile.rendering.views.webview.AdWebViewClient.AdAssetsLoadedListener;

public class WebViewBase extends AdWebView implements AdAssetsLoadedListener {

    private static final String TAG = WebViewBase.class.getSimpleName();

    private static final String REGEX_IFRAME = "(<iframe[^>]*)>";

    protected MraidEventsManager.MraidListener mraidListener;
    protected String MRAIDBridgeName;

    private PreloadManager.PreloadedListener preloadedListener;

    private BaseJSInterface mraidInterface;
    private String adHTML;
    private AdBaseDialog dialog;

    private boolean containsIFrame;
    private boolean isClicked = false;
    protected boolean isMRAID;

    private String targetUrl;

    public WebViewBase(
            Context context,
            String html,
            int width,
            int height,
            PreloadManager.PreloadedListener preloadedListener,
            MraidEventsManager.MraidListener mraidInterface
    ) {
        super(context);

        this.width = width;
        this.height = height;
        adHTML = html;
        this.preloadedListener = preloadedListener;
        mraidListener = mraidInterface;
        initWebView();
    }

    public WebViewBase(Context context, PreloadManager.PreloadedListener preloadedListener, MraidEventsManager.MraidListener mraidHelper) {
        super(context);
        this.preloadedListener = preloadedListener;
        mraidListener = mraidHelper;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        mraidListener.onAdWebViewWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public void startLoadingAssets() {
        mraidInterface.loading();
    }

    @Override
    public void adAssetsLoaded() {

        if (isMRAID) {
            getMRAIDInterface().prepareAndSendReady();
        }

        if (preloadedListener != null) {

            preloadedListener.preloaded(this);
        }
    }

    @Override
    public void notifyMraidScriptInjected() {
        isMRAID = true;
    }

    @Override
    public void destroy() {
        super.destroy();
        mraidInterface.destroy();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (isMRAID()) {
            getMRAIDInterface().updateScreenMetricsAsync(null);
        }
    }

    public void loadAd() {
        //inject MRAID here
        initLoad();

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setIsClicked(true);
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        /****
         * Sometimes assets just don't load into the WebView, event though the
         * onPageFinished is called. So replacing the following loadData call
         * with loadDataWithBaseURL, although it doesn't seem to have an effect!
         * http
         * ://stackoverflow.com/questions/2969949/cant-loadAdConfiguration-image-in-webview-
         * via-javascript
         * ***/
        loadDataWithBaseURL(PrebidMobile.SCHEME_HTTPS + "://" + domain + "/", adHTML, "text/html", "utf-8", null);
    }

    public void setJSName(String name) {
        MRAIDBridgeName = name;
    }

    public String getJSName() {
        return MRAIDBridgeName;
    }

    public MraidEventsManager.MraidListener getMraidListener() {
        return mraidListener;
    }

    public void setDialog(AdBaseDialog dialog) {
        this.dialog = dialog;
    }

    public AdBaseDialog getDialog() {
        return dialog;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public void setIsClicked(boolean isClicked) {
        this.isClicked = isClicked;
    }

    public PreloadManager.PreloadedListener getPreloadedListener() {
        return preloadedListener;
    }

    /**
     * Initializes {@link #containsIFrame}. Sets true if <iframe> tag was found in html
     *
     * @param html html without injected {@link R.raw#omsdk_v1} NOTE: Without injected {@link R.raw#omsdk_v1}
     *             because {@link R.raw#omsdk_v1} contains <iframe>
     */
    public void initContainsIFrame(String html) {
        Pattern iframePattern = Pattern.compile(REGEX_IFRAME, Pattern.CASE_INSENSITIVE);
        Matcher matcher = iframePattern.matcher(html);
        containsIFrame = matcher.find();
    }

    /**
     * @return true if html in {@link #initContainsIFrame(String)} contained <iframe>
     */
    public boolean containsIFrame() {
        return containsIFrame;
    }

    public ViewGroup getParentContainer() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            return (ViewGroup) parent;
        }
        return null;
    }

    public void detachFromParent() {
        ViewGroup parent = getParentContainer();

        if (parent != null) {
            parent.removeView(this);
        }
    }

    public BaseJSInterface getMRAIDInterface() {
        return mraidInterface;
    }

    public void setBaseJSInterface(BaseJSInterface mraid) {
        mraidInterface = mraid;
    }

    public void setAdWidth(int width) {
        this.width = width;
    }

    public int getAdWidth() {
        return width;
    }

    public void setAdHeight(int height) {
        this.height = height;
    }

    public int getAdHeight() {
        return height;
    }

    public boolean isMRAID() {
        return isMRAID;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void initLoad() {

        setVisibility(INVISIBLE);

        if (MraidVariableContainer.getDisabledFlags() == null) {
            MraidVariableContainer.setDisabledSupportFlags(0);
        }

        //IMPORTANT: sets the webviewclient to get callbacks on webview
        final String mraidScript = JSLibraryManager.getInstance(getContext()).getMRAIDScript();
        setMraidAdAssetsLoadListener(this,
                                     mraidScript);
        /*
         * Keep this for development purposes...very handy!
         */
        //adHTML = Utils.loadStringFromFile(getResources(), R.raw.testmraidfullad);
        // adHTML = Utils.loadStringFromFile(getResources(), R.raw.html);

        adHTML = createAdHTML(adHTML);
    }

    public void sendClickCallBack(String url) {
        post(() -> mraidListener.openMraidExternalLink(url));
    }

    public boolean canHandleClick() {
        return getMRAIDInterface() != null && getPreloadedListener() != null;
    }

    protected void initWebView() {
        super.initializeWebView();
        super.initializeWebSettings();
    }

    private String createAdHTML(String originalHtml) {
        String meta = buildViewportMetaTag();
        String centerAdStyle = "<style type='text/css'>html,body {margin: 0;padding: 0;width: 100%;height: 100%;}html {display: table;}body {display: table-cell;vertical-align: middle;text-align: center;}</style>";

        originalHtml = "<html><head>" + meta

                       + "<body>"
                       + centerAdStyle

                       + originalHtml

                       + "</body></html>";
        return originalHtml;
    }

    private String buildViewportMetaTag() {
        String meta;

        // Android 2.2: viewport meta tag does not seem to be supported at all.
        //
        // Android 2.3.x/3.x: By setting user-scalable=no you disable the
        // scaling of the viewport meta tag yourself
        // as well. This is probably why your width option is having no effect.
        // To allow the browser to scale your
        // content, you need to set user-scalable=yes, then to disable zoom you
        // can set the min and max scale to the
        // same value so it cannot shrink or grow. Toy with the initial scale
        // until your site fits snugly.
        //
        // Android 4.x: Same rule apply as 2.3.x except the min and max scales
        // are not honored anymore and if you use
        // user-scalable=yes the user can always zoom, setting it to 'no' means
        // your own scale is ignored, this is the
        // issue I'm facing now that drew me to this question... You cannot seem
        // to disable zoom and scale at the same
        // time in 4.x.
        //
        // Note, when you work with this area of code, it goes hand in hand with code
        // in AdWebView in the initializeWebSettings method, in particular the webSettings.
        // Also, important, if one changes the AndroidManifest.xml for targetSDKVersion from <=17 to >=19
        // The new OS 19 (KitKat) will have a drastic effect on the WebView.  Which is why we have
        // forked the code.
        //

        String scale = getInitialScaleValue();
        String metaTag;
        if (scale != null && !scale.isEmpty()) {
            metaTag = "<meta name='viewport' content='width=device-width, initial-scale=" + scale + ", minimum-scale=0.01' />";
        } else {
            metaTag = "<meta name='viewport' content='width=device-width' />";
        }
        return metaTag;
    }
}
