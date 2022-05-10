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
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.CreativeModelMakerBids;
import org.prebid.mobile.rendering.models.CreativeModelsMaker;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager implements AdLoadListener, Transaction.Listener {

    private static final String TAG = "TransactionManager";

    private final WeakReference<Context> weakContextReference;
    private final List<Transaction> transactions = new ArrayList<>();
    private final InterstitialManager interstitialManager;
    private final CreativeModelMakerBids creativeModelMakerBids = new CreativeModelMakerBids(this);

    private Transaction latestTransaction;
    private int currentTransactionCreativeIndex;
    private TransactionManagerListener listener;

    public TransactionManager(
            Context context,
            TransactionManagerListener listener,
            InterstitialManager interstitialManager
    ) {
        weakContextReference = new WeakReference<>(context);
        this.listener = listener;
        this.interstitialManager = interstitialManager;
    }

    //// AdLoadManager.Listener implementation
    @Override
    public void onCreativeModelReady(CreativeModelsMaker.Result result) {
        try {
            // Assign transaction to a field to prevent from being destroyed
            latestTransaction = Transaction.createTransaction(weakContextReference.get(),
                    result,
                    interstitialManager,
                    this
            );
            latestTransaction.startCreativeFactories();
        }
        catch (AdException e) {
            notifyListenerError(e);
        }
    }

    @Override
    public void onFailedToLoadAd(AdException e, String vastLoaderIdentifier) {
        notifyListenerError(e);
    }

    //// Transaction.Listener implementation
    @Override
    public void onTransactionSuccess(Transaction transaction) {
        latestTransaction = null;
        if (listener == null) {
            LogUtil.warning(TAG, "Unable to notify listener. Listener is null");
            return;
        }
        transactions.add(transaction);
        listener.onFetchingCompleted(transaction);
    }

    @Override
    public void onTransactionFailure(AdException e, String identifier) {
        notifyListenerError(e);
    }

    /**
     * Initiates the process of creating creative model and transaction from parsed bid response
     *
     * @param adConfiguration - AdConfiguration
     * @param bidResponse     - parsed bid response
     */
    public void fetchBidTransaction(AdUnitConfiguration adConfiguration, BidResponse bidResponse) {
        creativeModelMakerBids.makeModels(adConfiguration, bidResponse);
    }

    public void fetchVideoTransaction(AdUnitConfiguration adConfiguration, String vastXml) {
        creativeModelMakerBids.makeVideoModels(adConfiguration, vastXml);
    }

    /**
     * Returns the transaction that should be displayed right now.
     *
     * @return first transaction
     */
    public Transaction getCurrentTransaction() {
        if (hasTransaction()) {
            return transactions.get(0);
        }
        return null;
    }

    /**
     * Removes the current transaction from internal cache
     *
     * @return the next transactions in the list
     */
    public Transaction dismissCurrentTransaction() {
        if (hasTransaction()) {
            transactions.remove(0);
        }
        return getCurrentTransaction();
    }

    public AbstractCreative getCurrentCreative() {
        Transaction transaction = getCurrentTransaction();
        if (transaction == null) {
            LogUtil.error(TAG, "Get Current creative called with no ad");
            return null;
        }
        return transaction.getCreativeFactories().get(currentTransactionCreativeIndex).getCreative();
    }

    public boolean hasNextCreative() {
        Transaction currentTransaction = getCurrentTransaction();
        if (currentTransaction == null) {
            return false;
        }
        int creativeFactoriesSize = currentTransaction.getCreativeFactories().size();
        return currentTransactionCreativeIndex < creativeFactoriesSize - 1;
    }

    public void resetState() {
        Transaction transaction = getCurrentTransaction();
        if (transaction != null) {
            transaction.destroy();
            transactions.remove(0);
        }
        currentTransactionCreativeIndex = 0;
        cancelBidModelMaker();
    }

    public boolean hasTransaction() {
        return !transactions.isEmpty();
    }

    public void destroy() {
        for (Transaction transaction : transactions) {
            transaction.destroy();
        }
        if (latestTransaction != null) {
            latestTransaction.destroy();
            latestTransaction = null;
        }

        cancelBidModelMaker();
        listener = null;
    }

    public void incrementCreativesCounter() {
        currentTransactionCreativeIndex++;
    }

    private void cancelBidModelMaker() {
        if (creativeModelMakerBids != null) {
            creativeModelMakerBids.cancel();
        }
    }

    private void notifyListenerError(AdException e) {
        if (listener == null) {
            LogUtil.warning(TAG, "Unable to notify listener. Listener is null");
            return;
        }
        listener.onFetchingFailed(e);
    }
}
