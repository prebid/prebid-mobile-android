package org.prebid.mobile;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class JsonUserDataTest {

    @Test
    public void putUsersDataListToRequestParams_addElementsToOutsideList_elementsAddedToRequestParams() {
        ArrayList<DataObject> userDataObjects = new ArrayList<>();
        DataObject.SegmentObject segment = new DataObject.SegmentObject();
        segment.setId("segmentId1");
        segment.setName("segmentName1");
        segment.setValue("segmentValue1");
        DataObject dataObject = new DataObject();
        dataObject.setId("testId1");
        dataObject.setName("testName1");
        dataObject.addSegment(segment);
        userDataObjects.add(dataObject);

        RequestParams requestParams = new RequestParams(
                "configId", AdType.BANNER,
                null, null, null, null, null, null, null, null,
                userDataObjects
        );

        assertEquals(1, requestParams.getUserDataObjects().size());
        assertThat(requestParams.getUserDataObjects(), Matchers.contains(dataObject));

        DataObject secondObject = new DataObject();
        secondObject.setId("secondId");
        userDataObjects.add(secondObject);

        assertEquals(2, requestParams.getUserDataObjects().size());
        assertThat(requestParams.getUserDataObjects(), hasItems(
                dataObject,
                secondObject
        ));
    }

}
