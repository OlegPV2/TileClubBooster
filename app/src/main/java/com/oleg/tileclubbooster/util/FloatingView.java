package com.oleg.tileclubbooster.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.oleg.tileclubbooster.OverlayService;
import com.oleg.tileclubbooster.R;
import com.oleg.tileclubbooster.constant.ServiceIntent;

public class FloatingView extends FrameLayout implements View.OnClickListener {

	private final Context mContext;
	private final View mView;
	private final Button button;
	private final FloatingManager floatingManager;
	private WindowManager.LayoutParams params;

	private long lastPressTime;

	@SuppressLint({"ClickableViewAccessibility", "InflateParams"})
	public FloatingView(@NonNull Context context) {
		super(context);
		mContext = context.getApplicationContext();
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		mView = layoutInflater.inflate(R.layout.overlay_button, null);
		button = mView.findViewById(R.id.overlay_button);
		button.setOnClickListener(this);
		button.setOnTouchListener(new OnTouchListener() {
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						long pressTime = System.currentTimeMillis();

						// If double click...
						if (pressTime - lastPressTime <= 500) {
							Intent intent = new Intent(getContext(), OverlayService.class);
							intent.putExtra(ServiceIntent.ACTION, ServiceIntent.DOUBLE_CLICK);
							mContext.startService(intent);
						}
						lastPressTime = pressTime;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						break;
					case MotionEvent.ACTION_UP:
						break;
					case MotionEvent.ACTION_MOVE:
						params.x += (int) (event.getRawX() - initialTouchX);
						params.y += (int) (event.getRawY() - initialTouchY);
						floatingManager.updateView(button, params);
						initialTouchX = (int) event.getRawX();
						initialTouchY = (int) event.getRawY();
						break;
				}
				return false;
			}
		});
		floatingManager = FloatingManager.getInstance(mContext);
	}

	public void show() {
		params = new WindowManager.LayoutParams();
		params.x = 0;
		params.y = 300;
		if (Build.VERSION.SDK_INT >= 26) {
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else {
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}
		params.format = PixelFormat.RGBA_8888;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		floatingManager.addView(mView, params);
	}

	public void hide() {
		floatingManager.removeView(mView);
	}

	public void updateText(String text) {
		button.setText(text);
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent(getContext(), OverlayService.class);
		intent.putExtra(ServiceIntent.ACTION, ServiceIntent.SINGLE_CLICK);
		mContext.startService(intent);
	}
}
