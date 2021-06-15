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

package org.prebid.mobile.rendering.video.vast;


public interface VASTInterface {
	
	/*	These are handled by the SDK:
	 
	 	/**
	 *  Per IAB: this event is used to indicate that an individual creative within the ad was loaded and playback
	 *	began. As with creativeView, this event is another way of tracking creative playback.
	 
		public void creativeView();
		
		public void firstQuartile();
		public void midPoint();
		public void thirdQuartile();
		public void complete();
	*/
	
	/**
	 * this event is used to indicate that an individual creative within the ad was loaded and playback
	 * began. As with creativeView, this event is another way of tracking creative playback.
	 * 
	 */
	//public void start();
	
	/**
	 * Per IAB: the user activated the resume control after the creative had been stopped or paused.
	 */
    void resume();
	
	/**
	 * Per IAB: the user clicked the pause control and stopped the creative.
	 */
    void pause();
	
	/**
	 * Per IAB: the user activated a control to expand the creative.
	 */
    void expand();
	
	/**
	 * Per IAB: the user activated a control to extend the video player to the edges of the viewer’s
	 * screen.	
	 */
    void fullScreen();
	
	/**
	 * the user activated a control to reduce the creative to its original dimensions.
	 */
    void collapse();
	
	/**
	 * the user activated the control to reduce video player size to original dimensions.
	 */
    void exitFullScreen();
	
	/**
	 * the user activated the mute control and muted the creative.
	 */
    void mute();
	
	/**
	 * the user activated the mute control and unmuted the creative.
	 */
    void unmute();
	
	/**
	 * the user clicked the close button on the creative.
	 */
    void close();
	
	/**
	 * the user clicked the close button on the creative. The name of this event distinguishes it
	 * from the existing “close” event described in the 2008 IAB Digital Video In-Stream Ad Metrics
	 * Definitions, which defines the “close” metric as applying to non-linear ads only. The “closeLinear” event
	 * extends the “close” event for use in Linear creative.
	 */
    void closeLinear();
	
	/**
	 * the user activated a skip control to skip the creative, which is a different control than the one
	 * used to close the creative.
	 */
    void skip();
	
	/**
	 * the user activated the rewind control to access a previous point in the creative timeline.
	 */
    void rewind();
	
	/**
	 * the user simply touched the video player in a non-specific region of the player (touched an empty area of the video, not a widget or button).
	 */
    void touch();
	
	/**
	 * the user made an orientation change by rotating the device.  The orientation of Configuration.ORIENTATION_LANDSCAPE or Configuration.ORIENTATION_PORTRAIT is passed in.
	 */
    void orientationChanged(int orientation);
	
	/**
	 * the user made a window focus change, such as leaving the activity, pressing back, invoking Recents, or returning from one of these states back to the video player.  
	 * This will allow for internal threads to pause and resume correctly.
	 */
    void onWindowFocusChanged(boolean hasFocus);
	
	
	/* Not supported yet, 3.0, Ad Server would have to support it
	/**
	 * the creative played for a duration at normal speed that is equal to or greater than the
	 * value provided in an additional attribute for offset. Offset values can be time in the format
	 * HH:MM:SS or HH:MM:SS.mmm or a percentage value in the format n%. Multiple progress events with
	 * different values can be used to track multiple progress points in the Linear creative timeline.
	 *
	public void progress();
	*/

}
