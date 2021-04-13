package org.prebid.mobile.rendering.mraid.methods;

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidCalendarEventTest {

    public final static String mCalendarFile = "calendar.txt";
    private String mCalendarParameters = null;
    private Activity mTestActivity;

    @Before
    public void setUp() throws Exception {
        mTestActivity = Robolectric.buildActivity(Activity.class).create().get();
        mCalendarParameters = ResourceUtils.convertResourceToString(mCalendarFile);
        ManagersResolver.getInstance().prepare(mTestActivity);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateCalendarEvent() {
        BaseJSInterface mockJs = mock(BaseJSInterface.class);
        MraidCalendarEvent event = new MraidCalendarEvent(mockJs);

        event.createCalendarEvent(mCalendarParameters);
        ShadowActivity shadowActivity = shadowOf(mTestActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertFalse("somecrap.calendar/event".equals(startedIntent.getType()));
        assertEquals("vnd.android.cursor.item/event", startedIntent.getType());
        assertFalse("fail.event.type".equals(startedIntent.getType()));
        assertEquals("", startedIntent.getExtras().get(CalendarContract.Events.TITLE));
        assertTrue(startedIntent.getExtras().get(CalendarContract.Events.DESCRIPTION).equals
            ("mayanApocalypse/End of world"));
    }
}