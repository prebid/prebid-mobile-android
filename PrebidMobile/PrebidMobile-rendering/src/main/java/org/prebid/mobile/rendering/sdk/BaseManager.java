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

package org.prebid.mobile.rendering.sdk;

import android.content.Context;

import java.lang.ref.WeakReference;

public class BaseManager implements Manager
{
	private WeakReference<Context> mContextReference;
	private boolean mIsInit;

	/**
	 * Check initialization of manager.
	 * 
	 * @return true, if manager was initialized
	 */
	@Override
	public boolean isInit()
	{
		return mIsInit;
	}

	/**
	 * Initialize manager.
	 * 
	 * @param context
	 *            the context for which manager will be initialized.
	 */
	@Override
	public void init(Context context)
	{
		if (context != null)
		{
			mContextReference = new WeakReference<>(context);
			mIsInit = true;
		}
	}

	/**
	 * Get the context for which manager was initialized.
	 * 
	 * @return the context
	 */
	@Override
	public Context getContext()
	{
		if (mContextReference != null) {
			return mContextReference.get();
		}
		return null;
	}

	/**
	 * Dispose manager and release all necessary resources.
	 * 
	 */
	@Override
	public void dispose()
	{
		mIsInit = false;
		mContextReference = null;
	}
}
