package com.openx.apollo.loading;

import com.openx.apollo.errors.AdException;

public interface TransactionManagerListener {
    /**
     * Is called when TransactionManager has finished the fetching process.
     * In case of success, the transaction represents the loaded transaction.
     *
     * @param transaction successful transaction
     */
    void onFetchingCompleted(Transaction transaction);

    /**
     * In case of failure, the error should be not null and contains the description of the issue
     *
     * @param exception used to inform the listener in case something is wrong
     */
    void onFetchingFailed(AdException exception);
}
