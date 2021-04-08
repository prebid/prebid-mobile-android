package org.prebid.mobile.renderingtestapp.plugplay.bidding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HeaderBiddingViewModelFactory(private val integrationCategories: Array<String>,
                                    private val adCategories: Array<String>) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeaderBiddingViewModel::class.java)) {
            return HeaderBiddingViewModel(integrationCategories, adCategories) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}