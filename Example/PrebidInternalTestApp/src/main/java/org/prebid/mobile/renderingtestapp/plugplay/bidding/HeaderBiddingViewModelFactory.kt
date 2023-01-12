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

package org.prebid.mobile.renderingtestapp.plugplay.bidding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.prebid.mobile.renderingtestapp.utils.GdprHelper

class HeaderBiddingViewModelFactory(
    private val integrationCategories: Array<String>,
    private val adCategories: Array<String>,
    private val gdprHelper: GdprHelper
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeaderBiddingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeaderBiddingViewModel(integrationCategories, adCategories, gdprHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}