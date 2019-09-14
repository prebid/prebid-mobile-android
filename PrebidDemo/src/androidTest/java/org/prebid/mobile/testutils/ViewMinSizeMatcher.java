package org.prebid.mobile.testutils;

import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ViewMinSizeMatcher extends TypeSafeMatcher<View> {
    private final int expectedMinWith;
    private final int expectedMinHeight;

    public ViewMinSizeMatcher(int expectedMinWith, int expectedMinHeight) {
        super(View.class);
        this.expectedMinWith = expectedMinWith;
        this.expectedMinHeight = expectedMinHeight;
    }

    @Override
    protected boolean matchesSafely(View target) {
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
