package com.openx.apollo.sdk;

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
