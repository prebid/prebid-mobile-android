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

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.prebid.mobile.http.HTTPGet;
import org.prebid.mobile.http.HTTPResponse;
import org.prebid.mobile.http.HttpErrorCode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Internal class for network operations.
 */
public class SharedNetworkManager {

    private static SharedNetworkManager manager;

    public static SharedNetworkManager getInstance(Context context) {
        if (manager == null) {
            manager = new SharedNetworkManager(context);
        }
        return manager;
    }


    private ArrayList<UrlObject> urls = new ArrayList<UrlObject>();
    private Timer retryTimer;
    private static final int TOTAL_RETRY_TIMES = 3;
    private static final int TOTAL_RETRY_WAIT_INTERVAL_MILLISECONDS = 10 * 1000;
    private static final String permission = "android.permission.ACCESS_NETWORK_STATE";
    private boolean permitted;
    private ImpressionTrackerListener impressionTrackerListener;
    private ClickTrackerListener clickTrackerListener;

    private SharedNetworkManager(Context context) {
        int permissionStatus = context.getPackageManager().checkPermission(
                permission,
                context.getPackageName()
        );
        permitted = (permissionStatus == PackageManager.PERMISSION_GRANTED);
    }

    public boolean isConnected(Context context) {
        if (permitted) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        } else {
            return true;
        }
    }

    synchronized void addURL(String url, Context context) {
        addURL(url, context, null);
    }

    synchronized void addURL(String url, Context context, ImpressionTrackerListener impressionTrackerListener) {
        LogUtil.debug("SharedNetworkManager adding URL for Network Retry");
        this.impressionTrackerListener = impressionTrackerListener;
        urls.add(new UrlObject(url));
        startTimer(context);
    }

    synchronized void addClickTrackerURL(String url, Context context, ClickTrackerListener clickTrackerListener) {
        LogUtil.debug("SharedNetworkManager adding URL for Network Retry");
        this.clickTrackerListener = clickTrackerListener;
        urls.add(new UrlObject(url));
        startTimer(context);
    }

    private void startTimer(Context context) {
        if (retryTimer == null) {
            // check Network Connectivity after a certain period
            final WeakReference<Context> weakContext = new WeakReference<Context>(context);
            retryTimer = new Timer();
            retryTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Context context = weakContext.get();
                    if (context != null) {
                        while (!urls.isEmpty()) {
                            if (isConnected(context)) {
                                final UrlObject urlObject = urls.remove(0);
                                if (urlObject.retryTimes < TOTAL_RETRY_TIMES) {
                                    {
                                        HTTPGet fire = new HTTPGet() {
                                            @Override
                                            protected void onPostExecute(HTTPResponse response) {
                                                if (response == null ||
                                                        (!response.getSucceeded() && response.getErrorCode() == HttpErrorCode.CONNECTION_FAILURE)) {
                                                    urlObject.retryTimes += 1;
                                                    urls.add(urlObject);
                                                } else {
                                                    // Nothing more to do just print logs and exit.
                                                    if (impressionTrackerListener != null) {
                                                        impressionTrackerListener.onImpressionTrackerFired();
                                                    }

                                                    if (clickTrackerListener != null) {
                                                        clickTrackerListener.onClickTrackerFired();
                                                    }
                                                }

                                            }

                                            @Override
                                            protected String getUrl() {
                                                return urlObject.url;
                                            }
                                        };
                                        fire.execute();

                                    }
                                }
                            } else {
                                break;
                            }
                        }
                        if (urls.isEmpty()) stopTimer();
                    } else {
                        stopTimer();
                    }
                }
            }, TOTAL_RETRY_WAIT_INTERVAL_MILLISECONDS, TOTAL_RETRY_WAIT_INTERVAL_MILLISECONDS);

        }
    }

    private void stopTimer() {
        if (retryTimer != null) {
            retryTimer.cancel();
            retryTimer = null;
        }
    }

    class UrlObject {
        String url;
        int retryTimes;

        UrlObject(String url) {
            this.url = url;
            this.retryTimes = 0;
        }
    }

}
