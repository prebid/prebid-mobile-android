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

package org.prebid.mobile.rendering.sdk.deviceData.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.rendering.sdk.BaseManager;

public class UserConsentManager extends BaseManager {

    static final int NOT_ASSIGNED = -1;

    // TCF v1 constants
    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    private static final String CONSENT_STRING = "IABConsent_ConsentString";

    // TCF v2 constants
    private static final String CMP_SDK_ID = "IABTCF_CmpSdkID";
    private static final String GDPR_APPLIES = "IABTCF_gdprApplies";
    private static final String TRANSPARENCY_CONSENT_STRING = "IABTCF_TCString";
    private static final String PURPOSE_CONSENT = "IABTCF_PurposeConsents";

    // CCPA
    private static final String US_PRIVACY_STRING = "IABUSPrivacy_String";

    private String mUsPrivacyString;

    private String mIsSubjectToGdpr = "";
    private String mConsentString;
    private String mPurposeConsent;

    private String mTcfV2ConsentString;
    private int mTcfV2GdprApplies = NOT_ASSIGNED;
    /**
     * The unsigned integer ID of CMP SDK. Less than 0 values should be considered invalid.
     */
    private int mCmpSdkId = NOT_ASSIGNED;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    @Override
    public void init(Context context) {
        super.init(context);

        if (super.isInit() && context != null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            initConsentValuesAtStart(mSharedPreferences);

            mOnSharedPreferenceChangeListener = this::getConsentValues;

            mSharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        }
    }

    private void initConsentValuesAtStart(SharedPreferences preferences) {
        getConsentValues(preferences, SUBJECT_TO_GDPR);
        getConsentValues(preferences, CONSENT_STRING);
        getConsentValues(preferences, CMP_SDK_ID);
        getConsentValues(preferences, GDPR_APPLIES);
        getConsentValues(preferences, TRANSPARENCY_CONSENT_STRING);
        getConsentValues(preferences, US_PRIVACY_STRING);
        getConsentValues(preferences, PURPOSE_CONSENT);
    }

    private Object getConsentValues(SharedPreferences preferences, String key) {
        switch (key) {
            case SUBJECT_TO_GDPR:
                return mIsSubjectToGdpr = preferences.getString(SUBJECT_TO_GDPR, null);
            case CONSENT_STRING:
                return mConsentString = preferences.getString(CONSENT_STRING, null);
            case CMP_SDK_ID:
                return mCmpSdkId = preferences.getInt(CMP_SDK_ID, NOT_ASSIGNED);
            case GDPR_APPLIES:
                return mTcfV2GdprApplies = preferences.getInt(GDPR_APPLIES, NOT_ASSIGNED);
            case TRANSPARENCY_CONSENT_STRING:
                return mTcfV2ConsentString = preferences.getString(TRANSPARENCY_CONSENT_STRING, null);
            case US_PRIVACY_STRING:
                return mUsPrivacyString = preferences.getString(US_PRIVACY_STRING, null);
            case PURPOSE_CONSENT:
                return mPurposeConsent = preferences.getString(PURPOSE_CONSENT, null);
        }

        return null;
    }

    public String getSubjectToGdpr() {
        if (shouldUseTcfV2()) {
            return getTcfV2GdprApplies();
        }

        return mIsSubjectToGdpr;
    }

    public String getUserConsentString() {
        if (shouldUseTcfV2()) {
            return mTcfV2ConsentString;
        }

        return mConsentString;
    }

    public String getUsPrivacyString() {
        return mUsPrivacyString;
    }

    //fetch advertising identifier based TCF 2.0 Purpose1 value
    //truth table
    /*
                         deviceAccessConsent=true   deviceAccessConsent=false  deviceAccessConsent undefined
    gdprApplies=false        Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
    gdprApplies=true         Yes, read IDFA             No, don’t read IDFA           No, don’t read IDFA
    gdprApplies=undefined    Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
    */

    public boolean canAccessDeviceData() {
        final String subjectToGdpr = getSubjectToGdpr();
        final int deviceConsentIndex = 0;

        Boolean gdprApplies = TextUtils.isEmpty(subjectToGdpr) ? null : "1".equals(subjectToGdpr);
        Boolean deviceAccessConsent = getPurposeConsent(deviceConsentIndex);

        //deviceAccess undefined and gdprApplies undefined
        if (deviceAccessConsent == null && gdprApplies == null) {
            return true;
        }

        //deviceAccess undefined and gdprApplies false
        if (deviceAccessConsent == null && Boolean.FALSE.equals(gdprApplies)) {
            return true;
        }

        //deviceAccess true
        return Boolean.TRUE.equals(deviceAccessConsent);
    }

    @Nullable
    Boolean getPurposeConsent(int consentIndex) {
        boolean isConsentStringInvalid = TextUtils.isEmpty(mPurposeConsent) || mPurposeConsent.length() <= consentIndex;
        return isConsentStringInvalid ? null : mPurposeConsent.charAt(consentIndex) == '1';
    }

    /**
     * @return true if {@link #mCmpSdkId} is grater or equal than 0, false otherwise.
     */
    @VisibleForTesting
    boolean shouldUseTcfV2() {
        return mCmpSdkId >= 0;
    }

    @VisibleForTesting
    String getTcfV2GdprApplies() {
        if (mTcfV2GdprApplies == NOT_ASSIGNED) {
            return null;
        }

        return String.valueOf(mTcfV2GdprApplies);
    }
}
