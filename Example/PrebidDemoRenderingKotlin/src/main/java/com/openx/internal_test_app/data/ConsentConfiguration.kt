package com.openx.internal_test_app.data

import com.google.gson.annotations.SerializedName

class ConsentConfiguration(@SerializedName("launchOptions") val launchOptions: Map<String, Any?>?,
                           @SerializedName("updateInterval") val updateIntervalSec: Long?,
                           @SerializedName("updatedOptions") val updatedOptionList: List<Map<String, Any?>>?)