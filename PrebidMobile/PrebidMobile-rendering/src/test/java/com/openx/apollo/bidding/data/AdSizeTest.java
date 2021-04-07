package com.openx.apollo.bidding.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdSizeTest {

    @Test
    public void whenInit_FieldsAssignedCorrectly() {
        AdSize adSize = new AdSize(1, 2);
        assertEquals(adSize.width, 1);
        assertEquals(adSize.height, 2);
    }

    @Test
    public void whenFieldsAreEqual_ObjectsAreEqual() {
        AdSize adSize = new AdSize(1, 1);
        AdSize newAdSize = new AdSize(1, 1);
        assertEquals(adSize, newAdSize);
    }
}