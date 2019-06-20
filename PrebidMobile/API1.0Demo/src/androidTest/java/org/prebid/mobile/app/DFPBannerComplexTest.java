/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile.app;

import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DFPBannerComplexTest {
    @Rule
    public ActivityTestRule<DemoActivity> m = new ActivityTestRule<>(DemoActivity.class);

    //30x250 -> 728x90
    @Test
    public void testRubiconDFPBannerResizeSanityAppCheckTest() throws Exception {

        final CountDownLatch lock = new CountDownLatch(1);

        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_RUBICON);

        DemoActivity demoActivity = m.getActivity();

        final IntegerWrapper firstTransactionCount = new IntegerWrapper();
        final IntegerWrapper secondTransactionCount = new IntegerWrapper();

        final int transactionFailRepeatCount = 5;
        final int screenshotDelayMillis = 3_000;
        final int transactionFailDelayMillis = 3_000;

        final FrameLayout adFrame = demoActivity.findViewById(R.id.adFrame);
        demoActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adFrame.removeAllViews();
            }
        });

        final PublisherAdView dfpAdView = new PublisherAdView(demoActivity);
        //Programmatic fix
        dfpAdView.setAdUnitId("/5300653/test_adunit_pavliuchyk_300x250_puc_ucTagData_prebid-server.rubiconproject.com");
        dfpAdView.setAdSizes(new AdSize(300, 250));

        //Targeting creative
        /*
        dfpAdView.setAdUnitId("/5300653/Banner_PUC_b397711");
        dfpAdView.setAdSizes(new AdSize(300, 250), new AdSize(728, 90));
        */

        final BannerAdUnit bannerAdUnit = new BannerAdUnit("1001-1", 300, 250);

        final PublisherAdRequest request = new PublisherAdRequest.Builder().build();
        final OnCompleteListener completeListener = new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                dfpAdView.loadAd(request);
            }
        };

        dfpAdView.setAdListener(new AdListener() {

            private void notifyResult() {
                lock.countDown();
            }

            private void update(boolean isSuccess) {
                if (isSuccess) {

                    if (firstTransactionCount.getValue() != -1) {
                        firstTransactionCount.value = -1;
                        //TODO make a call

                        bannerAdUnit.addAdditionalSize(728, 90);
                        bannerAdUnit.fetchDemand(request, completeListener);
                    } else if (secondTransactionCount.getValue() != -1) {
                        secondTransactionCount.value = -1;

                        notifyResult();
                    }

                } else {
                    try {
                        Thread.sleep(transactionFailDelayMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (firstTransactionCount.getValue() != -1) {
                        if (firstTransactionCount.getValue() > transactionFailRepeatCount -2) {
                            Assert.fail("first Transaction Count == " + transactionFailRepeatCount);
                        } else {
                            //repeat
                            firstTransactionCount.value++;
                            bannerAdUnit.fetchDemand(request, completeListener);
                        }
                    } else if (secondTransactionCount.getValue() != -1) {
                        if (secondTransactionCount.getValue() > transactionFailRepeatCount -2) {
                            Assert.fail("second Transaction Count == " + transactionFailRepeatCount);
                        } else {
                            //repeat
                            secondTransactionCount.value++;
                            bannerAdUnit.fetchDemand(request, completeListener);
                        }
                    } else {
                        Assert.fail("Unexpected");
                    }

                }

            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                //Programmatic fix
                Util.findPrebidCreativeSize(dfpAdView, new Util.CreativeSizeCompletionHandler() {
                    @Override
                    public void onSize(final Util.CreativeSize size) {
                        if (size != null) {

                            dfpAdView.setAdSizes(new AdSize(size.getWidth(), size.getHeight()));

                            final View child = dfpAdView.getChildAt(0);
                            child.setBackgroundColor(Color.RED);

                            dfpAdView.post(new Runnable() {
                                @Override
                                public void run() {

                                    float density = dfpAdView.getResources().getDisplayMetrics().density;
                                    double dpW = Math.ceil(child.getMinimumWidth() / density);
                                    double dpH = Math.ceil(child.getMinimumHeight() / density);

                                    try {
                                        Thread.sleep(screenshotDelayMillis);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    assertEquals((int)dpW, size.getWidth());
                                    assertEquals((int)dpH, size.getHeight());

                                    update(true);

                                }
                            });

                        } else {
                            LogUtil.w("size is null");
                            update(false);
                        }
                    }
                });

                //Targeting creative
                /*
                try {
                    Thread.sleep(screenshotDelayMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                update(true);
                */
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                LogUtil.w("onAdFailedToLoad:" + i);

                update(false);
            }
        });

        demoActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adFrame.addView(dfpAdView);
            }
        });

        bannerAdUnit.fetchDemand(request, completeListener);

        //TravisCI fix
        Thread.sleep(2 * transactionFailRepeatCount * transactionFailDelayMillis + 2 * screenshotDelayMillis);
        //local test
//        lock.await();

        try {
            Thread.sleep(screenshotDelayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(-1, firstTransactionCount.getValue());
        assertEquals(-1, secondTransactionCount.getValue());


    }

    private static class IntegerWrapper {
        int value = 0;

        public int getValue() {
            return value;
        }
    }

}
