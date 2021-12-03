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

import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeModelMakerBids;
import org.prebid.mobile.rendering.models.CreativeModelsMaker;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager implements AdLoadListener, Transaction.Listener {
    private static final String TAG = "TransactionManager";

    private final WeakReference<Context> mWeakContextReference;
    private final List<Transaction> mTransactions = new ArrayList<>();
    private final InterstitialManager mInterstitialManager;
    private final CreativeModelMakerBids mCreativeModelMakerBids = new CreativeModelMakerBids(this);

    private Transaction mLatestTransaction;
    private int mCurrentTransactionCreativeIndex;
    private TransactionManagerListener mListener;

    public TransactionManager(Context context, TransactionManagerListener listener, InterstitialManager interstitialManager) {
        mWeakContextReference = new WeakReference<>(context);
        mListener = listener;
        mInterstitialManager = interstitialManager;
    }

    //// AdLoadManager.Listener implementation
    @Override
    public void onCreativeModelReady(CreativeModelsMaker.Result result) {
        try {
            // Assign transaction to a field to prevent from being destroyed
            mLatestTransaction = Transaction.createTransaction(mWeakContextReference.get(),
                                                               result,
                                                               mInterstitialManager,
                                                               this);
            mLatestTransaction.startCreativeFactories();
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
        mLatestTransaction = null;
        if (mListener == null) {
            LogUtil.warn(TAG, "Unable to notify listener. Listener is null");
            return;
        }
        mTransactions.add(transaction);
        mListener.onFetchingCompleted(transaction);
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
    public void fetchBidTransaction(AdConfiguration adConfiguration, BidResponse bidResponse) {
        mCreativeModelMakerBids.makeModels(adConfiguration, bidResponse);
    }

    public void fetchVideoTransaction(AdConfiguration adConfiguration, String vastXml) {
        mCreativeModelMakerBids.makeVideoModels(adConfiguration, vastXml);
    }

    /**
     * Returns the transaction that should be displayed right now.
     *
     * @return first transaction
     */
    public Transaction getCurrentTransaction() {
        if (hasTransaction()) {
            return mTransactions.get(0);
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
            mTransactions.remove(0);
        }
        return getCurrentTransaction();
    }

    public AbstractCreative getCurrentCreative() {
        Transaction transaction = getCurrentTransaction();
        if (transaction == null) {
            LogUtil.error(TAG, "Get Current creative called with no ad");
            return null;
        }
        return transaction.getCreativeFactories().get(mCurrentTransactionCreativeIndex).getCreative();
    }

    public boolean hasNextCreative() {
        Transaction currentTransaction = getCurrentTransaction();
        if (currentTransaction == null) {
            return false;
        }
        int creativeFactoriesSize = currentTransaction.getCreativeFactories().size();
        return mCurrentTransactionCreativeIndex < creativeFactoriesSize - 1;
    }

    public void resetState() {
        Transaction transaction = getCurrentTransaction();
        if (transaction != null) {
            transaction.destroy();
            mTransactions.remove(0);
        }
        mCurrentTransactionCreativeIndex = 0;
        cancelBidModelMaker();
    }

    public boolean hasTransaction() {
        return !mTransactions.isEmpty();
    }

    public void destroy() {
        for (Transaction transaction : mTransactions) {
            transaction.destroy();
        }
        if (mLatestTransaction != null) {
            mLatestTransaction.destroy();
            mLatestTransaction = null;
        }

        cancelBidModelMaker();
        mListener = null;
    }

    public void incrementCreativesCounter() {
        mCurrentTransactionCreativeIndex++;
    }

    private void cancelBidModelMaker() {
        if (mCreativeModelMakerBids != null) {
            mCreativeModelMakerBids.cancel();
        }
    }

    private void notifyListenerError(AdException e) {
        if (mListener == null) {
            LogUtil.warn(TAG, "Unable to notify listener. Listener is null");
            return;
        }
        mListener.onFetchingFailed(e);
    }
}
