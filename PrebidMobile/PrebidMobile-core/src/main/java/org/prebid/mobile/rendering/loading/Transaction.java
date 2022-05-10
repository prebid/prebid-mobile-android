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

package org.prebid.mobile.rendering.loading;

import android.content.Context;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.CreativeModelsMaker;
import org.prebid.mobile.rendering.sdk.JSLibraryManager;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Transaction {

    public static final String TAG = Transaction.class.getSimpleName();

    private List<CreativeFactory> creativeFactories;
    private Iterator<CreativeFactory> creativeFactoryIterator;

    private List<CreativeModel> creativeModels;

    private WeakReference<Context> contextReference;
    private Listener listener;
    private OmAdSessionManager omAdSessionManager;
    private final InterstitialManager interstitialManager;

    private String transactionState;
    private String loaderIdentifier;

    private long transactionCreateTime;

    public interface Listener {

        void onTransactionSuccess(Transaction transaction);

        void onTransactionFailure(
                AdException e,
                String identifier
        );

    }

    private Transaction(Context context, List<CreativeModel> creativeModels,
                        String transactionState,
                        InterstitialManager interstitialManager,
                        Listener listener)
    throws AdException {
        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Transaction - Context is null");
        }

        if (creativeModels == null || creativeModels.isEmpty()) {
            throw new AdException(AdException.INTERNAL_ERROR, "Transaction - Creative models is empty");
        }

        if (listener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Transaction - Listener is null");
        }

        contextReference = new WeakReference<>(context);
        this.creativeModels = creativeModels;
        checkForBuiltInVideo();
        this.transactionState = transactionState;
        this.listener = listener;
        this.interstitialManager = interstitialManager;

        omAdSessionManager = OmAdSessionManager.createNewInstance(JSLibraryManager.getInstance(context));

        creativeFactories = new ArrayList<>();
    }

    public static Transaction createTransaction(Context context, CreativeModelsMaker.Result result,
                                                InterstitialManager interstitialManager, Listener listener)
    throws AdException {
        Transaction transaction = new Transaction(
            context,
            result.creativeModels,
            result.transactionState,
            interstitialManager,
            listener);
        transaction.setTransactionCreateTime(System.currentTimeMillis());
        transaction.setLoaderIdentifier(result.loaderIdentifier);

        return transaction;
    }

    private void checkForBuiltInVideo() {
        try {
            if (creativeModels != null && creativeModels.size() > 1) {
                CreativeModel creativeModel = creativeModels.get(0);
                boolean isBannerVideo = creativeModel.getAdConfiguration().isBuiltInVideo();
                if (isBannerVideo) {
                    CreativeModel possibleEndCard = creativeModels.get(1);
                    possibleEndCard.getAdConfiguration().setBuiltInVideo(true);
                }
            }
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed to check for built in video override");
        }
    }

    public String getTransactionState() {
        return transactionState;
    }

    public void startCreativeFactories() {
        try {
            // Initialize list of CreativeFactories
            creativeFactories.clear();
            for (CreativeModel creativeModel : creativeModels) {
                CreativeFactory creativeFactory = new CreativeFactory(contextReference.get(),
                        creativeModel,
                        new CreativeFactoryListener(this),
                        omAdSessionManager,
                        interstitialManager
                );
                creativeFactories.add(creativeFactory);
            }

            // Start first CreativeFactory, if any
            // On success, the CreativeFactoryListener will start the next CreativeFactory
            creativeFactoryIterator = creativeFactories.iterator();
            startNextCreativeFactory();
        }
        catch (AdException e) {
            listener.onTransactionFailure(e, loaderIdentifier);
        }
    }

    public void destroy() {
        stopOmAdSession();

        for (CreativeFactory creativeFactory : creativeFactories) {
            creativeFactory.destroy();
        }
    }

    private boolean startNextCreativeFactory() {
        // No CreativeFactory to start
        if (creativeFactoryIterator == null || !creativeFactoryIterator.hasNext()) {
            return false;
        }

        CreativeFactory creativeFactory = creativeFactoryIterator.next();
        creativeFactory.start();
        return true;
    }

    private void stopOmAdSession() {
        if (omAdSessionManager == null) {
            LogUtil.error(TAG, "Failed to stopOmAdSession. OmAdSessionManager is null");
            return;
        }

        omAdSessionManager.stopAdSession();
        omAdSessionManager = null;
    }

    public List<CreativeFactory> getCreativeFactories() {
        return creativeFactories;
    }

    public String getLoaderIdentifier() {
        return loaderIdentifier;
    }

    public void setLoaderIdentifier(String loaderIdentifier) {
        this.loaderIdentifier = loaderIdentifier;
    }

    public long getTransactionCreateTime() {
        return transactionCreateTime;
    }

    public void setTransactionCreateTime(long transactionCreateTime) {
        this.transactionCreateTime = transactionCreateTime;
    }

    /**
     * Listens for when CreativeFactory is done making a creative
     * When all CreativeFactory's are done, relays that back to Transaction's Listener
     */
    public static class CreativeFactoryListener implements CreativeFactory.Listener {

        private WeakReference<Transaction> weakTransaction;

        CreativeFactoryListener(Transaction transaction) {
            weakTransaction = new WeakReference<>(transaction);
        }

        @Override
        public void onSuccess() {
            Transaction transaction = weakTransaction.get();
            if (transaction == null) {
                LogUtil.warning(TAG, "CreativeMaker is null");
                return;
            }

            // Start next CreativeFactory, if any
            if (transaction.startNextCreativeFactory()) {
                return;
            }

            // If all CreativeFactories succeeded, return success
            transaction.listener.onTransactionSuccess(transaction);
        }

        @Override
        public void onFailure(AdException e) {
            Transaction transaction = weakTransaction.get();
            if (transaction == null) {
                LogUtil.warning(TAG, "CreativeMaker is null");
                return;
            }

            transaction.listener.onTransactionFailure(e, transaction.getLoaderIdentifier());
            transaction.destroy();
        }
    }
}
