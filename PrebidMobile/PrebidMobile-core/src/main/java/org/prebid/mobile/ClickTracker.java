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
 * Internal click tracker.
 */
class ClickTracker {
    private String url;
    private boolean fired = false;
    private Context context;
    private ClickTrackerListener clickTrackerListener;

    static ClickTracker createAndFire(String url, Context context, ClickTrackerListener clickTrackerListener) {
        ClickTracker clickTracker = new ClickTracker(url, context, clickTrackerListener);
        clickTracker.fire();
        return clickTracker;
    }

    private ClickTracker(String url, Context context, ClickTrackerListener clickTrackerListener) {
        this.url = url;
        this.context = context.getApplicationContext();
        this.clickTrackerListener = clickTrackerListener;
    }

    private synchronized void fire() {
        // check if impression has already fired
        if (!fired) {
            SharedNetworkManager nm = SharedNetworkManager.getInstance(context);
            if (nm.isConnected(context)) {
                @SuppressLint("StaticFieldLeak") HTTPGet asyncTask = new HTTPGet() {
                    @Override
                    protected void onPostExecute(HTTPResponse response) {
                        if (clickTrackerListener != null) {
                            clickTrackerListener.onClickTrackerFired();
                        }
                    }

                    @Override
                    protected String getUrl() {
                        return url;
                    }
                };
                asyncTask.execute();
            } else {
                nm.addClickTrackerURL(url, context, new ClickTrackerListener() {
                    @Override
                    public void onClickTrackerFired() {
                        if (clickTrackerListener != null) {
                            clickTrackerListener.onClickTrackerFired();
                        }
                    }
                });
            }
            fired = true;
        }
    }
}
