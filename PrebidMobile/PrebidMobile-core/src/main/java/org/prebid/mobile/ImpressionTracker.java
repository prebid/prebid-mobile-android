/*
 *    Copyright 2020-2021 Prebid.org, Inc.
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

package org.prebid.mobile;

import android.annotation.SuppressLint;
import android.content.Context;

import org.prebid.mobile.http.HTTPGet;
import org.prebid.mobile.http.HTTPResponse;

/**
 * Impression tracker for native ad.
 */
class ImpressionTracker {
    private String url;
    private VisibilityDetector visibilityDetector;
    private boolean fired = false;
    private Context context;
    private ImpressionListener listener;
    private ImpressionTrackerListener impressionTrackerListener;

    static ImpressionTracker create(String url, VisibilityDetector visibilityDetector, Context context, ImpressionTrackerListener impressionTrackerListener) {
        if (visibilityDetector == null) {
            return null;
        } else {
            ImpressionTracker impressionTracker = new ImpressionTracker(url, visibilityDetector, context, impressionTrackerListener);
            visibilityDetector.addVisibilityListener(impressionTracker.listener);
            return impressionTracker;
        }
    }

    private ImpressionTracker(String url, VisibilityDetector visibilityDetector, Context context, ImpressionTrackerListener impressionTrackerListener) {
        this.url = url;
        this.visibilityDetector = visibilityDetector;
        this.listener = new ImpressionListener();
        this.context = context.getApplicationContext();
        this.impressionTrackerListener = impressionTrackerListener;
    }

    private synchronized void fire() {
        // check if impression has already fired
        if (!fired) {
            SharedNetworkManager nm = SharedNetworkManager.getInstance(context);
            if (nm.isConnected(context)) {
                @SuppressLint("StaticFieldLeak") HTTPGet asyncTask = new HTTPGet() {
                    @Override
                    protected void onPostExecute(HTTPResponse response) {
                        if (impressionTrackerListener != null) {
                            impressionTrackerListener.onImpressionTrackerFired();
                        }
                    }

                    @Override
                    protected String getUrl() {
                        return url;
                    }
                };
                asyncTask.execute();
                visibilityDetector.removeVisibilityListener(listener);
                listener = null;
            } else {
                nm.addURL(url, context, new ImpressionTrackerListener() {
                    @Override
                    public void onImpressionTrackerFired() {
                        if (impressionTrackerListener != null) {
                            impressionTrackerListener.onImpressionTrackerFired();
                        }
                    }
                });
            }
            fired = true;
        }
    }

    class ImpressionListener implements VisibilityDetector.VisibilityListener {
        long elapsedTime = 0;

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                elapsedTime += VisibilityDetector.VISIBILITY_THROTTLE_MILLIS;
            } else {
                elapsedTime = 0;
            }
            if (elapsedTime >= Util.NATIVE_AD_VISIBLE_PERIOD_MILLIS) {
                ImpressionTracker.this.fire();
            }
        }
    }

}
