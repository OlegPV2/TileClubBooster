package com.oleg.tileclubbooster.autoclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.accessibility.AccessibilityEvent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.oleg.tileclubbooster.App;
import com.oleg.tileclubbooster.OverlayService;
import com.oleg.tileclubbooster.R;
import com.oleg.tileclubbooster.util.GameJSON;
import com.oleg.tileclubbooster.util.ToastUtils;

public class TapAccessibilityService extends AccessibilityService {
	public static final String ACTION = "action";
	public static final String PLAY = "play";
	public static final String STOP = "stop";

	private Handler mHandler;
	private int mX;
	private int mY;
	private int mInterval;

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread handlerThread = new HandlerThread("tap-handler");
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getStringExtra(ACTION);
			if (PLAY.equals(action)) {
				mX = intent.getIntExtra("x", 0);
				mY = intent.getIntExtra("y", 0);
				mInterval = intent.getIntExtra("interval", 3000);
				if (myRunnable == null) {
					myRunnable = new myRunnable();
				}
				mHandler.post(myRunnable);
				ToastUtils.shortCall(R.string.autoclick_enabled);
			} else if (STOP.equals(action)) {
				mHandler.removeCallbacksAndMessages(null);
				ToastUtils.shortCall(R.string.autoclick_disabled);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
	}

	@Override
	public void onInterrupt() {

	}

	private void tap(int x, int y) {
		String level = GameJSON.currentLevelFromLevelsData(App.get());
		if (level.equals(App.get().getResources().getString(R.string.button_try_again_text)))
			level = GameJSON.currentLevelFromLevelsData(App.get());
		GameJSON.currentLevelStatusPatch(App.get(), level);
		Path swipePath = new Path();
		swipePath.moveTo(x, y);
		swipePath.lineTo(x, y);
		GestureDescription.Builder gBuilder = new GestureDescription.Builder();
		gBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1));
		String finalLevel = level;
		dispatchGesture(gBuilder.build(), new GestureResultCallback() {
			@Override
			public void onCompleted(GestureDescription gestureDescription) {
				super.onCompleted(gestureDescription);
				sendUpdateTextToButton(finalLevel);//GameJSON.currentLevel(App.get()));
				mHandler.postDelayed(myRunnable, mInterval);
			}

			@Override
			public void onCancelled(GestureDescription gestureDescription) {
				super.onCancelled(gestureDescription);
			}
		}, null);
	}

	private myRunnable myRunnable;

	private class myRunnable implements Runnable {

		@Override
		public void run() {
			tap(mX, mY);
		}
	}

	private void sendUpdateTextToButton (String text) {
		Intent intent = new Intent(OverlayService.ACTION_BROADCAST);
		intent.putExtra(OverlayService.UPDATE_TEXT, text);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

}
