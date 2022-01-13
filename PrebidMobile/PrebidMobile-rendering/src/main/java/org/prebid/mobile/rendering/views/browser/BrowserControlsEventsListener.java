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