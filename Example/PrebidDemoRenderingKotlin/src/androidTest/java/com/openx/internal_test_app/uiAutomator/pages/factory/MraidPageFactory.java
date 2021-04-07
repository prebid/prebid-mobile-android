package com.openx.internal_test_app.uiAutomator.pages.factory;

import androidx.test.uiautomator.UiDevice;

import com.openx.internal_test_app.uiAutomator.pages.PageFactory;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.Mraid3LoadAndEventsPage;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.Mraid3ResizeNegativePage;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.Mraid3TestMethods;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.Mraid3TestProperties;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.Mraid3ViewabilityCompliancePage;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.MraidExpand1Page;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.MraidExpand2Page;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.MraidResizePage;
import com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds.MraidResizeWithErrorsPage;

public class MraidPageFactory extends PageFactory {

    public MraidPageFactory(UiDevice device) {
        super(device);
    }

    public MraidExpand2Page goToMraidExpand2(String exampleName) {
        findListItem(exampleName);
        return new MraidExpand2Page(device);
    }

    public MraidResizeWithErrorsPage goToMraidResizeErrors(String exampleName) {
        findListItem(exampleName);
        return new MraidResizeWithErrorsPage(device);
    }

    public Mraid3TestMethods goToMraid3TestMethods(String exampleName) {
        findListItem(exampleName);
        return new Mraid3TestMethods(device);
    }

    public Mraid3TestProperties goToMraid3TestProperties(String exampleName) {
        findListItem(exampleName);
        return new Mraid3TestProperties(device);
    }

    public Mraid3ViewabilityCompliancePage goToMraid3ViewabilityCompliancePage(String exampleName) {
        findListItem(exampleName);
        return new Mraid3ViewabilityCompliancePage(device);
    }

    public Mraid3ResizeNegativePage goToMraid3ResizeNegativePage(String exampleName) {
        findListItem(exampleName);
        return new Mraid3ResizeNegativePage(device);
    }

    public Mraid3LoadAndEventsPage goToMraid3LoadAndEvents(String exampleName) {
        findListItem(exampleName);
        return new Mraid3LoadAndEventsPage(device);
    }

    public MraidExpand1Page goToMraidExpand(String exampleName) {
        findListItem(exampleName);
        return new MraidExpand1Page(device);
    }

    public MraidResizePage goToMraidResize(String exampleName) {
        findListItem(exampleName);
        return new MraidResizePage(device);
    }
}
