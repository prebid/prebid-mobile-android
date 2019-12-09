/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class StorageUtilsTest extends BaseSetup {

    @Test
    public void testIABUSPrivacy_StringKey() throws Exception {
        assertEquals("IABUSPrivacy_String", StorageUtils.IABUSPrivacy_StringKey);

    }

    @Test
    public void testIABConsent_SubjectToGDPRKey() throws Exception {
        assertEquals("IABConsent_SubjectToGDPR", StorageUtils.IABConsent_SubjectToGDPRKey);

    }

    @Test
    public void testIABConsent_ConsentStringKey() throws Exception {
        assertEquals("IABConsent_ConsentString", StorageUtils.IABConsent_ConsentStringKey);

    }
    @Test
    public void testPBConsent_SubjectToGDPRKey() throws Exception {
        assertEquals("Prebid_GDPR", StorageUtils.PBConsent_SubjectToGDPRKey);

    }
    @Test
    public void testPBConsent_ConsentStringKey() throws Exception {
        assertEquals("Prebid_GDPR_consent_strings", StorageUtils.PBConsent_ConsentStringKey);

    }

}
