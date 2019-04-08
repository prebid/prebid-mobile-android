package org.prebid.mobile.testutils;

import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ViewMinSizeMatcher extends TypeSafeMatcher<View> {
    // field to store values
    private final int expectedMinWith;
    private final int expectedMinHeight;

    public ViewMinSizeMatcher(int expectedMinWith, int expectedMinHeight) {
        super(View.class);
        this.expectedMinWith = expectedMinWith;
        this.expectedMinHeight = expectedMinHeight;
    }

    @Override
    protected boolean matchesSafely(View target) {
        // stop executing if target is not textview
//            if (!(target instanceof TextView)){
//                return false;
//            }
        // target is a text view so apply casting then retrieve and test the desired value
//            TextView targetEditText = (TextView) target;

        int targetWidth = target.getWidth();
        int targetHeight = target.getHeight();
        return targetWidth >= expectedMinWith && targetHeight >= expectedMinHeight;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with MinSizeMatcher: ");
        description.appendValue(expectedMinWith + "x" + expectedMinHeight);
    }
}
