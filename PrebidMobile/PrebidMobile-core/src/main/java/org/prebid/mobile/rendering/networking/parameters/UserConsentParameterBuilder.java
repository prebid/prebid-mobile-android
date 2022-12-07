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

package org.prebid.mobile.rendering.networking.parameters;

import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

public class UserConsentParameterBuilder extends ParameterBuilder {

    private static final String GDPR = "gdpr";
    private static final String US_PRIVACY = "us_privacy";
    private static final String CONSENT = "consent";
    private static final String COPPA_SUBJECT = "coppa";

    private final UserConsentManager userConsentManager;

    public UserConsentParameterBuilder() {
        this.userConsentManager = ManagersResolver.getInstance().getUserConsentManager();
    }

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        BidRequest bidRequest = adRequestInput.getBidRequest();

        appendGdprParameter(bidRequest);
        appendCcpaParameter(bidRequest);
        appendCoppaParameter(bidRequest);
        appendGppParameter(bidRequest);
    }

    private void appendGdprParameter(BidRequest bidRequest) {
        Boolean subjectToGdpr = userConsentManager.getSubjectToGdpr();

        if (subjectToGdpr != null) {
            int gdprValue = subjectToGdpr ? 1 : 0;
            bidRequest.getRegs().getExt().put(GDPR, gdprValue);

            String userConsentString = userConsentManager.getGdprConsent();
            if (!Utils.isBlank(userConsentString)) {
                bidRequest.getUser().getExt().put(CONSENT, userConsentString);
            }
        }
    }

    private void appendCcpaParameter(BidRequest bidRequest) {
        String usPrivacyString = userConsentManager.getUsPrivacyString();

        if (!Utils.isBlank(usPrivacyString)) {
            bidRequest.getRegs().getExt().put(US_PRIVACY, usPrivacyString);
        }
    }

    private void appendCoppaParameter(BidRequest bidRequest) {
        Boolean subjectToCoppa = userConsentManager.getSubjectToCoppa();
        if (subjectToCoppa != null) {
            bidRequest.getRegs().getExt().put(COPPA_SUBJECT, subjectToCoppa ? 1 : 0);
        }
    }

    private void appendGppParameter(BidRequest bidRequest) {
        String gppString = userConsentManager.getRealGppString();
        if (gppString != null) {
            bidRequest.getRegs().setGppString(gppString);
        }

        String gppSid = userConsentManager.getRealGppSid();
        if (gppSid != null) {
            bidRequest.getRegs().setGppSid(gppSid);
        }
    }

}
