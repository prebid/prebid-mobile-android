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

package org.prebid.mobile.rendering.views.webview.mraid;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class ViewsTest {

    private Context context;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void findFirstDescendantOfType_nestedMatch_isFound() {
        FrameLayout root = new FrameLayout(context);
        LinearLayout container = new LinearLayout(context);
        TextView target = new TextView(context);

        root.addView(container);
        container.addView(target);

        assertEquals(target, Views.findFirstDescendantOfType(root, TextView.class));
    }

    @Test
    public void findFirstDescendantOfType_noMatch_isNull() {
        FrameLayout root = new FrameLayout(context);
        root.addView(new LinearLayout(context));

        assertNull(Views.findFirstDescendantOfType(root, TextView.class));
    }

    @Test
    public void findFirstDescendantOfType_nullOrLeafRoot_isNull() {
        assertNull(Views.findFirstDescendantOfType(null, TextView.class));
        assertNull(Views.findFirstDescendantOfType(new TextView(context), TextView.class));
    }

    @Test
    public void findFirstDescendantOfType_rootItself_isNotAMatch() {
        FrameLayout root = new FrameLayout(context);

        assertNull(Views.findFirstDescendantOfType(root, FrameLayout.class));
    }

    @Test
    public void findFirstDescendantOfType_depthFirst_prefersTheDeeperEarlierBranch() {
        FrameLayout root = new FrameLayout(context);
        LinearLayout firstBranch = new LinearLayout(context);
        TextView deepMatch = new TextView(context);
        TextView shallowMatch = new TextView(context);

        root.addView(firstBranch);
        firstBranch.addView(deepMatch);
        root.addView(shallowMatch);

        assertEquals(deepMatch, Views.findFirstDescendantOfType(root, TextView.class));
    }

    @Test
    public void findFirstDescendantOfType_matchesSubtypes() {
        FrameLayout root = new FrameLayout(context);
        Button button = new Button(context);
        root.addView(button);

        assertEquals(button, Views.findFirstDescendantOfType(root, TextView.class));
    }

}
