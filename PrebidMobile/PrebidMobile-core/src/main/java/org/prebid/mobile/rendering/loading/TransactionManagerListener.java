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

import org.prebid.mobile.api.exceptions.AdException;

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
