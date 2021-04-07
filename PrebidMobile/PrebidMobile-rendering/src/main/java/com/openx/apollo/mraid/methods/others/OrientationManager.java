package com.openx.apollo.mraid.methods.others;

import android.content.pm.ActivityInfo;

public class OrientationManager
{
	public enum ForcedOrientation {
		portrait(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
		landscape(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
		none(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		private final int mActivityInfoOrientation;

		ForcedOrientation(final int activityInfoOrientation) {
			mActivityInfoOrientation = activityInfoOrientation;
		}

		public int getActivityInfoOrientation() {
			return mActivityInfoOrientation;
		}
	}
}
