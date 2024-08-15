package com.oleg.tileclubbooster;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.oleg.tileclubbooster.autoclick.TapAccessibilityService;
import com.oleg.tileclubbooster.util.GameJSON;

public class OverlayService extends Service {
	public static final String UPDATE_TEXT = "button_text";
	public static final String ACTION_BROADCAST = "textBroadcast";

	private WindowManager windowManager;
	private Button button;
	private boolean autoclickEnabled = false;

	long lastPressTime;
	boolean mHasDoubleClicked = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onCreate();

		WindowManager.LayoutParams params;

		if (Build.VERSION.SDK_INT >= 26) {
			String CHANNEL_ID = "channel1";
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					"Overlay notification",
					NotificationManager.IMPORTANCE_LOW);

			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.createNotificationChannel(channel);

			Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setContentTitle("Mahjong Club Booster")
					.setContentText("Foreground process")
					.setSmallIcon(R.mipmap.ic_launcher)
					.build();

			startForeground(1, notification);

			params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);
		} else {
			params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_PHONE,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);

		}

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		button = new Button(this);
		button.setBackgroundResource(R.drawable.round_button);
		button.setTextSize(25);
		button.setText(R.string.button_default_text);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						String text = intent.getStringExtra(UPDATE_TEXT);
						button.setText(text);
					}
				}, new IntentFilter(ACTION_BROADCAST)
		);

		params.gravity = Gravity.TOP | Gravity.START;
		params.x = 0;
		params.y = 100;

		windowManager.addView(button, params);

		button.setOnTouchListener(new View.OnTouchListener() {
			private final WindowManager.LayoutParams paramsF = params;
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!autoclickEnabled) switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						long pressTime = System.currentTimeMillis();

						// If double click...
						if (pressTime - lastPressTime <= 500) {
							initiatePopupWindow(v);
							mHasDoubleClicked = true;
						} else {     // If not double click....
							mHasDoubleClicked = false;
						}
						lastPressTime = pressTime;
						initialX = paramsF.x;
						initialY = paramsF.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						break;
					case MotionEvent.ACTION_UP:
						break;
					case MotionEvent.ACTION_MOVE:
						paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
						paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
						windowManager.updateViewLayout(button, paramsF);
						break;
				}
				return false;
			}
		});


		button.setOnClickListener(arg0 -> {
			if (autoclickEnabled) {
				Intent intentAutoclick = new Intent(App.get(), TapAccessibilityService.class);
				intentAutoclick.putExtra(TapAccessibilityService.ACTION, TapAccessibilityService.STOP);
				startService(intentAutoclick);
				button.setBackgroundResource(R.drawable.round_button);
				autoclickEnabled = false;
//				ToastUtils.shortCall(R.string.autoclick_disabled);
			} else {
				String level = GameJSON.currentLevelFromLevelsData(App.get());
				button.setText(level);
				GameJSON.currentLevelStatusPatch(this, level);
			}
		});
		button.setText(GameJSON.currentLevelFromLevelsData(App.get()));

		return START_STICKY;
	}

	private int measureContentWidth(String[] listAdapter) {
		int maxWidth = 0;
		Paint p = new Paint();
		Rect bounds = new Rect();
		for (String s : listAdapter) {
			p.getTextBounds(s, 0, s.length(), bounds);
			maxWidth = Math.max(bounds.width(), maxWidth);
		}
		return maxWidth;
	}

	private void initiatePopupWindow(View anchor) {
		String[] menu = new String[]{
				getString(R.string.popupmenu_item1)
		};
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		ListPopupWindow listPopupWindow = new ListPopupWindow(this);
		listPopupWindow.setAnchorView(anchor);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.popup_menu_item, menu);
		Point p = new Point();
		display.getSize(p);
		listPopupWindow.setWidth(measureContentWidth(menu) * 2);
		listPopupWindow.setAdapter(adapter);
		listPopupWindow.setModal(true);
		listPopupWindow.setOnItemClickListener((arg0, view, position, id3) -> {
			if (position == 0) {
				String level = GameJSON.currentLevelFromLevelsData(App.get());
				button.setText(level);
				GameJSON.currentLevelStatusPatch(App.get(), level);
				button.setBackgroundResource(R.drawable.round_button_green);
				Intent intent = new Intent(App.get(), TapAccessibilityService.class);
				intent.putExtra(TapAccessibilityService.ACTION, TapAccessibilityService.PLAY);
				intent.putExtra("interval", 3000);
				int[] location = new int[2];
				view.getLocationOnScreen(location);
				intent.putExtra("x", location[0] - 1);
				intent.putExtra("y", location[1] - 1);
				startService(intent);
				autoclickEnabled = true;
			} else if (position == 1) {
				String level = GameJSON.currentLevelFromLevelsData(App.get());
				button.setText(level);
				GameJSON.currentLevelStatusPuzzlesPatch(App.get(), level);
			}
			listPopupWindow.dismiss();
		});
		listPopupWindow.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (button != null) {
			windowManager.removeView(button);
			button = null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}