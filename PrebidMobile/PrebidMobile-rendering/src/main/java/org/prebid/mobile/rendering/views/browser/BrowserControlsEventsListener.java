package org.prebid.mobile.rendering.views.browser;


/**
 * The listener interface for receiving browser controls events. The class that
 * is interested in processing a browser controls events implements this
 * interface, and pass this object into constructor of that class. When the
 * browser controls event occurs, that object's appropriate method is invoked.
 * 
 */
public interface BrowserControlsEventsListener
{

	/**
	 * Close browser.
	 */
	void closeBrowser();

	/**
	 * Perform browser "back" operation.
	 */
	void onGoBack();

	/**
	 * Perform browser "forward" operation.
	 */
	void onGoForward();

	/**
	 * Perform browser "relaod" operation.
	 */
	void onRelaod();

	/**
	 * Check state if browser can navigate back.
	 * 
	 * @return true, if successful
	 */
	boolean canGoBack();

	/**
	 * Check state if browser can navigate forward.
	 * 
	 * @return true, if successful
	 */
	boolean canGoForward();

	/**
	 * Get the current browser URL.
	 * 
	 * @return the current URL
	 */
	String getCurrentURL();
}