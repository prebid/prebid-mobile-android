package org.prebid.mobile.rendering.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.reflection.sdk.ManagersResolverReflection;
import org.prebid.mobile.reflection.sdk.UserConsentManagerReflection;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserConsentUtilsTest {

    private Context context;
    private SharedPreferences sharedPreferences = mock(SharedPreferences.class);

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        resetAllPreferences();

        ManagersResolver resolver = ManagersResolver.getInstance();
        ManagersResolverReflection.resetManagers(resolver);
    }

    @After
    public void destroy() {
        resetAllPreferences();
    }

    private void resetAllPreferences() {
        UserConsentManager userConsentManager = new UserConsentManager(context);
        sharedPreferences
            .edit()
            .remove(UserConsentManagerReflection.getConstGdpr2Subject(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2Consent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2PurposeConsent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstUsPrivacyString(userConsentManager))
            .apply();
    }

    @Test
    public void subjectToCoppa() {
        UserConsentUtils.tryToSetSubjectToCoppa(false);
        assertNull(UserConsentUtils.tryToGetSubjectToCoppa());

        ManagersResolver.getInstance().prepare(context);

        assertNull(UserConsentUtils.tryToGetSubjectToCoppa());

        UserConsentUtils.tryToSetSubjectToCoppa(false);
        assertEquals(Boolean.FALSE, UserConsentUtils.tryToGetSubjectToCoppa());
    }

    @Test
    public void gdprConsent() {
        UserConsentUtils.tryToSetPrebidGdprConsent("1");
        assertNull(UserConsentUtils.tryToGetGdprConsent());

        ManagersResolver.getInstance().prepare(context);

        assertNull(UserConsentUtils.tryToGetGdprConsent());

        UserConsentUtils.tryToSetPrebidGdprConsent("1");
        assertEquals("1", UserConsentUtils.tryToGetGdprConsent());
    }

    @Test
    public void gdprPurposeConsents() {
        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("1");
        assertNull(UserConsentUtils.tryToGetGdprPurposeConsents());

        ManagersResolver.getInstance().prepare(context);

        assertNull(UserConsentUtils.tryToGetGdprPurposeConsents());

        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("1");
        assertEquals("1", UserConsentUtils.tryToGetGdprPurposeConsents());
    }

    @Test
    public void tryToGetDeviceAccessConsent() {
        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("1");

        assertNull(UserConsentUtils.tryToGetDeviceAccessConsent());

        ManagersResolver.getInstance().prepare(context);

        assertTrue(UserConsentUtils.tryToGetDeviceAccessConsent());

        UserConsentUtils.tryToSetPrebidSubjectToGdpr(true);
        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("0");
        assertFalse(UserConsentUtils.tryToGetDeviceAccessConsent());
    }

}