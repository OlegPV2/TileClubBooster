package com.oleg.tileclubbooster.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by wilburnLee on 2019/4/22.
 */
public class FloatingManager {
	private final WindowManager mWindowManager;
	private static FloatingManager mInstance;

	public static FloatingManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new FloatingManager(context);
		}
		return mInstance;
	}

	private FloatingManager(Context context) {
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	protected void addView(View view, WindowManager.LayoutParams params) {
		try {
			mWindowManager.addView(view, params);
		} catch (Exception e) {
			Log.d("addView", e.toString());
		}
	}

	protected void removeView(View view) {
		try {
			mWindowManager.removeView(view);
		} catch (Exception e) {
			Log.d("removeView", e.toString());
		}
	}

	protected void updateView(View view, WindowManager.LayoutParams params) {
		try {
			mWindowManager.updateViewLayout(view, params);
		} catch (Exception e) {
			Log.d("updateView", e.toString());
		}
	}
}