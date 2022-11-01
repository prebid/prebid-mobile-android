package org.prebid.mobile.admob;

import com.google.android.gms.ads.AdError;

interface OnLoadFailure {

    void run(AdError adError);

}
