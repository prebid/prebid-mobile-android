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

package org.prebid.mobile.rendering.utils.exposure;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.test.filters.LargeTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.webview.mraid.Views;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@LargeTest
@Config(sdk = 23, qualifiers = "w800dp-h800dp-xhdpi")
public class ViewExposureCheckerTest {

    private Activity activity;
    private FrameLayout container;
    private ViewExposureChecker viewExposureChecker;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class)
                              .setup()
                              .create()
                              .visible()
                              .resume()
                              .windowFocusChanged(true)
                              .get();
        container = new FrameLayout(activity);
        container.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        activity.setContentView(container);
        viewExposureChecker = new ViewExposureChecker();
    }

    @Test
    public void whenNoObstruction_FullyVisible() {
        FrameLayout grandParent = new FrameLayout(activity);
        FrameLayout parent = new FrameLayout(activity);
        View view = new View(activity);

        container.addView(grandParent);
        grandParent.addView(parent);
        parent.addView(view);

        grandParent.layout(20, 20, 420, 820);
        parent.layout(20, 20, 320, 720);
        view.layout(20, 20, 220, 220);

        ViewExposure resultExposure = viewExposureChecker.exposure(view);
        assertEquals(new ViewExposure(1, new Rect(0, 0, 200, 200), new ArrayList<>()), resultExposure);
    }

    // MARK: - Single obstruction
    //
    //      0   10   20   30   40   50   60
    //     ][___][___][___][___][___][___][__
    //   0l +-----------+
    //    l |root       |
    //    l |   +----------------------+
    //    l |   |grandparent           |
    //  10L_|   |           +------+   |
    //    l |   |           |parent|   |
    //    l |   |           |      |   |
    //    l |   | +-----------+    |   |
    //    l |   | |obstruction|    |   |
    //  20L_|   +-|.........: |    |---+
    //    l |     |     :   : |    |
    //    l |     |     :   : |    |
    //    l |     |   +.......|-+  |
    //    l |     |   :view   | |  |
    //  30L_|     +-----------+ |  |
    //    l |         |         |  |
    //    l |         +---------+  |
    //    l |           |   |      |
    //    l |           |   +------+
    //  40L_|           |
    //    l +-----------+
    //
    // no clipping
    // +---------------------+-------+-------+-------+-----------+---------+-------+
    // |         view        | p-glob| p-loc |  size | obstructed|unexposed|exposed|
    // +---------------------+-------+-------+-------+-----+-----+---------+-------+
    // | *━┯ window          |  0, 0 |  0, 0 | 56x42 | N/A | N/A |   N/A   |  N/A  |
    // |   ┡━┯ root          |  1, 1 |  1, 1 | 24x40 |12,14|12x14|   7/40  | 33/40 |
    // |   │ ┗━┯ grandparent |  9, 5 |  8, 4 | 46x14 | 4,10|24x 4|  24/161 |137/161|
    // |   │   ┗━┯ parent    | 33, 9 | 24, 4 | 14x28 | 0, 6| 4x14|   1/7   |  6/7  |
    // |   │     ┗━━ view    | 21,25 |-12,16 | 20x 8 | 0, 0|16x 4|   2/5   |  3/5  |
    // |   ┗━━ obstruction   | 13,15 | 13,15 | 24x14 | N/A | N/A |   N/A   |  N/A  |
    // +---------------------+-------+-------+-------+-----+-----+---------+-------+
    //
    // unexposed = obstructed.size.area / size.area
    // exposed = 1 - unexposed
    //
    // assertEquals(1 - 24/161.0, 137/161.0) //  ("0.8509316770186335") is not equal to ("0.8509316770186336")
    //
    // => use exact values from 'exposed' to build ViewExposure, otherwise 'isEqual' might fail due to rounding errors
    //
    // root.clipToBounds = true
    //   => parent -- clipped out
    // +-------------+-------+-----+-----+-------+-----+-----+-------+-------+
    // |     view    |  size |  visible  | -area | obstructed| -area |exposed|
    // +-------------+-------+-----+-----+-------+-----+-----+-------+-------+
    // | grandparent | 46x14 | 0, 0|16x14|  8/23 | 4,10|12x 4| 12/161| 44/161|
    // | view        | 20x 8 | 0, 4| 4x 8|  1/5  | 0, 0| 0x 0|  1/10 |  1/10 |
    // +-------------+-------+-----+-----+-------+-----+-----+-------+-------+
    //
    // parent.clipToBounds = true
    // +-------------+-------+-----+-----+-------+-----+-----+-------+-------+
    // |     view    |  size |  visible  | -area | obstructed| -area |exposed|
    // +-------------+-------+-----+-----+-------+-----+-----+-------+-------+
    // | view        | 20x 8 |12, 0| 8x 8|  2/5  |12, 0| 4x 4|  1/10 |  3/10 |
    // +-------------+-------+-----+-----+-------+-----+-----+-------+-------+
    //
    // move obstruction to background
    // +-------------+-------+-----+-----+-------+
    // |     view    |  size |  visible  | -area |
    // +-------------+-------+-----+-----+-------+
    // | obstruction | 24x14 |12, 4| 8x 6|  1/7  |
    // +-------------+-------+-----+-----+-------+
    //

    @Test
    public void whenSingleObstruction_DetectObstruction() {
        FrameLayout root = new FrameLayout(activity);
        FrameLayout grandParent = new FrameLayout(activity);
        FrameLayout parent = new FrameLayout(activity);
        View view = new View(activity);
        View obstruction = new View(activity);

        final ColorDrawable background = mock(ColorDrawable.class);
        when(background.getAlpha()).thenReturn(255);
        parent.setBackground(background);
        grandParent.setBackground(background);
        root.setBackground(background);

        container.addView(root);
        root.addView(grandParent);
        grandParent.addView(parent);
        parent.addView(view);
        container.addView(obstruction);

        root.layout(10, 10, 250, 410);
        grandParent.layout(80, 40, 540, 180);
        parent.layout(240, 40, 380, 320);
        view.layout(-120, 160, 80, 240);
        obstruction.layout(130, 150, 370, 290);

        root.setClipChildren(false);
        grandParent.setClipChildren(false);
        parent.setClipChildren(false);

        assertEquals(new ViewExposure(33 / 40.0f,
                                      new Rect(0, 0, 240, 400),
                                      Collections.singletonList(new Rect(120, 140, 240, 280))),
                     getViewExposure(root));

        assertEquals(new ViewExposure(137 / 161.0f,
                                      new Rect(0, 0, 460, 140),
                                      Collections.singletonList(new Rect(40, 100, 280, 140))),
                     getViewExposure(grandParent));

        assertEquals(new ViewExposure(6 / 7.0f,
                                      new Rect(0, 0, 140, 280),
                                      Collections.singletonList(new Rect(0, 60, 40, 200))),
                     getViewExposure(parent));

        assertEquals(new ViewExposure(3 / 5.0f,
                                      new Rect(0, 0, 200, 80),
                                      Collections.singletonList(new Rect(0, 0, 160, 40))),
                     getViewExposure(view));

        //// table 2
        root.setClipChildren(true);

        assertEquals(new ViewExposure(44 / 161.0f,
                                      new Rect(0, 0, 160, 140),
                                      Collections.singletonList(new Rect(40, 100, 160, 140))),
                     getViewExposure(grandParent));

        assertEquals(new ViewExposure(0,
                                      new Rect(0, 0, 0, 0),
                                      null),
                     getViewExposure(parent));

        assertEquals(new ViewExposure(1 / 10.0f,
                                      new Rect(0, 40, 40, 80),
                                      new ArrayList<>()),
                     getViewExposure(view));

        /// table 3
        root.setClipChildren(false);
        parent.setClipChildren(true);
        assertEquals(new ViewExposure(3 / 10.0f,
                                      new Rect(120, 0, 200, 80),
                                      Collections.singletonList(new Rect(120, 0, 160, 40))),
                     getViewExposure(view));

        ///table 4
        parent.setClipChildren(false);
        Views.removeFromParent(obstruction);
        container.addView(obstruction, 0);
        root.layout(10, 10, 250, 410);
        grandParent.layout(80, 40, 540, 180);
        parent.layout(240, 40, 380, 320);
        view.layout(-120, 160, 80, 240);
        obstruction.layout(130, 150, 370, 290);

        assertEquals(new ViewExposure(1 / 7.0f,
                                      new Rect(120, 40, 200, 100),
                                      Collections.emptyList()),
                     getViewExposure(obstruction));
    }

    // MARK: - Composite hierarchy
    //
    //      0   10   20   30   40   50   60
    //     ][___][___][___][___][___][___][__
    //   0l +-----------------------------+
    //    l |parent   +-----------------+ |
    //    l |         |brother          | |
    //    l |  +------|.............+   | |
    //  10L_|  |adView|             :   | |
    //    l |  |      +-----------------+ |
    //    l |  | +-----+            |     |
    //    l |  | |X-btn|            |     |
    //    l |  | +-----+  +-----------+   |
    //  20L_|  |          |uncle    : |   |
    //    l |  |          |         : |   |
    //    l |  |          |         : |   |
    //    l |  |          +-----------+   |
    //    l |  |                    |     |
    //  30L_|+--------------------------+ |
    //    l ||aunt    +---------+   :   | |
    //    l || :      |cousin   |   :   | |
    //    l || +......|.........|...+   | |
    //    l ||        +---------+       | |
    //  40L_|+--------------------------+ |
    //    l +-----------------------------+
    //
    // no clipping
    // +---------------------+-------+-------+-------+-----------+-----------+-----------+---------+-----------+---------+
    // |         view        | p-glob| p-loc |  size | obstructed| obstructed| obstructed|unexposed|  visible  | exposed |
    // +---------------------+-------+-------+-------+-----+-----+-----+-----+-----+-----+---------+-----+-----+---------+
    // | *━┯ window          |  0, 0 |  0, 0 | 62x42 |     :     |     :     |     :     |   N/A   |    N/A    |   N/A   |
    // |   ┡━┯ parent        |  1, 1 |  1, 1 | 60x40 | 2,28:54x10|28,16:24x 8|     :     | 732/2400| 0, 0:60x40|1668/2400|
    // |   │ ┡━━ adView      |  7, 7 |  6, 6 | 42x28 |22,10:20x 8|14, 0:28x 4| 4, 6:12x 4| 320/1176| 0, 0:42x22| 604/1176|
    // |   │ ┗━━ brother     | 21, 3 | 20, 2 | 36x 8 |     :     |     :     |     :     |   N/A   | 0, 0:36x 8|    1    |
    // |   │ ┗━━ X-btn       | 11,13 | 10,12 | 12x 4 |     :     |     :     |     :     |   N/A   | 0, 0:12x 4|    1    |
    // |   ┡━━ uncle         | 29,17 | 29,17 | 24x 8 |     :     |     :     |     :     |   N/A   | 0, 0:24x 8|    1    |
    // |   ┗━┯ aunt          |  3,29 |  3,29 | 54x10 |     :     |     :     |     :     |   N/A   | 0, 0:54x10|    1    |
    // |     ┗━━ cousin      | 21,31 | 18, 2 | 20x 6 |     :     |     :     |     :     |   N/A   | 0, 0:20x 6|    1    |
    // +---------------------+-------+-------+-------+-----+-----+-----+-----+-----+-----+---------+-----+-----+---------+
    //

    @Test
    public void whenMultipleObstructions_DetectObstructions() {
        FrameLayout parent = new FrameLayout(activity);
        View adView = new View(activity);
        View brother = new View(activity);
        View xBtn = new View(activity);
        View uncle = new View(activity);
        FrameLayout aunt = new FrameLayout(activity);
        View cousin = new View(activity);

        final ColorDrawable background = mock(ColorDrawable.class);
        when(background.getAlpha()).thenReturn(255);
        parent.setBackground(background);
        aunt.setBackground(background);

        container.setClipChildren(false);
        parent.setClipChildren(false);
        aunt.setClipChildren(false);

        container.addView(parent);
        parent.addView(adView);
        parent.addView(brother);
        parent.addView(xBtn);
        container.addView(uncle);
        container.addView(aunt);
        aunt.addView(cousin);

        parent.layout(10, 10, 610, 410);
        adView.layout(60, 60, 480, 340);
        brother.layout(200, 20, 560, 100);
        xBtn.layout(100, 120, 220, 160);
        uncle.layout(290, 170, 530, 250);
        aunt.layout(30, 290, 570, 390);
        cousin.layout(180, 20, 380, 80);

        ArrayList<Rect> occlusionsList = new ArrayList<>();

        occlusionsList.add(new Rect(20, 280, 560, 380));
        occlusionsList.add(new Rect(280, 160, 520, 240));
        assertEquals(new ViewExposure(1668 / 2400.0f,
                                      new Rect(0, 0, 600, 400),
                                      occlusionsList),
                     getViewExposure(parent));

        occlusionsList.clear();
        occlusionsList.add(new Rect(220, 100, 420, 180));
        occlusionsList.add(new Rect(140, 0, 420, 40));
        occlusionsList.add(new Rect(40, 60, 160, 100));

        assertEquals(new ViewExposure(604 / 1176.0f,
                                      new Rect(0, 0, 420, 220),
                                      occlusionsList),
                     getViewExposure(adView));

        assertEquals(new ViewExposure(1,
                                      new Rect(0, 0, 360, 80),
                                      Collections.emptyList()),
                     getViewExposure(brother));

        assertEquals(new ViewExposure(1,
                                      new Rect(0, 0, 120, 40),
                                      Collections.emptyList()),
                     getViewExposure(xBtn));

        assertEquals(new ViewExposure(1,
                                      new Rect(0, 0, 240, 80),
                                      Collections.emptyList()),
                     getViewExposure(uncle));

        assertEquals(new ViewExposure(1,
                                      new Rect(0, 0, 540, 100),
                                      Collections.emptyList()),
                     getViewExposure(aunt));

        assertEquals(new ViewExposure(1,
                                      new Rect(0, 0, 200, 60),
                                      Collections.emptyList()),
                     getViewExposure(cousin));
    }

    @Test
    public void whenShouldCollectObstructionWithTransparentBgAndNonTransparentFg_ReturnTrue() {
        FrameLayout child = new FrameLayout(activity);
        final ColorDrawable foreground = mock(ColorDrawable.class);
        final ColorDrawable background = mock(ColorDrawable.class);
        when(foreground.getAlpha()).thenReturn(255);
        when(background.getAlpha()).thenReturn(0);

        child.setForeground(foreground);
        child.setBackground(background);
        assertTrue(viewExposureChecker.shouldCollectObstruction(child));
    }

    @Test
    public void whenShouldCollectObstructionWithTransparentFgAndNonTransparentBg_ReturnTrue() {
        FrameLayout child = new FrameLayout(activity);
        final ColorDrawable foreground = mock(ColorDrawable.class);
        final ColorDrawable background = mock(ColorDrawable.class);
        when(foreground.getAlpha()).thenReturn(0);
        when(background.getAlpha()).thenReturn(255);

        child.setForeground(foreground);
        child.setBackground(background);
        assertTrue(viewExposureChecker.shouldCollectObstruction(child));
    }

    @Test
    public void whenShouldCollectObstructionWithNonViewGroup_ReturnTrue() {
        View child = new View(activity);
        assertTrue(viewExposureChecker.shouldCollectObstruction(child));
    }

    @Test
    public void whenShouldCollectObstructionWithTransparentBgAndTransparentFg_ReturnFalse() {
        FrameLayout child = new FrameLayout(activity);
        final ColorDrawable foreground = mock(ColorDrawable.class);
        final ColorDrawable background = mock(ColorDrawable.class);
        when(foreground.getAlpha()).thenReturn(0);
        when(background.getAlpha()).thenReturn(0);

        child.setForeground(foreground);
        child.setBackground(background);
        assertFalse(viewExposureChecker.shouldCollectObstruction(child));
    }

    private ViewExposure getViewExposure(View view) {
        return viewExposureChecker.exposure(view);
    }
}