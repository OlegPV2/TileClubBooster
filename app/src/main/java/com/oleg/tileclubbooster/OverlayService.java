package com.oleg.tileclubbooster;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.oleg.tileclubbooster.constant.ServiceIntent;
import com.oleg.tileclubbooster.util.FloatingView;
import com.oleg.tileclubbooster.util.GameJSON;

public class OverlayService extends Service {
	public static final String UPDATE_TEXT = "button_text";
	public static final String ACTION_BROADCAST = "textBroadcast";

	private FloatingView floatingView;
	private Handler handler;
	private Context context;
	private int currentLevelCounter;
	private boolean useDataLevelNumber = false;


	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		floatingView = new FloatingView(this);
		HandlerThread handlerThread = new HandlerThread("level-handler");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		runnable = new LevelRunnable();
		handler.post(runnable);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		floatingView.show();
		if (intent != null) {
			String action = intent.getStringExtra(ServiceIntent.ACTION);
			if (ServiceIntent.DOUBLE_CLICK.equals(action)) {
				handler.post(runnable);
			} else if (ServiceIntent.SINGLE_CLICK.equals(action)) {
				if (useDataLevelNumber) {
					GameJSON.currentLevelStatusPatch(context, String.valueOf(currentLevelCounter));
					currentLevelCounter++;
					floatingView.updateText(String.valueOf(currentLevelCounter));
				} else {
					String level = GameJSON.getCurrentLevel(context);
					floatingView.updateText(level);
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
//		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (handler != null) {
			handler.removeCallbacksAndMessages(runnable);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private LevelRunnable runnable;

	private class LevelRunnable implements Runnable {
		@Override
		public void run() {
			currentLevelCounter = Integer.parseInt(GameJSON.getCurrentLevelFromLevelsData(context));
//			floatingView.updateText(String.valueOf(currentLevelCounter));
			useDataLevelNumber = true;
			currentLevelCounter--;
			handler.removeCallbacksAndMessages(runnable);
		}
	}
}